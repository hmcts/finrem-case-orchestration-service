package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class ApplicantOtherDocumentsHandlerTest extends CaseDocumentHandlerTest {

    ApplicantOtherDocumentsHandler applicantOtherDocumentsHandler = new ApplicantOtherDocumentsHandler();


    @Test
    public void appOtherDocsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("other", "applicant", YesOrNo.NO, YesOrNo.NO, "Other Example"));
        uploadDocumentList.add(createContestedUploadDocumentItem("Form B", "applicant", YesOrNo.NO, YesOrNo.NO, null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Form F", "applicant", YesOrNo.NO, YesOrNo.NO, null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Care Plan", "applicant", YesOrNo.NO, YesOrNo.NO, null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Pension Plan", "applicant", YesOrNo.NO, YesOrNo.NO, null));

        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);

        applicantOtherDocumentsHandler.handle(uploadDocumentList, caseData);

        assertThat(caseData.getUploadCaseDocumentWrapper().getAppOtherCollection(), hasSize(5));
    }
}