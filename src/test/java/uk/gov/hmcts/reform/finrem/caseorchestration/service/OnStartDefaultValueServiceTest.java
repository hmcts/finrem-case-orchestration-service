package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ISSUE_DATE;


public class OnStartDefaultValueServiceTest  extends BaseServiceTest {

    private final OnStartDefaultValueService service =  new OnStartDefaultValueService();

    @Test
    public void setDefaultDate() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        service.defaultIssueDate(callbackRequest);
        assertNotNull(callbackRequest.getCaseDetails().getData().get(ISSUE_DATE));
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).data(caseData).build();
        return CallbackRequest.builder().eventId("SomeEventId").caseDetails(caseDetails).build();
    }
}