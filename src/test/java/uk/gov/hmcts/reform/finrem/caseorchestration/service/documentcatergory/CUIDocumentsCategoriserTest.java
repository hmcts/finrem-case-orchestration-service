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
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.BUSINESS_VALUATION;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.OTHER_PROPERTY_VALUATION;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.CUIDocumentsCategoriser.Party.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.CUIDocumentsCategoriser.Party.RESPONDENT;

class CUIDocumentsCategoriserTest {

    private static final boolean FDR = true;
    private static final boolean NON_FDR = false;

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

    private FinremCaseData buildCaseData(List<CitizenDocumentCollection> docs, CUIDocumentsCategoriser.Party party) {
        FinremCaseData caseData = new FinremCaseData();
        CitizenDocumentWrapper wrapper = new CitizenDocumentWrapper();

        if (party == APPLICANT) {
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
        CUIDocumentsCategoriser categoriser =
            new CUIDocumentsCategoriser(featureToggleService, APPLICANT);

        assertThatCode(() -> categoriser.categorise(buildCaseData(null, APPLICANT)))
            .doesNotThrowAnyException();
    }

    @Test
    void shouldIgnoreInvalidDocument() {
        CitizenUploadDocument doc = new CitizenUploadDocument();

        CUIDocumentsCategoriser categoriser =
            new CUIDocumentsCategoriser(featureToggleService, APPLICANT);

        categoriser.categorise(buildCaseData(List.of(wrap(doc)), APPLICANT));

        assertThat(doc.getDocumentLink()).isNull();
    }

    @Test
    void shouldIgnoreNullDocumentValue() {
        CUIDocumentsCategoriser categoriser =
            new CUIDocumentsCategoriser(featureToggleService, APPLICANT);

        assertThatCode(() -> categoriser.categorise(buildCaseData(List.of(wrap(null)), APPLICANT)))
            .doesNotThrowAnyException();
    }

    @Test
    void shouldIgnoreDocumentWithoutType() {
        CitizenUploadDocument doc = buildDocument(null, NON_FDR);

        CUIDocumentsCategoriser categoriser =
            new CUIDocumentsCategoriser(featureToggleService, APPLICANT);

        categoriser.categorise(buildCaseData(List.of(wrap(doc)), APPLICANT));

        assertThat(doc.getDocumentLink().getCategoryId()).isNull();
    }

    @Test
    void shouldNotCategoriseWhenCaseFileViewDisabled() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(false);

        CitizenUploadDocument doc = buildDocument(CASE_SUMMARY, NON_FDR);

        new CUIDocumentsCategoriser(featureToggleService, APPLICANT)
            .categorise(buildCaseData(List.of(wrap(doc)), APPLICANT));

        assertThat(doc.getDocumentLink().getCategoryId()).isNull();
    }

    @Test
    void shouldHandleEmptyDocumentListGracefully() {
        CUIDocumentsCategoriser categoriser =
            new CUIDocumentsCategoriser(featureToggleService, APPLICANT);

        assertThatCode(() -> categoriser.categorise(buildCaseData(List.of(), APPLICANT)))
            .doesNotThrowAnyException();
    }

    @Test
    void shouldHandleNullWrapper() {
        CUIDocumentsCategoriser categoriser =
            new CUIDocumentsCategoriser(featureToggleService, APPLICANT);

        FinremCaseData caseData = mock(FinremCaseData.class);
        when(caseData.getCitizenDocumentWrapper()).thenReturn(null);

        assertThatCode(() -> categoriser.categorise(caseData))
            .doesNotThrowAnyException();
    }

    @Test
    void shouldProcessMultipleMixedDocuments() {
        CitizenUploadDocument doc1 =
            buildDocument(CitizenUploadDocumentType.CASE_SUMMARY, NON_FDR);

        CitizenUploadDocument doc2 =
            buildDocument(HOUSING_NEEDS_PROPERTY_PARTICULARS, FDR);

        CitizenUploadDocument doc3 =
            buildDocument(PRE_HEARING_DRAFT_ORDER, NON_FDR);

        CUIDocumentsCategoriser categoriser =
            new CUIDocumentsCategoriser(featureToggleService, RESPONDENT);

        categoriser.categorise(buildCaseData(
            List.of(wrap(doc1), wrap(doc2), wrap(doc3)), RESPONDENT));

        assertThat(doc1.getDocumentLink().getCategoryId()).isNotNull();
        assertThat(doc2.getDocumentLink().getCategoryId()).isNotNull();
        assertThat(doc3.getDocumentLink().getCategoryId()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("documentCategoryProvider")
    void shouldCategorise(
        CitizenUploadDocumentType type,
        boolean isFdr,
        CUIDocumentsCategoriser.Party party,
        DocumentCategory expected
    ) {
        CitizenUploadDocument doc = buildDocument(type, isFdr);

        CUIDocumentsCategoriser categoriser =
            new CUIDocumentsCategoriser(featureToggleService, party);

        FinremCaseData caseData =
            buildCaseData(List.of(wrap(doc)), party);

        categoriser.categorise(caseData);

        if (expected == null) {
            assertThat(doc.getDocumentLink().getCategoryId()).isNull();
        } else {
            assertThat(doc.getDocumentLink().getCategoryId())
                .as("Document type '%s' (FDR: %s) incorrectly categorised. Expected: %s, Actual: %s",
                    type, isFdr, expected, doc.getDocumentLink().getCategoryId())
                .isEqualTo(expected.getDocumentCategoryId());
        }
    }

    @Test
    void shouldHaveTestCaseForEveryCitizenUploadDocumentType() {
        Set<CitizenUploadDocumentType> testedTypes = documentCategoryProvider()
            .map(arguments -> (CitizenUploadDocumentType) arguments.get()[0])
            .collect(java.util.stream.Collectors.toSet());

        assertThat(testedTypes)
            .containsExactlyInAnyOrder(CitizenUploadDocumentType.values());
    }

    private static Stream<Arguments> documentCategoryProvider() {
        return Stream.of(

            applicant(CitizenUploadDocumentType.FAMILY_MEDIATION_INFORMATION_AND_ASSESSMENT_MEETING_MIAM_FORM_FM1,
                NON_FDR, DocumentCategory.APPLICATIONS_MAIN_APPLICATION),

            applicant(POINTS_OF_CLAIM_DEFENCE, NON_FDR, APPLICANT_DOCUMENTS_POINTS_OF_CLAIM_OR_DEFENCE),

            respondent(POINTS_OF_CLAIM_DEFENCE, NON_FDR,
                DocumentCategory.RESPONDENT_DOCUMENTS_POINTS_OF_CLAIM_OR_DEFENCE),

            applicant(POINTS_OF_CLAIM_DEFENCE, FDR, APPLICANT_DOCUMENTS_POINTS_OF_CLAIM_OR_DEFENCE),

            applicant(STATEMENT_OF_POSITION_ON_NON_COURT_DISPUTE_RESOLUTION_NCDR_FORM_FM5,
                NON_FDR, HEARING_DOCUMENTS_APPLICANT_FM5),

            respondent(STATEMENT_OF_POSITION_ON_NON_COURT_DISPUTE_RESOLUTION_NCDR_FORM_FM5,
                FDR, DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_FM5),

            applicant(FINANCIAL_STATEMENT_FORM_E_E1_OR_E2, NON_FDR, APPLICANT_DOCUMENTS_FORM_E),

            respondent(FINANCIAL_STATEMENT_FORM_E_E1_OR_E2, NON_FDR, RESPONDENT_DOCUMENTS_FORM_E),

            applicant(CitizenUploadDocumentType.ATTACHMENTS_TO_FORM_E, NON_FDR, APPLICANT_DOCUMENTS_FORM_E),

            applicant(ESTIMATE_OF_COSTS_INCURRED_FORM_H, NON_FDR,
                HEARING_DOCUMENTS_APPLICANT_COSTS_FORM_H_OR_FORM_H1_OR_FORM_N260),

            respondent(STATEMENT_OF_COSTS_FORM_H1, NON_FDR,
                HEARING_DOCUMENTS_RESPONDENT_COSTS_FORM_H_OR_FORM_H1_OR_FORM_N260),

            respondent(STATEMENT_OF_COSTS_SUMMARY_ASSESSMENT_FORM_N260, NON_FDR,
                HEARING_DOCUMENTS_RESPONDENT_COSTS_FORM_H_OR_FORM_H1_OR_FORM_N260),

            applicant(CERTIFICATE_OF_SERVICE_FORM_FP6, NON_FDR, APPLICANT_DOCUMENTS_CERTIFICATES_OF_SERVICE),

            respondent(CERTIFICATE_OF_SERVICE_FORM_FP6, NON_FDR,
                DocumentCategory.RESPONDENT_DOCUMENTS_CERTIFICATES_OF_SERVICE),

            applicant(RESPONSE_TO_THE_NOTICE_OF_FIRST_APPOINTMENT_FORM_G, NON_FDR,
                DocumentCategory.APPLICANT_DOCUMENTS_FORM_G),

            respondent(RESPONSE_TO_THE_NOTICE_OF_FIRST_APPOINTMENT_FORM_G, NON_FDR, RESPONDENT_DOCUMENTS_FORM_G),

            applicant(SCHEDULE_OF_DEFICIENCIES, NON_FDR, HEARING_DOCUMENTS_APPLICANT_REPLIES_TO_QUESTIONNAIRE),

            respondent(SCHEDULE_OF_DEFICIENCIES, NON_FDR,
                DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_REPLIES_TO_QUESTIONNAIRE),

            applicant(SCHEDULE_OF_DEFICIENCIES, FDR, FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER),

            respondent(SCHEDULE_OF_DEFICIENCIES, FDR, FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER),

            applicant(CASE_SUMMARY, NON_FDR, HEARING_DOCUMENTS_APPLICANT_CASE_SUMMARY),

            respondent(CASE_SUMMARY, NON_FDR, DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_CASE_SUMMARY),

            applicant(CASE_SUMMARY, FDR, FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER),

            respondent(CASE_SUMMARY, FDR, FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER),

            applicant(POSITION_STATEMENT, FDR, FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_POSITION_STATEMENTS),

            applicant(POSITION_STATEMENT, NON_FDR, DocumentCategory.HEARING_DOCUMENTS_APPLICANT_POSITION_STATEMENT),

            respondent(POSITION_STATEMENT, NON_FDR, HEARING_DOCUMENTS_RESPONDENT_POSITION_STATEMENT),

            respondent(POSITION_STATEMENT, FDR, FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_POSITION_STATEMENTS),

            applicant(CHRONOLOGY, FDR, FDR_JOINT_DOCUMENTS_CHRONOLOGY),

            applicant(CHRONOLOGY, NON_FDR, DocumentCategory.HEARING_DOCUMENTS_APPLICANT_CHRONOLOGY),

            respondent(CHRONOLOGY, NON_FDR, HEARING_DOCUMENTS_RESPONDENT_CHRONOLOGY),

            applicant(STATEMENT_OF_ISSUES, NON_FDR, HEARING_DOCUMENTS_APPLICANT_CONCISE_STATEMENT_OF_ISSUES),

            respondent(STATEMENT_OF_ISSUES, NON_FDR,
                DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_CONCISE_STATEMENT_OF_ISSUES),

            applicant(STATEMENT_OF_ISSUES, FDR, FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_POSITION_STATEMENTS),

            respondent(STATEMENT_OF_ISSUES, FDR, FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_POSITION_STATEMENTS),

            applicant(DIVORCE_APPLICATION_PETITION, NON_FDR, DIVORCE_DOCUMENTS_APPLICATION_OR_PETITION),

            applicant(DIVORCE_CONDITIONAL_ORDER_DECREE_NISI, NON_FDR,
                DIVORCE_DOCUMENTS_CONDITIONAL_ORDER_OR_DECREE_NISI),

            applicant(DIVORCE_FINAL_ORDER_DECREE_ABSOLUTE, NON_FDR, DIVORCE_DOCUMENTS_FINAL_ORDER_OR_DECREE_ABSOLUTE),

            applicant(COMPOSITE_CASE_SUMMARY_FORM_ES1, FDR, FDR_JOINT_DOCUMENTS_ES1),

            applicant(COMPOSITE_CASE_SUMMARY_FORM_ES1, NON_FDR, DocumentCategory.HEARING_DOCUMENTS_APPLICANT_ES1),

            respondent(COMPOSITE_CASE_SUMMARY_FORM_ES1, NON_FDR, DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_ES1),

            applicant(COMPOSITE_SCHEDULE_OF_ASSETS_AND_INCOME_FORM_ES2, FDR, DocumentCategory.FDR_JOINT_DOCUMENTS_ES2),

            applicant(COMPOSITE_SCHEDULE_OF_ASSETS_AND_INCOME_FORM_ES2, NON_FDR,
                DocumentCategory.HEARING_DOCUMENTS_APPLICANT_ES2),

            respondent(COMPOSITE_SCHEDULE_OF_ASSETS_AND_INCOME_FORM_ES2, NON_FDR, HEARING_DOCUMENTS_RESPONDENT_ES2),

            applicant(MARKET_APPRAISAL_OR_VALUATION_OF_FAMILY_HOME, FDR, FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER),

            respondent(MARKET_APPRAISAL_OR_VALUATION_OF_FAMILY_HOME, FDR,
                FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER),

            respondent(MARKET_APPRAISAL_OR_VALUATION_OF_FAMILY_HOME, NON_FDR,
                RESPONDENT_MORTGAGE_CAPACITIES_OR_MARKET_APPRAISAL),

            applicant(MARKET_APPRAISAL_OR_VALUATION_OF_FAMILY_HOME, NON_FDR,
                APPLICANT_MORTGAGE_CAPACITIES_OR_MARKET_APPRAISAL),

            respondent(HOUSING_NEEDS_PROPERTY_PARTICULARS, FDR, FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER),

            applicant(HOUSING_NEEDS_PROPERTY_PARTICULARS, NON_FDR,
                APPLICANT_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS),

            respondent(HOUSING_NEEDS_PROPERTY_PARTICULARS, NON_FDR,
                DocumentCategory.RESPONDENT_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS),

            applicant(HOUSING_NEEDS_PROPERTY_PARTICULARS, FDR, FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER),

            respondent(CitizenUploadDocumentType.POTENTIAL_BORROWING_CAPACITY_MORTGAGE_CAPACITIES, FDR,
                FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER),

            respondent(CitizenUploadDocumentType.POTENTIAL_BORROWING_CAPACITY_MORTGAGE_CAPACITIES, NON_FDR,
                DocumentCategory.RESPONDENT_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS),

            applicant(CitizenUploadDocumentType.POTENTIAL_BORROWING_CAPACITY_MORTGAGE_CAPACITIES, NON_FDR,
                APPLICANT_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS),

            applicant(OPEN_OFFERS, FDR, FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER),

            respondent(OPEN_OFFERS, FDR, FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER),

            applicant(OPEN_OFFERS, NON_FDR, DocumentCategory.APPLICANT_DOCUMENTS_OPEN_OFFERS),

            respondent(OPEN_OFFERS, NON_FDR, RESPONDENT_DOCUMENTS_OPEN_OFFERS),

            applicant(QUESTIONNAIRE_REQUEST_FOR_FURTHER_DOCUMENTS, FDR, FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER),

            respondent(QUESTIONNAIRE_REQUEST_FOR_FURTHER_DOCUMENTS, FDR,
                FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER),

            applicant(QUESTIONNAIRE_REQUEST_FOR_FURTHER_DOCUMENTS, NON_FDR,
                HEARING_DOCUMENTS_APPLICANT_QUESTIONNAIRES),

            respondent(QUESTIONNAIRE_REQUEST_FOR_FURTHER_DOCUMENTS, NON_FDR,
                HEARING_DOCUMENTS_RESPONDENT_QUESTIONNAIRES),

            applicant(SUPPLEMENTAL_QUESTIONNAIRE, NON_FDR, HEARING_DOCUMENTS_APPLICANT_QUESTIONNAIRES),

            respondent(SUPPLEMENTAL_QUESTIONNAIRE, FDR, FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER),

            applicant(SECTION_25_STATEMENT, FDR, FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER),

            respondent(SECTION_25_STATEMENT, FDR, FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER),

            applicant(SECTION_25_STATEMENT, NON_FDR, DocumentCategory.APPLICANT_DOCUMENTS_S25_STATEMENT),

            respondent(SECTION_25_STATEMENT, NON_FDR, RESPONDENT_DOCUMENTS_S25_STATEMENT),

            applicant(WITNESS_STATEMENT, FDR, FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER),

            respondent(WITNESS_STATEMENT, FDR, FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER),

            applicant(WITNESS_STATEMENT, NON_FDR, DocumentCategory.APPLICANT_DOCUMENTS_WITNESS_STATEMENTS),

            respondent(WITNESS_STATEMENT, NON_FDR, RESPONDENT_DOCUMENTS_WITNESS_STATEMENTS),

            applicant(WITHOUT_PREJUDICE_OFFERS_FOR_SETTLEMENT, FDR,
                FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_WITHOUT_PREJUDICE_OFFERS),

            respondent(WITHOUT_PREJUDICE_OFFERS_FOR_SETTLEMENT, FDR,
                DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_WITHOUT_PREJUDICE_OFFERS),

            applicant(WITHOUT_PREJUDICE_OFFERS_FOR_SETTLEMENT, NON_FDR,
                FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_WITHOUT_PREJUDICE_OFFERS),

            respondent(WITHOUT_PREJUDICE_OFFERS_FOR_SETTLEMENT, NON_FDR,
                DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_WITHOUT_PREJUDICE_OFFERS),

            applicant(PENSION_REPORT_EXPERT_REPORT, FDR, FDR_REPORTS),

            applicant(PENSION_REPORT_EXPERT_REPORT, NON_FDR, REPORTS),

            applicant(BUSINESS_VALUATION, NON_FDR, REPORTS),

            applicant(OTHER_PROPERTY_VALUATION, NON_FDR, REPORTS),

            applicant(MEDICAL_REPORT, FDR, FDR_REPORTS),

            applicant(MEDICAL_REPORT, NON_FDR, REPORTS),

            applicant(HEARING_BUNDLE, FDR, FDR_BUNDLE),

            applicant(HEARING_BUNDLE, NON_FDR, DocumentCategory.HEARING_BUNDLE),

            respondent(CitizenUploadDocumentType.FDR_BUNDLE, FDR, FDR_BUNDLE),

            respondent(PRE_HEARING_DRAFT_ORDER, FDR, FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_DRAFT_ORDER),

            applicant(PRE_HEARING_DRAFT_ORDER, FDR,
                DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_DRAFT_ORDER),

            applicant(PRE_HEARING_DRAFT_ORDER, NON_FDR, HEARING_DOCUMENTS_APPLICANT_PRE_HEARING_DRAFT_ORDER),

            respondent(PRE_HEARING_DRAFT_ORDER, NON_FDR, HEARING_DOCUMENTS_RESPONDENT_PRE_HEARING_DRAFT_ORDER),

            applicant(REPLY_TO_QUESTIONNAIRE, NON_FDR, APPLICANT_DOCUMENTS_REPLIES_TO_QUESTIONNAIRE),

            respondent(REPLY_TO_SCHEDULE_OF_DEFICIENCIES_OR_SUPPLEMENTAL_QUESTIONNAIRES, NON_FDR,
                RESPONDENT_DOCUMENTS_REPLIES_TO_QUESTIONNAIRE),

            applicant(CitizenUploadDocumentType.REPLY_TO_QUESTIONNAIRE_SUPPORTING_DOCUMENTS, NON_FDR,
                APPLICANT_DOCUMENTS_REPLIES_TO_QUESTIONNAIRE),

            respondent(
                CitizenUploadDocumentType.REPLY_TO_SCHEDULE_OF_DEFICIENCIES_OR_SUPPLEMENTAL_QUESTIONNAIRES_SUPPORTING_DOCUMENTS,
                NON_FDR,
                RESPONDENT_DOCUMENTS_REPLIES_TO_QUESTIONNAIRE
            ),

            applicant(CitizenUploadDocumentType.UPDATING_DISCLOSURE, NON_FDR,
                DocumentCategory.APPLICANT_DOCUMENTS_UPDATING_DISCLOSURE),

            respondent(CitizenUploadDocumentType.UPDATING_DISCLOSURE, NON_FDR,
                DocumentCategory.RESPONDENT_DOCUMENTS_UPDATING_DISCLOSURE),

            applicant(CitizenUploadDocumentType.BANK_STATEMENTS, NON_FDR,
                DocumentCategory.APPLICANT_DOCUMENTS_UPDATING_DISCLOSURE),

            applicant(CitizenUploadDocumentType.PAYSLIPS, NON_FDR,
                DocumentCategory.APPLICANT_DOCUMENTS_UPDATING_DISCLOSURE),

            applicant(CitizenUploadDocumentType.P60, NON_FDR,
                DocumentCategory.APPLICANT_DOCUMENTS_UPDATING_DISCLOSURE),

            applicant(CitizenUploadDocumentType.P45, NON_FDR,
                DocumentCategory.APPLICANT_DOCUMENTS_UPDATING_DISCLOSURE),

            applicant(CitizenUploadDocumentType.DEBT_STATEMENT, NON_FDR,
                DocumentCategory.APPLICANT_DOCUMENTS_UPDATING_DISCLOSURE),

            applicant(CitizenUploadDocumentType.LIST_OF_ASSETS, NON_FDR,
                DocumentCategory.APPLICANT_DOCUMENTS_UPDATING_DISCLOSURE),

            applicant(CitizenUploadDocumentType.LOAN_STATEMENT, NON_FDR,
                DocumentCategory.APPLICANT_DOCUMENTS_UPDATING_DISCLOSURE),

            applicant(CitizenUploadDocumentType.CAR_INSURANCE_LOAN_STATEMENT, NON_FDR,
                DocumentCategory.APPLICANT_DOCUMENTS_UPDATING_DISCLOSURE),

            applicant(CitizenUploadDocumentType.PERSONAL_SELLING_SIGHT_STATEMENT, NON_FDR,
                DocumentCategory.APPLICANT_DOCUMENTS_UPDATING_DISCLOSURE),

            applicant(CitizenUploadDocumentType.SELF_ASSESSMENT_TAX_FORMS, NON_FDR,
                DocumentCategory.APPLICANT_DOCUMENTS_UPDATING_DISCLOSURE),

            applicant(CitizenUploadDocumentType.UNIVERSAL_CREDIT_STATEMENT, NON_FDR,
                DocumentCategory.APPLICANT_DOCUMENTS_UPDATING_DISCLOSURE),

            applicant(CitizenUploadDocumentType.MORTGAGE_STATEMENTS_FOR_FAMILY_HOME, NON_FDR,
                DocumentCategory.APPLICANT_DOCUMENTS_UPDATING_DISCLOSURE),

            applicant(CitizenUploadDocumentType.MORTGAGE_STATEMENTS_FOR_OTHER_PROPERTIES, NON_FDR,
                DocumentCategory.APPLICANT_DOCUMENTS_UPDATING_DISCLOSURE),

            applicant(CitizenUploadDocumentType.INVESTMENT_STATEMENTS, NON_FDR,
                DocumentCategory.APPLICANT_DOCUMENTS_UPDATING_DISCLOSURE),

            applicant(CitizenUploadDocumentType.BUSINESS_ACCOUNTS, NON_FDR,
                DocumentCategory.APPLICANT_DOCUMENTS_UPDATING_DISCLOSURE),

            applicant(CitizenUploadDocumentType.P11D, NON_FDR,
                DocumentCategory.APPLICANT_DOCUMENTS_UPDATING_DISCLOSURE),

            applicant(CitizenUploadDocumentType.TAX_ASSESSMENTS, NON_FDR,
                DocumentCategory.APPLICANT_DOCUMENTS_UPDATING_DISCLOSURE),

            applicant(CitizenUploadDocumentType.INCOME_EVIDENCE, NON_FDR,
                DocumentCategory.APPLICANT_DOCUMENTS_UPDATING_DISCLOSURE),

            applicant(CitizenUploadDocumentType.PENSION_STATEMENT, NON_FDR,
                DocumentCategory.APPLICANT_DOCUMENTS_UPDATING_DISCLOSURE),

            applicant(CitizenUploadDocumentType.LIFE_INSURANCE_INCLUDING_ENDOWMENT_POLICIES, NON_FDR,
                DocumentCategory.APPLICANT_DOCUMENTS_UPDATING_DISCLOSURE),

            applicant(CitizenUploadDocumentType.MANAGEMENT_ACCOUNTS, NON_FDR,
                DocumentCategory.APPLICANT_DOCUMENTS_UPDATING_DISCLOSURE),

            applicant(CitizenUploadDocumentType.SCHOOL_FEES, NON_FDR, null)
        );
    }

    private static Arguments applicant(CitizenUploadDocumentType type, boolean isFdr, DocumentCategory expected) {
        return category(type, isFdr, APPLICANT, expected);
    }

    private static Arguments respondent(CitizenUploadDocumentType type, boolean isFdr, DocumentCategory expected) {
        return category(type, isFdr, RESPONDENT, expected);
    }

    private static Arguments category(CitizenUploadDocumentType type,
                                      boolean isFdr,
                                      CUIDocumentsCategoriser.Party party,
                                      DocumentCategory expected) {
        return Arguments.of(type, isFdr, party, expected);
    }

}
