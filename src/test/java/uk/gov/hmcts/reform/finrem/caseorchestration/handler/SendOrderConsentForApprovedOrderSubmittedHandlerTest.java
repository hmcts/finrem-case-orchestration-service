package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentSendOrderCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SendOrderConsentForApprovedOrderSubmittedHandlerTest {

    private static final String APPLICANT = "applicant";
    private static final String RESPONDENT = "respondent";

    @InjectMocks
    private SendOrderConsentForApprovedOrderSubmittedHandler underTest;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private EvidenceManagementDeleteService evidenceManagementDeleteService;
    @Mock
    private RetryExecutor retryExecutor;
    @Mock
    private CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrcService;
    @Mock
    private ConsentSendOrderCorresponder consentSendOrderCorresponder;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.CONSENT_SEND_ORDER_FOR_APPROVED_ORDER);
    }

    @Test
    void givenAllEventsPublished_whenHandled_thenNoErrors() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        SendCorrespondenceEvent applicantEvent = event(APPLICANT);
        SendCorrespondenceEvent respondentEvent = event(RESPONDENT);
        stubEvents(callbackRequest, List.of(applicantEvent, respondentEvent));
        when(correspondenceEventAuditOrcService.publishEvent(applicantEvent, actionName(APPLICANT))).thenReturn(true);
        when(correspondenceEventAuditOrcService.publishEvent(respondentEvent, actionName(RESPONDENT))).thenReturn(true);

        var response = underTest.handle(callbackRequest, AUTH_TOKEN);

        verify(correspondenceEventAuditOrcService).publishEvent(applicantEvent, actionName(APPLICANT));
        verify(correspondenceEventAuditOrcService).publishEvent(respondentEvent, actionName(RESPONDENT));
        verifyNoMoreInteractions(correspondenceEventAuditOrcService);
        assertAll(
            () -> assertNull(response.getConfirmationHeader()),
            () -> assertNull(response.getConfirmationBody())
        );
    }

    @Test
    void givenRespondentEventFailsToPublish_whenHandled_thenErrorShownForRespondentOnly() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        SendCorrespondenceEvent applicantEvent = event(APPLICANT);
        SendCorrespondenceEvent respondentEvent = event(RESPONDENT);
        stubEvents(callbackRequest, List.of(applicantEvent, respondentEvent));
        when(correspondenceEventAuditOrcService.publishEvent(applicantEvent, actionName(APPLICANT))).thenReturn(true);
        when(correspondenceEventAuditOrcService.publishEvent(respondentEvent, actionName(RESPONDENT))).thenReturn(false);

        var response = underTest.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> assertThat(response.getConfirmationHeader()).contains("Send order event submitted with errors"),
            () -> assertThat(response.getConfirmationBody())
                .contains(errorMessage(RESPONDENT))
                .doesNotContain(errorMessage(APPLICANT))
        );
    }

    @Test
    void givenBothEventsFailToPublish_whenHandled_thenErrorShownForBothParties() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        SendCorrespondenceEvent applicantEvent = event(APPLICANT);
        SendCorrespondenceEvent respondentEvent = event(RESPONDENT);
        stubEvents(callbackRequest, List.of(applicantEvent, respondentEvent));
        when(correspondenceEventAuditOrcService.publishEvent(applicantEvent, actionName(APPLICANT))).thenReturn(false);
        when(correspondenceEventAuditOrcService.publishEvent(respondentEvent, actionName(RESPONDENT))).thenReturn(false);

        var response = underTest.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> assertThat(response.getConfirmationHeader()).contains("Send order event submitted with errors"),
            () -> assertThat(response.getConfirmationBody())
                .contains(errorMessage(APPLICANT))
                .contains(errorMessage(RESPONDENT))
        );
    }

    @Test
    void givenNoEvents_whenHandled_thenNothingPublished() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        stubEvents(callbackRequest, List.of());

        var response = underTest.handle(callbackRequest, AUTH_TOKEN);

        verifyNoInteractions(correspondenceEventAuditOrcService);
        assertAll(
            () -> assertNull(response.getConfirmationHeader()),
            () -> assertNull(response.getConfirmationBody())
        );
    }

    private void stubEvents(FinremCallbackRequest callbackRequest, List<SendCorrespondenceEvent> events) {
        when(consentSendOrderCorresponder.buildApprovedOrderEvents(
            callbackRequest.getCaseDetails(), callbackRequest.getCaseDetailsBefore(), AUTH_TOKEN))
            .thenReturn(events);
    }

    private SendCorrespondenceEvent event(String party) {
        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(event.describeNotificationParties()).thenReturn(party);
        return event;
    }

    private String actionName(String party) {
        return "Send consent order correspondence to " + party;
    }

    private String errorMessage(String party) {
        return "Failed to send correspondence to " + party;
    }
}