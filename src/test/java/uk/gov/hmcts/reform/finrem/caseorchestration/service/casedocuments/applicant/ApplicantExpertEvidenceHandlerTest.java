package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class ApplicantExpertEvidenceHandlerTest extends CaseDocumentHandlerTest {

    ApplicantExpertEvidenceHandler applicantExpertEvidenceHandler = new ApplicantExpertEvidenceHandler();

    @Test
    public void appExpertEvidenceFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Valuation Report", "applicant", YesOrNo.NO, YesOrNo.NO, null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Expert Evidence", "applicant", YesOrNo.NO, YesOrNo.NO, null));

        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);

        applicantExpertEvidenceHandler.handle(uploadDocumentList, caseData);

        assertThat(caseData.getUploadCaseDocumentWrapper().getAppExpertEvidenceCollection(), hasSize(2));
    }
}