package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder;

import org.junit.Before;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.CaseType;

import java.util.HashMap;
import java.util.Map;

public class UploadApprovedOrderBaseHandlerTest {

    protected static final String SUCCESS_KEY = "successKey";
    protected static final String SUCCESS_VALUE = "successValue";

    protected CallbackRequest callbackRequest;

    protected Map<String, Object> caseData;

    @Before
    public void setUp() {
        callbackRequest =
            CallbackRequest.builder().caseDetails(buildCaseDetails()).build();
        caseData = new HashMap<>();
        caseData.put(SUCCESS_KEY, SUCCESS_VALUE);
    }

    protected CaseDetails buildCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(Long.valueOf(123)).caseTypeId(CaseType.CONTESTED.getCcdType()).build();
        caseDetails.setData(caseData);
        return caseDetails;
    }
}
