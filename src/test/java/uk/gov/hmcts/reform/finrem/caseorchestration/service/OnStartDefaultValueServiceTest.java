package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TYPE_OF_APPLICATION_DEFAULT_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.URGENT_CASE_QUESTION;

@ExtendWith(MockitoExtension.class)
class OnStartDefaultValueServiceTest {

    private static final LocalDate FIXED_DATE = LocalDate.now();

    @InjectMocks
    private OnStartDefaultValueService service;

    @Mock
    private IdamService idamService;

    @Test
    void setDefaultDate() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        service.defaultIssueDate(callbackRequest);
        assertNotNull(callbackRequest.getCaseDetails().getData().getIssueDate());
    }

    @Test
    void defaultCivilPartnershipField() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        service.defaultCivilPartnershipField(callbackRequest);
        assertEquals(NO_VALUE, callbackRequest.getCaseDetails().getData().get(CIVIL_PARTNERSHIP));
    }

    @Test
    void defaultCivilPartnership_defaultValue_finremRequest() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        service.defaultCivilPartnershipField(callbackRequest);
        assertEquals(NO_VALUE, callbackRequest.getCaseDetails().getData().getCivilPartnership().getYesOrNo());
    }

    @Test
    void defaultCivilPartnership_userValue_finremRequest() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        callbackRequest.getCaseDetails().getData().setCivilPartnership(YesOrNo.YES);
        service.defaultCivilPartnershipField(callbackRequest);
        assertEquals(YES_VALUE, callbackRequest.getCaseDetails().getData().getCivilPartnership().getYesOrNo());
    }

    @Test
    void defaultTypeOfApplication_defaultValue() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        service.defaultTypeOfApplication(callbackRequest);
        assertEquals(TYPE_OF_APPLICATION_DEFAULT_TO,callbackRequest.getCaseDetails().getData().get(TYPE_OF_APPLICATION));
    }

    @Test
    void defaultTypeOfApplication_userValue() {
        var schedule1 = "Under paragraph 1 or 2 of schedule 1 children act 1989";
        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().getData().put(TYPE_OF_APPLICATION, schedule1);
        service.defaultTypeOfApplication(callbackRequest);
        assertEquals(schedule1,callbackRequest.getCaseDetails().getData().get(TYPE_OF_APPLICATION));
    }

    @Test
    void defaultTypeOfApplication_defaultValue_finremRequest() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        service.defaultTypeOfApplication(callbackRequest);
        assertEquals(Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS.getText(),
            callbackRequest.getCaseDetails().getData().getScheduleOneWrapper().getTypeOfApplication().getText());
    }

    @Test
    void defaultTypeOfApplication_userValue_finremRequest() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        callbackRequest.getCaseDetails().getData().getScheduleOneWrapper().setTypeOfApplication(
            Schedule1OrMatrimonialAndCpList.SCHEDULE_1_CHILDREN_ACT_1989);
        service.defaultTypeOfApplication(callbackRequest);
        assertEquals(Schedule1OrMatrimonialAndCpList.SCHEDULE_1_CHILDREN_ACT_1989.getText(),
            callbackRequest.getCaseDetails().getData().getScheduleOneWrapper().getTypeOfApplication().getText());
    }

    @Test
    void setDefaultContestedJudgeName() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        when(idamService.getIdamSurname(AUTH_TOKEN)).thenReturn("test name");
        service.defaultContestedOrderJudgeName(callbackRequest, AUTH_TOKEN);
        assertNotNull(callbackRequest.getCaseDetails().getData().get(CONTESTED_ORDER_APPROVED_JUDGE_NAME));
    }

    @Test
    void setDefaultContestedOrderDate() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        service.defaultContestedOrderDate(callbackRequest);
        assertNotNull(callbackRequest.getCaseDetails().getData().get(CONTESTED_ORDER_APPROVED_DATE));
    }

    @Test
    void setDefaultUrgencyQuestion() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        service.defaultUrgencyQuestion(callbackRequest);
        assertNotNull(callbackRequest.getCaseDetails().getData().get(URGENT_CASE_QUESTION));
        assertEquals(NO_VALUE, callbackRequest.getCaseDetails().getData().get(URGENT_CASE_QUESTION));
    }

    @Test
    void defaultUrgencyQuestion_defaultValue_finremRequest() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        service.defaultUrgencyQuestion(callbackRequest);
        assertEquals(YesOrNo.NO, callbackRequest.getCaseDetails().getData().getPromptForUrgentCaseQuestion());
    }

    @Test
    void defaultUrgencyQuestion_userValue_finremRequest() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        callbackRequest.getCaseDetails().getData().setPromptForUrgentCaseQuestion(YesOrNo.YES);
        service.defaultUrgencyQuestion(callbackRequest);
        assertEquals(YesOrNo.YES, callbackRequest.getCaseDetails().getData().getPromptForUrgentCaseQuestion());
    }

    @Test
    void testDefaultConsentedOrderJudgeName() {
        when(idamService.getIdamSurname(AUTH_TOKEN)).thenReturn("test name");

        FinremCaseData caseData = mock(FinremCaseData.class);
        service.defaultConsentedOrderJudgeName(caseData, AUTH_TOKEN);

        assertAll(
            () -> verify(caseData).setOrderDirectionJudgeName("test name"),
            () -> verify(idamService).getIdamSurname(AUTH_TOKEN)
        );
    }

    @Test
    void tsetDefaultConsentedOrderDate() {
        FinremCaseData caseData = mock(FinremCaseData.class);

        try (MockedStatic<LocalDate> mockedStatic = Mockito.mockStatic(LocalDate.class)) {
            mockedStatic.when(LocalDate::now).thenReturn(FIXED_DATE);

            service.defaultConsentedOrderDate(caseData);
            verify(caseData).setOrderDirectionDate(FIXED_DATE);
        }
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID_IN_LONG).data(caseData).build();
        return CallbackRequest.builder().eventId("SomeEventId").caseDetails(caseDetails).build();
    }
}
