package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class ApplicantFormsHHandlerTest extends CaseDocumentHandlerTest {

    ApplicantFormsHHandler applicantFormsHHandler = new ApplicantFormsHHandler();


    @Test
    public void appFormsHFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Form H", "applicant", YesOrNo.NO, YesOrNo.NO, null));
        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);

        applicantFormsHHandler.handle(uploadDocumentList, caseData);

        assertThat(caseData.getUploadCaseDocumentWrapper().getAppFormsHCollection(), hasSize(1));
    }
}