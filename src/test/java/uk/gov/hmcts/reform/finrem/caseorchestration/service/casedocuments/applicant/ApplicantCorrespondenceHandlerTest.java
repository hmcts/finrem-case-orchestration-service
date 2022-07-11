package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class ApplicantCorrespondenceHandlerTest extends CaseDocumentHandlerTest {

    ApplicantCorrespondenceHandler applicantCorrespondenceHandler = new ApplicantCorrespondenceHandler();

    @Test
    public void appCorrespondenceDocsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Offers", "applicant", YesOrNo.NO, YesOrNo.NO, null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Letter from Applicant", "applicant", YesOrNo.NO, YesOrNo.NO, null));

        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);

        applicantCorrespondenceHandler.handle(uploadDocumentList, caseData);

        assertThat(caseData.getUploadCaseDocumentWrapper().getAppCorrespondenceDocsCollection(), hasSize(2));
    }

}