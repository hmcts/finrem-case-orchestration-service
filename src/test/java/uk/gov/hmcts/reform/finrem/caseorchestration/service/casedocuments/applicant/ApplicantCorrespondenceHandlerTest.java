package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;

public class ApplicantCorrespondenceHandlerTest extends CaseDocumentHandlerTest {

    ApplicantCorrespondenceHandler applicantCorrespondenceHandler = new ApplicantCorrespondenceHandler(new ObjectMapper());

    @Test
    public void appCorrespondenceDocsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Offers", "applicant", "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Letter from Applicant", "applicant", "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        applicantCorrespondenceHandler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, APP_CORRESPONDENCE_COLLECTION), hasSize(2));
    }

}