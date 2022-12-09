package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConfidentialDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConfidentialDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class ConfidentialDocumentsCollectionServiceTest extends CaseDocumentCollectionsServiceTest {

    ConfidentialDocumentsCollectionService collectionService =
        new ConfidentialDocumentsCollectionService(evidenceManagementDeleteService);

    @Test
    public void respondentConfidentialDocumentsFiltered() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.OTHER,
            CaseDocumentParty.RESPONDENT, YesOrNo.YES, YesOrNo.NO, "Other Example"));

        caseDetails.getData().getUploadCaseDocumentWrapper().setUploadCaseDocument(screenUploadDocumentList);

        collectionService.processUploadDocumentCollection(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build(),
            screenUploadDocumentList);

        assertThat(caseData.getConfidentialDocumentsUploaded(), hasSize(1));
    }

    @Test
    public void shouldNotAddConfidentialDocumentsFiltered() {

        List<UploadConfidentialDocumentCollection> confidentialUploadedDocumentData = new ArrayList<>();
        confidentialUploadedDocumentData.add(createConfidentialUploadedDocumentDataItem());
        caseDetails.getData().setConfidentialDocumentsUploaded(confidentialUploadedDocumentData);
        caseDetails.getData().getUploadCaseDocumentWrapper().setUploadCaseDocument(screenUploadDocumentList);

        collectionService.processUploadDocumentCollection(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build(),
            screenUploadDocumentList);

        assertThat(caseData.getConfidentialDocumentsUploaded(), hasSize(1));
    }

    protected UploadConfidentialDocumentCollection createConfidentialUploadedDocumentDataItem() {
        return UploadConfidentialDocumentCollection.builder().value(
            (UploadConfidentialDocument
                .builder()
                .documentType(CaseDocumentType.OTHER)
                .documentLink(CaseDocument.builder().documentUrl("url").documentFilename("filename").build())
                .documentComment("Comment")
                .build())).build();
    }
}