package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class ApplicantFormEExhibitsHandlerTest extends CaseDocumentHandlerTest {

    ApplicantFormEExhibitsHandler applicantFormEExhibitsHandler = new ApplicantFormEExhibitsHandler();

    @Test
    public void appFormEExhibitsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Applicant - Form E", "applicant", YesOrNo.NO, YesOrNo.NO, null));

        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);


        applicantFormEExhibitsHandler.handle(uploadDocumentList, caseData);

        assertThat(caseData.getUploadCaseDocumentWrapper().getAppFormEExhibitsCollection(), hasSize(1));
    }

}