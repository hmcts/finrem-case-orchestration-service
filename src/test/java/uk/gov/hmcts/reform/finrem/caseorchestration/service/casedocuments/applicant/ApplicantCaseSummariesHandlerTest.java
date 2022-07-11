package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class ApplicantCaseSummariesHandlerTest extends CaseDocumentHandlerTest {

    private FinremCaseDetails caseDetails;
    private FinremCaseData caseData;
    private final List<UploadCaseDocumentCollection> uploadDocumentList = new ArrayList<>();

    ApplicantCaseSummariesHandler applicantCaseSummariesHandler = new ApplicantCaseSummariesHandler();

    @Before
    public void setUp() {
        caseDetails = buildFinremCaseDetails();
        caseData = caseDetails.getCaseData();
    }

    @Test
    public void appCaseSummariesFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Position Statement", "applicant", YesOrNo.NO, YesOrNo.YES, null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Skeleton Argument", "applicant", YesOrNo.NO, YesOrNo.NO, null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Case Summary", "applicant", YesOrNo.NO, YesOrNo.NO, null));

        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);

        applicantCaseSummariesHandler.handle(uploadDocumentList, caseData);

        assertThat(caseData.getUploadCaseDocumentWrapper().getAppCaseSummariesCollection(), hasSize(3));
    }

}