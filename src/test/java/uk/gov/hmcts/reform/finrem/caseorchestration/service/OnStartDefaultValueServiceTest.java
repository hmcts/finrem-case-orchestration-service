package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_DIRECTION_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_DIRECTION_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TYPE_OF_APPLICATION_DEFAULT_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ISSUE_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.URGENT_CASE_QUESTION;

@RunWith(MockitoJUnitRunner.class)
public class OnStartDefaultValueServiceTest  extends BaseServiceTest {

    public static final String AUTH_TOKEN = "tokien:)";

    @InjectMocks
    private OnStartDefaultValueService service;

    @Mock
    private IdamService idamService;

    public OnStartDefaultValueServiceTest() {
    }

    @Test
    public void setDefaultDate() {
        FinremCallbackRequest callbackRequest = callbackRequest();
        service.defaultIssueDate(callbackRequest);
        assertNotNull(callbackRequest.getCaseDetails().getData().getIssueDate());
    }

    @Test
    public void defaultCivilPartnershipField() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        service.defaultCivilPartnershipField(callbackRequest);
        assertEquals(NO_VALUE,callbackRequest.getCaseDetails().getData().get(CIVIL_PARTNERSHIP));
    }


    @Test
    public void defaultTypeOfApplication_defaultValue() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        service.defaultTypeOfApplication(callbackRequest);
        assertEquals(TYPE_OF_APPLICATION_DEFAULT_TO,callbackRequest.getCaseDetails().getData().get(TYPE_OF_APPLICATION));
    }

    @Test
    public void defaultTypeOfApplication_userChoosen() {
        var schedule1 = "Under paragraph 1 or 2 of schedule 1 children act 1989";
        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().getData().put(TYPE_OF_APPLICATION, schedule1);
        service.defaultTypeOfApplication(callbackRequest);
        assertEquals(schedule1,callbackRequest.getCaseDetails().getData().get(TYPE_OF_APPLICATION));
    }

    @Test
    public void setDefaultConsentedJudgeName() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        when(idamService.getIdamSurname(AUTH_TOKEN)).thenReturn("test name");
        service.defaultConsentedOrderJudgeName(callbackRequest, AUTH_TOKEN);
        assertNotNull(callbackRequest.getCaseDetails().getData().get(CONSENTED_ORDER_DIRECTION_JUDGE_NAME));
    }

    @Test
    public void setDefaultContestedJudgeName() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        when(idamService.getIdamSurname(AUTH_TOKEN)).thenReturn("test name");
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

    @Test
    public void setDefaultUrgencyQuestion() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        service.defaultUrgencyQuestion(callbackRequest);
        assertNotNull(callbackRequest.getCaseDetails().getData().get(URGENT_CASE_QUESTION));
        assertEquals(NO_VALUE, callbackRequest.getCaseDetails().getData().get(URGENT_CASE_QUESTION));
    }


    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).data(caseData).build();
        return CallbackRequest.builder().eventId("SomeEventId").caseDetails(caseDetails).build();
    }

    private FinremCallbackRequest callbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.ISSUE_APPLICATION)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }
}