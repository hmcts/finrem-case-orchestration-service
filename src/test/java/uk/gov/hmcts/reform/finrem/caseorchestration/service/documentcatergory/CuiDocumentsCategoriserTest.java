package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    void shouldCategoriseApplicantNonFdrFormE() {
        CitizenUploadDocument doc =
            buildDocument(CitizenUploadDocumentType.FINANCIAL_STATEMENT_FORM_E_E1_OR_E2, false);

        CuiDocumentsCategoriser categoriser =
            new CuiDocumentsCategoriser(featureToggleService, CuiDocumentsCategoriser.Party.APPLICANT);

        FinremCaseData caseData =
            buildCaseData(List.of(wrap(doc)), true);

        categoriser.categorise(caseData);

        assertThat(doc.getDocumentLink().getCategoryId())
            .isEqualTo(DocumentCategory.APPLICANT_DOCUMENTS_FORM_E.getDocumentCategoryId());
    }

    @Test
    void shouldCategoriseRespondentNonFdrFormE() {
        CitizenUploadDocument doc =
            buildDocument(CitizenUploadDocumentType.FINANCIAL_STATEMENT_FORM_E_E1_OR_E2, false);

        CuiDocumentsCategoriser categoriser =
            new CuiDocumentsCategoriser(featureToggleService, CuiDocumentsCategoriser.Party.RESPONDENT);

        FinremCaseData caseData =
            buildCaseData(List.of(wrap(doc)), false);

        categoriser.categorise(caseData);

        assertThat(doc.getDocumentLink().getCategoryId())
            .isEqualTo(DocumentCategory.RESPONDENT_DOCUMENTS_FORM_E.getDocumentCategoryId());
    }

    @Test
    void shouldCategoriseFdrReportCorrectly() {
        CitizenUploadDocument doc =
            buildDocument(CitizenUploadDocumentType.PENSION_REPORT_EXPERT_REPORT, true);

        CuiDocumentsCategoriser categoriser =
            new CuiDocumentsCategoriser(featureToggleService, CuiDocumentsCategoriser.Party.APPLICANT);

        FinremCaseData caseData =
            buildCaseData(List.of(wrap(doc)), true);

        categoriser.categorise(caseData);

        assertThat(doc.getDocumentLink().getCategoryId())
            .isEqualTo(DocumentCategory.FDR_REPORTS.getDocumentCategoryId());
    }

    @Test
    void shouldCategoriseNonFdrReportCorrectly() {
        CitizenUploadDocument doc =
            buildDocument(CitizenUploadDocumentType.PENSION_REPORT_EXPERT_REPORT, false);

        CuiDocumentsCategoriser categoriser =
            new CuiDocumentsCategoriser(featureToggleService, CuiDocumentsCategoriser.Party.APPLICANT);

        FinremCaseData caseData =
            buildCaseData(List.of(wrap(doc)), true);

        categoriser.categorise(caseData);

        assertThat(doc.getDocumentLink().getCategoryId())
            .isEqualTo(DocumentCategory.REPORTS.getDocumentCategoryId());
    }

    @Test
    void shouldReturnNullCategoryForFdrPointsOfClaimDefence() {
        CitizenUploadDocument doc =
            buildDocument(CitizenUploadDocumentType.POINTS_OF_CLAIM_DEFENCE, true);

        CuiDocumentsCategoriser categoriser =
            new CuiDocumentsCategoriser(featureToggleService, CuiDocumentsCategoriser.Party.APPLICANT);

        FinremCaseData caseData =
            buildCaseData(List.of(wrap(doc)), true);

        categoriser.categorise(caseData);

        assertThat(doc.getDocumentLink().getCategoryId()).isNull();
    }

    @Test
    void shouldHandleEmptyDocumentsGracefully() {
        CuiDocumentsCategoriser categoriser =
            new CuiDocumentsCategoriser(featureToggleService, CuiDocumentsCategoriser.Party.APPLICANT);

        FinremCaseData caseData =
            buildCaseData(null, true);

        assertThatCode(() -> categoriser.categorise(caseData))
            .doesNotThrowAnyException();

    }

    @Test
    void shouldIgnoreInvalidDocument() {
        CitizenUploadDocument doc = new CitizenUploadDocument();

        CuiDocumentsCategoriser categoriser =
            new CuiDocumentsCategoriser(featureToggleService, CuiDocumentsCategoriser.Party.APPLICANT);

        FinremCaseData caseData =
            buildCaseData(List.of(wrap(doc)), true);

        categoriser.categorise(caseData);

        assertThat(doc.getDocumentLink()).isNull();
    }
}
