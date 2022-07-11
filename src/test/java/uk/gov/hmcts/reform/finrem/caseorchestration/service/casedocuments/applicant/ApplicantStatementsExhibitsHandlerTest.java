package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class ApplicantStatementsExhibitsHandlerTest extends CaseDocumentHandlerTest {

    ApplicantStatementsExhibitsHandler applicantStatementsExhibitsHandler = new ApplicantStatementsExhibitsHandler();


    @Test
    public void appStatementsExhibitsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Statement/Affidavit", "applicant", YesOrNo.NO, YesOrNo.NO, null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Witness Statement/Affidavit", "applicant", YesOrNo.NO, YesOrNo.NO, null));

        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);

        applicantStatementsExhibitsHandler.handle(uploadDocumentList, caseData);

        assertThat(caseData.getUploadCaseDocumentWrapper().getAppStatementsExhibitsCollection(), hasSize(2));
    }
}