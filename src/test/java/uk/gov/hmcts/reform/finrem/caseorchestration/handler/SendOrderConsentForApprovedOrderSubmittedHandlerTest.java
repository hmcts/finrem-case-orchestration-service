package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SendOrderConsentForApprovedOrderSubmittedHandlerTest {

    @InjectMocks
    private SendOrderConsentForApprovedOrderSubmittedHandler underTest;
    @Mock
    private ConsentOrderApprovedCorresponder consentOrderApprovedCorresponder;
    @Mock
    private CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.CONSENT_SEND_ORDER_FOR_APPROVED_ORDER);
    }

    @Test
    void givenAllEventsPublished_whenHandled_thenNoErrors() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        SendCorrespondenceEvent applicantEvent = event("applicant");
        SendCorrespondenceEvent respondentEvent = event("respondent");
        when(consentOrderApprovedCorresponder.buildCorrespondenceEvents(callbackRequest.getCaseDetails(), AUTH_TOKEN))
            .thenReturn(List.of(applicantEvent, respondentEvent));
        when(correspondenceEventAuditOrchestrationService.publishEvent(applicantEvent, actionName("applicant"))).thenReturn(true);
        when(correspondenceEventAuditOrchestrationService.publishEvent(respondentEvent, actionName("respondent"))).thenReturn(true);

        var response = underTest.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> assertNull(response.getConfirmationHeader()),
            () -> assertNull(response.getConfirmationBody())
        );
    }

    @Test
    void givenOneEventFailsToPublish_whenHandled_thenErrorShownForThatParty() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        SendCorrespondenceEvent applicantEvent = event("applicant");
        SendCorrespondenceEvent respondentEvent = event("respondent");
        when(consentOrderApprovedCorresponder.buildCorrespondenceEvents(callbackRequest.getCaseDetails(), AUTH_TOKEN))
            .thenReturn(List.of(applicantEvent, respondentEvent));
        when(correspondenceEventAuditOrchestrationService.publishEvent(applicantEvent, actionName("applicant"))).thenReturn(true);
        when(correspondenceEventAuditOrchestrationService.publishEvent(respondentEvent, actionName("respondent"))).thenReturn(false);

        var response = underTest.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> assertThat(response.getConfirmationHeader()).contains("Send order event submitted with errors"),
            () -> assertThat(response.getConfirmationBody())
                .contains("Unable to deliver send order correspondence to respondent. Please send it manually.")
                .doesNotContain("correspondence to applicant")
        );
    }

    @Test
    void givenNoEvents_whenHandled_thenNothingPublished() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        when(consentOrderApprovedCorresponder.buildCorrespondenceEvents(callbackRequest.getCaseDetails(), AUTH_TOKEN))
            .thenReturn(List.of());

        var response = underTest.handle(callbackRequest, AUTH_TOKEN);

        verifyNoInteractions(correspondenceEventAuditOrchestrationService);
        assertNull(response.getConfirmationHeader());
    }

    private SendCorrespondenceEvent event(String party) {
        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(event.describeNotificationParties()).thenReturn(party);
        return event;
    }

    private String actionName(String party) {
        return "Send consent order correspondence to party: %s for the send order event".formatted(party);
    }
}
