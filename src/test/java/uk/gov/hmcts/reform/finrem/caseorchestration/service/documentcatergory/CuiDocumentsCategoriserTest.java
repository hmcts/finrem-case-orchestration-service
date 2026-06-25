package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CitizenDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.CASE_SUMMARY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.CERTIFICATE_OF_SERVICE_FORM_FP6;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.CHRONOLOGY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.COMPOSITE_CASE_SUMMARY_FORM_ES1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.COMPOSITE_SCHEDULE_OF_ASSETS_AND_INCOME_FORM_ES2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.DIVORCE_APPLICATION_PETITION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.DIVORCE_CONDITIONAL_ORDER_DECREE_NISI;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.DIVORCE_FINAL_ORDER_DECREE_ABSOLUTE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.ESTIMATE_OF_COSTS_INCURRED_FORM_H;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.FINANCIAL_STATEMENT_FORM_E_E1_OR_E2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.HEARING_BUNDLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.HOUSING_NEEDS_PROPERTY_PARTICULARS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.MARKET_APPRAISAL_OR_VALUATION_OF_FAMILY_HOME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.MEDICAL_REPORT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.OPEN_OFFERS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.PENSION_REPORT_EXPERT_REPORT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.POINTS_OF_CLAIM_DEFENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.POSITION_STATEMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.PRE_HEARING_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.QUESTIONNAIRE_REQUEST_FOR_FURTHER_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.REPLY_TO_QUESTIONNAIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.REPLY_TO_SCHEDULE_OF_DEFICIENCIES_OR_SUPPLEMENTAL_QUESTIONNAIRES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.RESPONSE_TO_THE_NOTICE_OF_FIRST_APPOINTMENT_FORM_G;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.SCHEDULE_OF_DEFICIENCIES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.SECTION_25_STATEMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.STATEMENT_OF_COSTS_FORM_H1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.STATEMENT_OF_COSTS_SUMMARY_ASSESSMENT_FORM_N260;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.STATEMENT_OF_ISSUES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.STATEMENT_OF_POSITION_ON_NON_COURT_DISPUTE_RESOLUTION_NCDR_FORM_FM5;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.SUPPLEMENTAL_QUESTIONNAIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.WITHOUT_PREJUDICE_OFFERS_FOR_SETTLEMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.WITNESS_STATEMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.APPLICANT_DOCUMENTS_CERTIFICATES_OF_SERVICE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.APPLICANT_DOCUMENTS_FORM_E;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.APPLICANT_DOCUMENTS_POINTS_OF_CLAIM_OR_DEFENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.APPLICANT_DOCUMENTS_REPLIES_TO_QUESTIONNAIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.APPLICANT_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.APPLICANT_MORTGAGE_CAPACITIES_OR_MARKET_APPRAISAL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.DIVORCE_DOCUMENTS_APPLICATION_OR_PETITION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.DIVORCE_DOCUMENTS_CONDITIONAL_ORDER_OR_DECREE_NISI;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.DIVORCE_DOCUMENTS_FINAL_ORDER_OR_DECREE_ABSOLUTE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.FDR_BUNDLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_POSITION_STATEMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_WITHOUT_PREJUDICE_OFFERS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_POSITION_STATEMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.FDR_JOINT_DOCUMENTS_CHRONOLOGY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.FDR_JOINT_DOCUMENTS_ES1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.FDR_REPORTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_DOCUMENTS_APPLICANT_CASE_SUMMARY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_DOCUMENTS_APPLICANT_CONCISE_STATEMENT_OF_ISSUES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_DOCUMENTS_APPLICANT_COSTS_FORM_H_OR_FORM_H1_OR_FORM_N260;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_DOCUMENTS_APPLICANT_FM5;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_DOCUMENTS_APPLICANT_PRE_HEARING_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_DOCUMENTS_APPLICANT_QUESTIONNAIRES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_DOCUMENTS_APPLICANT_REPLIES_TO_QUESTIONNAIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_CHRONOLOGY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_COSTS_FORM_H_OR_FORM_H1_OR_FORM_N260;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_ES2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_POSITION_STATEMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_PRE_HEARING_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_QUESTIONNAIRES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.REPORTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.RESPONDENT_DOCUMENTS_FORM_E;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.RESPONDENT_DOCUMENTS_FORM_G;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.RESPONDENT_DOCUMENTS_OPEN_OFFERS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.RESPONDENT_DOCUMENTS_REPLIES_TO_QUESTIONNAIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.RESPONDENT_DOCUMENTS_S25_STATEMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.RESPONDENT_DOCUMENTS_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.RESPONDENT_MORTGAGE_CAPACITIES_OR_MARKET_APPRAISAL;

class CuiDocumentsCategoriserTest {

    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        featureToggleService = mock(FeatureToggleService.class);
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
    }

    private CitizenUploadDocument buildDocument(CitizenUploadDocumentType type, boolean isFdr) {
        CitizenUploadDocument doc = new CitizenUploadDocument();
        CaseDocument caseDocument = new CaseDocument();
        doc.setDocumentLink(caseDocument);
        doc.setDocumentType(type);
        doc.setIsFdr(isFdr ? YesOrNo.YES : YesOrNo.NO);
        return doc;
    }

    private FinremCaseData buildCaseData(List<CitizenDocumentCollection> docs, boolean applicant) {
        FinremCaseData caseData = new FinremCaseData();
        CitizenDocumentWrapper wrapper = new CitizenDocumentWrapper();

        if (applicant) {
            wrapper.setCitizenApplicantDocument(docs);
        } else {
            wrapper.setCitizenRespondentDocument(docs);
        }

        caseData.setCitizenDocumentWrapper(wrapper);
        return caseData;
    }

    private CitizenDocumentCollection wrap(CitizenUploadDocument doc) {
        CitizenDocumentCollection c = new CitizenDocumentCollection();
        c.setValue(doc);
        return c;
    }

    @Test
    void shouldHandleEmptyDocumentsGracefully() {
        CuiDocumentsCategoriser categoriser =
            new CuiDocumentsCategoriser(featureToggleService, CuiDocumentsCategoriser.Party.APPLICANT);

        assertThatCode(() -> categoriser.categorise(buildCaseData(null, true)))
            .doesNotThrowAnyException();
    }

    @Test
    void shouldIgnoreInvalidDocument() {
        CitizenUploadDocument doc = new CitizenUploadDocument();

        CuiDocumentsCategoriser categoriser =
            new CuiDocumentsCategoriser(featureToggleService, CuiDocumentsCategoriser.Party.APPLICANT);

        categoriser.categorise(buildCaseData(List.of(wrap(doc)), true));

        assertThat(doc.getDocumentLink()).isNull();
    }

    @Test
    void shouldHandleNullWrapper() {
        CuiDocumentsCategoriser categoriser =
            new CuiDocumentsCategoriser(featureToggleService, CuiDocumentsCategoriser.Party.APPLICANT);

        FinremCaseData caseData = new FinremCaseData();

        assertThatCode(() -> categoriser.categorise(caseData))
            .doesNotThrowAnyException();
    }

    @Test
    void shouldProcessMultipleMixedDocuments() {
        CitizenUploadDocument doc1 =
            buildDocument(CitizenUploadDocumentType.CASE_SUMMARY, false);

        CitizenUploadDocument doc2 =
            buildDocument(CitizenUploadDocumentType.HOUSING_NEEDS_PROPERTY_PARTICULARS, true);

        CitizenUploadDocument doc3 =
            buildDocument(CitizenUploadDocumentType.PRE_HEARING_DRAFT_ORDER, false);

        CuiDocumentsCategoriser categoriser =
            new CuiDocumentsCategoriser(featureToggleService,
                CuiDocumentsCategoriser.Party.RESPONDENT);

        categoriser.categorise(buildCaseData(
            List.of(wrap(doc1), wrap(doc2), wrap(doc3)), false));

        assertThat(doc1.getDocumentLink().getCategoryId()).isNotNull();
        assertThat(doc2.getDocumentLink().getCategoryId()).isNotNull();
        assertThat(doc3.getDocumentLink().getCategoryId()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("documentCategoryProvider")
    void shouldCategorise(
        CitizenUploadDocumentType type,
        boolean isFdr,
        CuiDocumentsCategoriser.Party party,
        boolean applicant,
        DocumentCategory expected
    ) {
        CitizenUploadDocument doc = buildDocument(type, isFdr);

        CuiDocumentsCategoriser categoriser =
            new CuiDocumentsCategoriser(featureToggleService, party);

        FinremCaseData caseData =
            buildCaseData(List.of(wrap(doc)), applicant);

        categoriser.categorise(caseData);

        if (expected == null) {
            assertThat(doc.getDocumentLink().getCategoryId()).isNull();
        } else {
            assertThat(doc.getDocumentLink().getCategoryId())
                .isEqualTo(expected.getDocumentCategoryId());
        }
    }

    private static Stream<Arguments> documentCategoryProvider() {
        return Stream.of(

            Arguments.of(FINANCIAL_STATEMENT_FORM_E_E1_OR_E2, false,
                CuiDocumentsCategoriser.Party.APPLICANT, true, APPLICANT_DOCUMENTS_FORM_E),

            Arguments.of(FINANCIAL_STATEMENT_FORM_E_E1_OR_E2, false,
                CuiDocumentsCategoriser.Party.RESPONDENT, false, RESPONDENT_DOCUMENTS_FORM_E),

            Arguments.of(PENSION_REPORT_EXPERT_REPORT, true,
                CuiDocumentsCategoriser.Party.APPLICANT, true, FDR_REPORTS),

            Arguments.of(PENSION_REPORT_EXPERT_REPORT, false,
                CuiDocumentsCategoriser.Party.APPLICANT, true, REPORTS),

            Arguments.of(STATEMENT_OF_POSITION_ON_NON_COURT_DISPUTE_RESOLUTION_NCDR_FORM_FM5,
                false, CuiDocumentsCategoriser.Party.APPLICANT, true,
                HEARING_DOCUMENTS_APPLICANT_FM5),

            Arguments.of(STATEMENT_OF_COSTS_FORM_H1, false,
                CuiDocumentsCategoriser.Party.RESPONDENT, false,
                HEARING_DOCUMENTS_RESPONDENT_COSTS_FORM_H_OR_FORM_H1_OR_FORM_N260),

            Arguments.of(SCHEDULE_OF_DEFICIENCIES, false,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                HEARING_DOCUMENTS_APPLICANT_REPLIES_TO_QUESTIONNAIRE),

            Arguments.of(SCHEDULE_OF_DEFICIENCIES, true,
                CuiDocumentsCategoriser.Party.RESPONDENT, false,
                FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER),

            Arguments.of(CASE_SUMMARY, false,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                HEARING_DOCUMENTS_APPLICANT_CASE_SUMMARY),

            Arguments.of(CASE_SUMMARY, true,
                CuiDocumentsCategoriser.Party.RESPONDENT, false,
                FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER),

            Arguments.of(POSITION_STATEMENT, true,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_POSITION_STATEMENTS),

            Arguments.of(POSITION_STATEMENT, false,
                CuiDocumentsCategoriser.Party.RESPONDENT, false,
                HEARING_DOCUMENTS_RESPONDENT_POSITION_STATEMENT),

            Arguments.of(CHRONOLOGY, true,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                FDR_JOINT_DOCUMENTS_CHRONOLOGY),

            Arguments.of(CHRONOLOGY, false,
                CuiDocumentsCategoriser.Party.RESPONDENT, false,
                HEARING_DOCUMENTS_RESPONDENT_CHRONOLOGY),

            Arguments.of(STATEMENT_OF_ISSUES, false,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                HEARING_DOCUMENTS_APPLICANT_CONCISE_STATEMENT_OF_ISSUES),

            Arguments.of(COMPOSITE_CASE_SUMMARY_FORM_ES1, true,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                FDR_JOINT_DOCUMENTS_ES1),

            Arguments.of(COMPOSITE_SCHEDULE_OF_ASSETS_AND_INCOME_FORM_ES2, false,
                CuiDocumentsCategoriser.Party.RESPONDENT, false,
                HEARING_DOCUMENTS_RESPONDENT_ES2),

            Arguments.of(MARKET_APPRAISAL_OR_VALUATION_OF_FAMILY_HOME, true,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER),

            Arguments.of(MARKET_APPRAISAL_OR_VALUATION_OF_FAMILY_HOME, false,
                CuiDocumentsCategoriser.Party.RESPONDENT, false,
                RESPONDENT_MORTGAGE_CAPACITIES_OR_MARKET_APPRAISAL),

            Arguments.of(HOUSING_NEEDS_PROPERTY_PARTICULARS, true,
                CuiDocumentsCategoriser.Party.RESPONDENT, false,
                FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER),

            Arguments.of(HOUSING_NEEDS_PROPERTY_PARTICULARS, false,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                APPLICANT_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS),

            Arguments.of(QUESTIONNAIRE_REQUEST_FOR_FURTHER_DOCUMENTS, true,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER),

            Arguments.of(QUESTIONNAIRE_REQUEST_FOR_FURTHER_DOCUMENTS, false,
                CuiDocumentsCategoriser.Party.RESPONDENT, false,
                HEARING_DOCUMENTS_RESPONDENT_QUESTIONNAIRES),

            Arguments.of(SECTION_25_STATEMENT, false,
                CuiDocumentsCategoriser.Party.RESPONDENT, false,
                RESPONDENT_DOCUMENTS_S25_STATEMENT),

            Arguments.of(WITNESS_STATEMENT, true,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER),

            Arguments.of(WITNESS_STATEMENT, false,
                CuiDocumentsCategoriser.Party.RESPONDENT, false,
                RESPONDENT_DOCUMENTS_WITNESS_STATEMENTS),

            Arguments.of(WITHOUT_PREJUDICE_OFFERS_FOR_SETTLEMENT, true,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_WITHOUT_PREJUDICE_OFFERS),

            Arguments.of(WITHOUT_PREJUDICE_OFFERS_FOR_SETTLEMENT, false,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                null),

            Arguments.of(OPEN_OFFERS, true,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER),

            Arguments.of(OPEN_OFFERS, false,
                CuiDocumentsCategoriser.Party.RESPONDENT, false,
                RESPONDENT_DOCUMENTS_OPEN_OFFERS),

            Arguments.of(HEARING_BUNDLE, true,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                FDR_BUNDLE),

            Arguments.of(PRE_HEARING_DRAFT_ORDER, true,
                CuiDocumentsCategoriser.Party.RESPONDENT, false,
                FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_DRAFT_ORDER),

            Arguments.of(PRE_HEARING_DRAFT_ORDER, false,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                HEARING_DOCUMENTS_APPLICANT_PRE_HEARING_DRAFT_ORDER),

            Arguments.of(POINTS_OF_CLAIM_DEFENCE, false,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                APPLICANT_DOCUMENTS_POINTS_OF_CLAIM_OR_DEFENCE),

            Arguments.of(POINTS_OF_CLAIM_DEFENCE, true,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                null),

            Arguments.of(ESTIMATE_OF_COSTS_INCURRED_FORM_H, false,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                HEARING_DOCUMENTS_APPLICANT_COSTS_FORM_H_OR_FORM_H1_OR_FORM_N260),

            Arguments.of(STATEMENT_OF_COSTS_SUMMARY_ASSESSMENT_FORM_N260, false,
                CuiDocumentsCategoriser.Party.RESPONDENT, false,
                HEARING_DOCUMENTS_RESPONDENT_COSTS_FORM_H_OR_FORM_H1_OR_FORM_N260),

            Arguments.of(SUPPLEMENTAL_QUESTIONNAIRE, false,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                HEARING_DOCUMENTS_APPLICANT_QUESTIONNAIRES),

            Arguments.of(REPLY_TO_QUESTIONNAIRE, false,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                APPLICANT_DOCUMENTS_REPLIES_TO_QUESTIONNAIRE),

            Arguments.of(REPLY_TO_SCHEDULE_OF_DEFICIENCIES_OR_SUPPLEMENTAL_QUESTIONNAIRES, false,
                CuiDocumentsCategoriser.Party.RESPONDENT, false,
                RESPONDENT_DOCUMENTS_REPLIES_TO_QUESTIONNAIRE),

            Arguments.of(DIVORCE_APPLICATION_PETITION, false,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                DIVORCE_DOCUMENTS_APPLICATION_OR_PETITION),

            Arguments.of(DIVORCE_CONDITIONAL_ORDER_DECREE_NISI, false,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                DIVORCE_DOCUMENTS_CONDITIONAL_ORDER_OR_DECREE_NISI),

            Arguments.of(DIVORCE_FINAL_ORDER_DECREE_ABSOLUTE, false,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                DIVORCE_DOCUMENTS_FINAL_ORDER_OR_DECREE_ABSOLUTE),

            Arguments.of(CERTIFICATE_OF_SERVICE_FORM_FP6, false,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                APPLICANT_DOCUMENTS_CERTIFICATES_OF_SERVICE),

            Arguments.of(RESPONSE_TO_THE_NOTICE_OF_FIRST_APPOINTMENT_FORM_G, false,
                CuiDocumentsCategoriser.Party.RESPONDENT, false,
                RESPONDENT_DOCUMENTS_FORM_G),

            Arguments.of(MEDICAL_REPORT, true,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                FDR_REPORTS),

            Arguments.of(MEDICAL_REPORT, false,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                REPORTS),

            Arguments.of(SUPPLEMENTAL_QUESTIONNAIRE, true,
                CuiDocumentsCategoriser.Party.RESPONDENT, false,
                FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER),

            Arguments.of(
                CitizenUploadDocumentType.STATEMENT_OF_ISSUES,
                true,
                CuiDocumentsCategoriser.Party.RESPONDENT,
                false,
                FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_POSITION_STATEMENTS
            ),

            Arguments.of(
                CitizenUploadDocumentType.MARKET_APPRAISAL_OR_VALUATION_OF_FAMILY_HOME,
                false,
                CuiDocumentsCategoriser.Party.APPLICANT,
                true,
                APPLICANT_MORTGAGE_CAPACITIES_OR_MARKET_APPRAISAL
            ),

            Arguments.of(
                CitizenUploadDocumentType.HOUSING_NEEDS_PROPERTY_PARTICULARS,
                true,
                CuiDocumentsCategoriser.Party.APPLICANT,
                true,
                FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER
            ),

            Arguments.of(
                CitizenUploadDocumentType.PRE_HEARING_DRAFT_ORDER,
                false,
                CuiDocumentsCategoriser.Party.RESPONDENT,
                false,
                HEARING_DOCUMENTS_RESPONDENT_PRE_HEARING_DRAFT_ORDER
            ),

            Arguments.of(
                CitizenUploadDocumentType.FDR_BUNDLE,
                true,
                CuiDocumentsCategoriser.Party.RESPONDENT,
                false,
                FDR_BUNDLE
            )
        );
    }

}
