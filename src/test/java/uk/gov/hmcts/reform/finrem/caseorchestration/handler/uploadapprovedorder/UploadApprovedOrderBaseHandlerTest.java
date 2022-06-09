package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder;

import org.junit.Before;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONTESTED;

public class UploadApprovedOrderBaseHandlerTest {

    protected static final String SUCCESS_KEY = "successKey";
    protected static final String SUCCESS_VALUE = "successValue";

    protected CallbackRequest callbackRequest;

    protected Map<String, Object> caseData;

    @Before
    public void setUp() {
        callbackRequest = CallbackRequest.builder().caseDetails(buildCaseDetails()).build();
        caseData = new HashMap<>();
        caseData.put(SUCCESS_KEY, SUCCESS_VALUE);
    }

    protected CaseDetails buildCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();
        return CaseDetails.builder().id(Long.valueOf(123)).caseTypeId(CASE_TYPE_ID_CONTESTED).data(caseData).build();
    }
}
