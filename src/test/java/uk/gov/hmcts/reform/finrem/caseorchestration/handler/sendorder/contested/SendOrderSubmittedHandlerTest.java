package uk.gov.hmcts.reform.finrem.caseorchestration.handler.sendorder.contested;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SendOrderEventPostStateOption;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.SendOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.sendorder.SendOrderCorresponder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.assertCondition;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SendOrderSubmittedHandlerTest {

    @InjectMocks
    private SendOrderSubmittedHandler underTest;
    @Mock
    private PartyService partyService;
    @Mock
    private SendOrderCorresponder sendOrderCorresponder;
    @Mock
    private CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;
    @Mock
    private CcdService ccdService;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.SEND_ORDER);
    }

    @Test
    void shouldUpdateCorrespondenceEnabledFromSelectedPartiesBeforePublishingSendCorrespondenceEvents() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();

        SendCorrespondenceEvent event1 = mock(SendCorrespondenceEvent.class);
        when(event1.getNotificationParties()).thenReturn(List.of(NotificationParty.RESPONDENT));
        when(event1.describeNotificationParties()).thenReturn("APPLICANT SOLICITOR");
        List<SendCorrespondenceEvent> events = List.of(event1);
        when(sendOrderCorresponder.buildCorrespondenceEventIfNeeded(callbackRequest.getCaseDetails(),
            AUTH_TOKEN)).thenReturn(events);

        var response = underTest.handle(callbackRequest, AUTH_TOKEN);

        InOrder inOrder = Mockito.inOrder(partyService, correspondenceEventAuditOrchestrationService);
        inOrder.verify(partyService)
            .updateCorrespondenceEnabledFromSelectedParties(callbackRequest.getCaseDetails());
        inOrder.verify(correspondenceEventAuditOrchestrationService)
            .publishEvent(eq(event1), eq("Send order correspondence to party: APPLICANT SOLICITOR for the send order event"),
                any(Runnable.class));

        assertAll(
            () -> assertNull(response.getConfirmationHeader()),
            () -> assertNull(response.getConfirmationBody())
        );
    }

    @Test
    void givenPublishError_whenHandled_thenPopulateErrors() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();

        SendCorrespondenceEvent event1 = mock(SendCorrespondenceEvent.class);
        when(event1.getNotificationParties()).thenReturn(List.of(NotificationParty.RESPONDENT));
        when(event1.describeNotificationParties()).thenReturn("APPLICANT SOLICITOR");
        List<SendCorrespondenceEvent> events = List.of(event1);
        when(sendOrderCorresponder.buildCorrespondenceEventIfNeeded(callbackRequest.getCaseDetails(),
            AUTH_TOKEN)).thenReturn(events);
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(2)).run();
            return null;
        }).when(correspondenceEventAuditOrchestrationService)
            .publishEvent(eq(event1), eq("Send order correspondence to party: APPLICANT SOLICITOR for the send order event"),
                any(Runnable.class));

        var response = underTest.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> assertThat(response.getConfirmationHeader()).contains("Send order event submitted with errors"),
            () -> assertCondition(response.getConfirmationBody(),
                "Unable to deliver send order correspondence to APPLICANT SOLICITOR. Please send it manually.",
                true)
        );
    }

    @ParameterizedTest
    @EnumSource(value = SendOrderEventPostStateOption.class, names = {"PREPARE_FOR_HEARING", "CLOSE"})
    void givenSendOrderEventPostStateOptionChosen_whenHandled_shouldInvokeCcdService(
        SendOrderEventPostStateOption option
    ) {
        SendOrderWrapper sendOrderWrapper = mock(SendOrderWrapper.class);
        when(sendOrderWrapper.getSendOrderPostStateOption()).thenReturn(option);
        FinremCaseData caseData = FinremCaseData.builder()
            .sendOrderWrapper(sendOrderWrapper)
            .build();
        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        when(caseDetails.getData()).thenReturn(caseData);
        when(caseDetails.getCaseType()).thenReturn(CaseType.CONTESTED);
        when(caseDetails.getId()).thenReturn(CASE_ID_IN_LONG);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(caseDetails);
        underTest.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService).executeCcdEventOnCase(AUTH_TOKEN, CASE_ID,
            CaseType.CONTESTED.getCcdType(), option.getEventToTrigger().getCcdType());
    }

    @ParameterizedTest
    @EnumSource(value = SendOrderEventPostStateOption.class, names = {"ORDER_SENT"})
    @NullSource
    void givenOrderSentChosenOrNull_whenHandled_shouldInvokeCcdService(
        SendOrderEventPostStateOption option
    ) {
        SendOrderWrapper sendOrderWrapper = mock(SendOrderWrapper.class);
        when(sendOrderWrapper.getSendOrderPostStateOption()).thenReturn(option);
        FinremCaseData caseData = FinremCaseData.builder()
            .sendOrderWrapper(sendOrderWrapper)
            .build();
        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        when(caseDetails.getData()).thenReturn(caseData);
        when(caseDetails.getCaseType()).thenReturn(CaseType.CONTESTED);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(caseDetails);
        underTest.handle(callbackRequest, AUTH_TOKEN);

        verifyNoInteractions(ccdService);
    }
}
