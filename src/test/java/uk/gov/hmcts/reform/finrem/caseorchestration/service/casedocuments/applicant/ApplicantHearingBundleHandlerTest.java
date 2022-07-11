package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class ApplicantHearingBundleHandlerTest extends CaseDocumentHandlerTest {

    ApplicantHearingBundleHandler applicantHearingBundleHandler = new ApplicantHearingBundleHandler();

    @Test
    public void appHearingBundlesFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Trial Bundle", "applicant", YesOrNo.NO, YesOrNo.NO, null));

        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);

        applicantHearingBundleHandler.handle(uploadDocumentList, caseData);

        assertThat(caseData.getUploadCaseDocumentWrapper().getAppHearingBundlesCollection(), hasSize(1));
    }
}