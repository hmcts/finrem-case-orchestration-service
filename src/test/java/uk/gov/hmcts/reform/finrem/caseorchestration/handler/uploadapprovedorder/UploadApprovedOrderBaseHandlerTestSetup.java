package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder;

import org.junit.Before;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

public class UploadApprovedOrderBaseHandlerTestSetup {

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
        CaseDetails caseDetails = CaseDetails.builder().id(123L).caseTypeId(CaseType.CONTESTED.getCcdType()).build();
        caseDetails.setData(caseData);
        return caseDetails;
    }

    protected FinremCallbackRequest buildFinremCallbackRequest(EventType eventType) {
        return FinremCallbackRequest
            .builder()
            .eventType(eventType)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }
}
