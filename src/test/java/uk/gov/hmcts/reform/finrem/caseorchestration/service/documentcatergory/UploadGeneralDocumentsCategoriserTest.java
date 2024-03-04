package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.BaseHandlerTestSetup;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadGeneralDocumentsCategoriserTest extends BaseHandlerTestSetup {
    private UploadGeneralDocumentsCategoriser uploadGeneralDocumentsCategoriser;

    @Mock
    FeatureToggleService featureToggleService;
    @Mock
    DocumentHelper documentHelper;

    @BeforeEach
    public void setUp() {
        uploadGeneralDocumentsCategoriser = new UploadGeneralDocumentsCategoriser(featureToggleService, documentHelper);
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
    }

    @Test
    void testCategorizeDocuments() {
        FinremCaseData finremCaseData = buildFinremCaseData();
        uploadGeneralDocumentsCategoriser.categorise(finremCaseData);

        List<UploadGeneralDocumentCollection> documents = finremCaseData.getUploadGeneralDocuments();

        assertEquals(DocumentCategory.CORRESPONDENCE_APPLICANT.getDocumentCategoryId(),
            documents.get(0).getValue().getDocumentLink().getCategoryId());
        assertEquals(DocumentCategory.CORRESPONDENCE_APPLICANT.getDocumentCategoryId(),
            documents.get(1).getValue().getDocumentLink().getCategoryId());

        assertEquals(DocumentCategory.CORRESPONDENCE_RESPONDENT.getDocumentCategoryId(),
            documents.get(2).getValue().getDocumentLink().getCategoryId());
        assertEquals(DocumentCategory.CORRESPONDENCE_RESPONDENT.getDocumentCategoryId(),
            documents.get(3).getValue().getDocumentLink().getCategoryId());

        assertEquals(DocumentCategory.CORRESPONDENCE_RESPONDENT.getDocumentCategoryId(),
            documents.get(4).getValue().getDocumentLink().getCategoryId());

        assertNull(documents.get(5).getValue().getDocumentLink().getCategoryId());

    }

    @Test
    void testCategorizeDocumentsWhenCollectionIsnull() {
        FinremCaseData finremCaseData = FinremCaseData.builder().uploadGeneralDocuments(List.of(UploadGeneralDocumentCollection.builder()
            .build())).build();
        uploadGeneralDocumentsCategoriser.categorise(finremCaseData);
        assertEquals(1, finremCaseData.getUploadGeneralDocuments().size());
        assertNull(finremCaseData.getUploadGeneralDocuments().get(0).getValue());

    }

    protected FinremCaseData buildFinremCaseData() {

        UploadGeneralDocumentCollection applicantDocument = UploadGeneralDocumentCollection.builder()
            .value(UploadGeneralDocument.builder().documentLink(CaseDocument.builder().build())
                .documentType(UploadGeneralDocumentType.LETTER_EMAIL_FROM_APPLICANT).build())
            .build();

        UploadGeneralDocumentCollection applicantSolicitorDocument = UploadGeneralDocumentCollection.builder()
            .value(UploadGeneralDocument.builder().documentLink(CaseDocument.builder().build())
                .documentType(UploadGeneralDocumentType.LETTER_EMAIL_FROM_APPLICANT_SOLICITOR).build())
            .build();
        UploadGeneralDocumentCollection respondentDocument = UploadGeneralDocumentCollection.builder()
            .value(UploadGeneralDocument.builder().documentLink(CaseDocument.builder().build())
                .documentType(UploadGeneralDocumentType.LETTER_EMAIL_FROM_RESPONDENT).build())
            .build();

        UploadGeneralDocumentCollection respondentSolicitorDocument = UploadGeneralDocumentCollection.builder()
            .value(UploadGeneralDocument.builder().documentLink(CaseDocument.builder().build())
                .documentType(UploadGeneralDocumentType.LETTER_EMAIL_FROM_RESPONDENT_SOLICITOR).build())
            .build();

        UploadGeneralDocumentCollection respondentContestedDocument = UploadGeneralDocumentCollection.builder()
            .value(UploadGeneralDocument.builder().documentLink(CaseDocument.builder().build())
                .documentType(UploadGeneralDocumentType.LETTER_EMAIL_FROM_RESPONDENT_CONTESTED).build())
            .build();

        UploadGeneralDocumentCollection drafOrder = UploadGeneralDocumentCollection.builder()
            .value(UploadGeneralDocument.builder().documentLink(CaseDocument.builder().build())
                .documentType(UploadGeneralDocumentType.DRAFT_ORDER).build())
            .build();

        return FinremCaseData.builder().uploadGeneralDocuments(List.of(applicantDocument, applicantSolicitorDocument,
            respondentDocument, respondentSolicitorDocument, respondentContestedDocument, drafOrder)).build();

    }
}
