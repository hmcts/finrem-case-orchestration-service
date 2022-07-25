package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder;

import org.junit.Before;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

public class UploadApprovedOrderBaseHandlerTest {

    protected static final String SUCCESS_KEY = "successKey";
    protected static final String SUCCESS_VALUE = "successValue";

    protected CallbackRequest callbackRequest;

    @Before
    public void setUp() {
        callbackRequest = CallbackRequest.builder().caseDetails(buildCaseDetails()).build();
    }

    protected FinremCaseDetails buildCaseDetails() {
        FinremCaseData caseData = new FinremCaseData();
        return FinremCaseDetails.builder().id(123L).caseType(CaseType.CONTESTED).caseData(caseData).build();
    }
}
