package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentSendOrderCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SendOrderConsentForNotApprovedOrderSubmittedHandlerTest {

    @InjectMocks
    private SendOrderConsentForNotApprovedOrderSubmittedHandler handler;

    @Mock
    private CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrcService;

    @Mock
    private ConsentSendOrderCorresponder consentSendOrderCorresponder;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.SEND_ORDER);
    }

    @Test
    void givenEvents_whenHandle_thenEachEventIsPublished() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).data(new FinremCaseData()).build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(caseDetails).build();
        SendCorrespondenceEvent applicantEvent = SendCorrespondenceEvent.builder().notificationParties(List.of(APPLICANT)).build();
        SendCorrespondenceEvent respondentEvent = SendCorrespondenceEvent.builder().notificationParties(List.of(RESPONDENT)).build();

        when(consentSendOrderCorresponder.buildNotApprovedOrderEvents(caseDetails, AUTH_TOKEN))
            .thenReturn(List.of(applicantEvent, respondentEvent));
        when(correspondenceEventAuditOrcService.publishEvent(eq(applicantEvent), anyString())).thenReturn(true);
        when(correspondenceEventAuditOrcService.publishEvent(eq(respondentEvent), anyString())).thenReturn(true);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(correspondenceEventAuditOrcService).publishEvent(eq(applicantEvent), anyString());
        verify(correspondenceEventAuditOrcService).publishEvent(eq(respondentEvent), anyString());
    }

    @Test
    void givenEventFailsToPublish_whenHandle_thenReturnsErrorConfirmation() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).data(new FinremCaseData()).build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(caseDetails).build();
        SendCorrespondenceEvent applicantEvent = SendCorrespondenceEvent.builder().notificationParties(List.of(APPLICANT)).build();

        when(consentSendOrderCorresponder.buildNotApprovedOrderEvents(caseDetails, AUTH_TOKEN))
            .thenReturn(List.of(applicantEvent));
        when(correspondenceEventAuditOrcService.publishEvent(eq(applicantEvent), anyString())).thenReturn(false);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getConfirmationHeader()).contains("Send order event submitted with errors");
        assertThat(response.getConfirmationBody()).contains("Failed to send correspondence to");
    }
}
