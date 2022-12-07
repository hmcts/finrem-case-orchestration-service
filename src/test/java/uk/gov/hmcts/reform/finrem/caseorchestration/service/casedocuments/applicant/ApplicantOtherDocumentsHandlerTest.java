package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentCollectionsManagerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;

public class ApplicantOtherDocumentsHandlerTest extends CaseDocumentCollectionsManagerTest {

    ApplicantOtherDocumentsCollectionService applicantOtherDocumentsHandler = new ApplicantOtherDocumentsCollectionService(new ObjectMapper());


    @Test
    public void appOtherDocsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("other", "applicant", "no", "no", "Other Example"));
        uploadDocumentList.add(createContestedUploadDocumentItem("Form B", "applicant", "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Form F", "applicant", "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Care Plan", "applicant", "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Pension Plan", "applicant", "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        applicantOtherDocumentsHandler.processUploadDocumentCollection(caseData);

        assertThat(getDocumentCollection(caseData, APP_OTHER_COLLECTION), hasSize(5));
    }
}