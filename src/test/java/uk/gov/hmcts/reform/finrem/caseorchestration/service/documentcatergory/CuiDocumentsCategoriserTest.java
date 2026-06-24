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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.CHRONOLOGY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.COMPOSITE_CASE_SUMMARY_FORM_ES1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.COMPOSITE_SCHEDULE_OF_ASSETS_AND_INCOME_FORM_ES2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.FINANCIAL_STATEMENT_FORM_E_E1_OR_E2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.HEARING_BUNDLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.PENSION_REPORT_EXPERT_REPORT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.POINTS_OF_CLAIM_DEFENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.POSITION_STATEMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.SCHEDULE_OF_DEFICIENCIES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.SECTION_25_STATEMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.STATEMENT_OF_COSTS_FORM_H1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.STATEMENT_OF_POSITION_ON_NON_COURT_DISPUTE_RESOLUTION_NCDR_FORM_FM5;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.WITHOUT_PREJUDICE_OFFERS_FOR_SETTLEMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.WITNESS_STATEMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.APPLICANT_DOCUMENTS_FORM_E;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.APPLICANT_DOCUMENTS_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.FDR_BUNDLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_POSITION_STATEMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.FDR_JOINT_DOCUMENTS_CHRONOLOGY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.FDR_JOINT_DOCUMENTS_ES1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.FDR_REPORTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_DOCUMENTS_APPLICANT_CASE_SUMMARY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_DOCUMENTS_APPLICANT_FM5;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_DOCUMENTS_APPLICANT_REPLIES_TO_QUESTIONNAIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_COSTS_FORM_H_OR_FORM_H1_OR_FORM_N260;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_ES2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.REPORTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.RESPONDENT_DOCUMENTS_FORM_E;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.RESPONDENT_DOCUMENTS_S25_STATEMENT;

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

            Arguments.of(FINANCIAL_STATEMENT_FORM_E_E1_OR_E2, false, CuiDocumentsCategoriser.Party.APPLICANT, true,
                APPLICANT_DOCUMENTS_FORM_E),

            Arguments.of(FINANCIAL_STATEMENT_FORM_E_E1_OR_E2, false, CuiDocumentsCategoriser.Party.RESPONDENT, false,
                RESPONDENT_DOCUMENTS_FORM_E),

            Arguments.of(PENSION_REPORT_EXPERT_REPORT, true, CuiDocumentsCategoriser.Party.APPLICANT, true,
                FDR_REPORTS),

            Arguments.of(PENSION_REPORT_EXPERT_REPORT, false, CuiDocumentsCategoriser.Party.APPLICANT, true,
                REPORTS),

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

            Arguments.of(CHRONOLOGY, true,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                FDR_JOINT_DOCUMENTS_CHRONOLOGY),

            Arguments.of(COMPOSITE_CASE_SUMMARY_FORM_ES1, true,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                FDR_JOINT_DOCUMENTS_ES1),

            Arguments.of(COMPOSITE_SCHEDULE_OF_ASSETS_AND_INCOME_FORM_ES2, false,
                CuiDocumentsCategoriser.Party.RESPONDENT, false,
                HEARING_DOCUMENTS_RESPONDENT_ES2),

            Arguments.of(HEARING_BUNDLE, true,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                FDR_BUNDLE),

            Arguments.of(HEARING_BUNDLE, false,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                DocumentCategory.HEARING_BUNDLE),

            Arguments.of(SECTION_25_STATEMENT, false,
                CuiDocumentsCategoriser.Party.RESPONDENT, false,
                RESPONDENT_DOCUMENTS_S25_STATEMENT),

            Arguments.of(WITNESS_STATEMENT, false,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                APPLICANT_DOCUMENTS_WITNESS_STATEMENTS),

            Arguments.of(WITHOUT_PREJUDICE_OFFERS_FOR_SETTLEMENT, false,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                null),

            Arguments.of(POINTS_OF_CLAIM_DEFENCE, true,
                CuiDocumentsCategoriser.Party.APPLICANT, true,
                null)
        );
    }
}
