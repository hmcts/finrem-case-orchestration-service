package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_DIRECTION_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_DIRECTION_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ISSUE_DATE;

@RunWith(MockitoJUnitRunner.class)
public class OnStartDefaultValueServiceTest  extends BaseServiceTest {

    public static final String AUTH_TOKEN = "tokien:)";

    @InjectMocks
    private final OnStartDefaultValueService service =  new OnStartDefaultValueService();

    @Mock
    private IdamService idamService;

    @Test
    public void setDefaultDate() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        service.defaultIssueDate(callbackRequest);
        assertNotNull(callbackRequest.getCaseDetails().getData().get(ISSUE_DATE));
    }

    @Test
    public void setDefaultConsentedJudgeName() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn("test name");
        service.defaultConsentedOrderJudgeName(callbackRequest, AUTH_TOKEN);
        assertNotNull(callbackRequest.getCaseDetails().getData().get(CONSENTED_ORDER_DIRECTION_JUDGE_NAME));
    }

    @Test
    public void setDefaultContestedJudgeName() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn("test name");
        service.defaultContestedOrderJudgeName(callbackRequest, AUTH_TOKEN);
        assertNotNull(callbackRequest.getCaseDetails().getData().get(CONTESTED_ORDER_APPROVED_JUDGE_NAME));
    }

    @Test
    public void setDefaultConsentedOrderDate() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        service.defaultConsentedOrderDate(callbackRequest);
        assertNotNull(callbackRequest.getCaseDetails().getData().get(CONSENTED_ORDER_DIRECTION_DATE));
    }

    @Test
    public void setDefaultContestedOrderDate() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        service.defaultContestedOrderDate(callbackRequest);
        assertNotNull(callbackRequest.getCaseDetails().getData().get(CONTESTED_ORDER_APPROVED_DATE));
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).data(caseData).build();
        return CallbackRequest.builder().eventId("SomeEventId").caseDetails(caseDetails).build();
    }
}