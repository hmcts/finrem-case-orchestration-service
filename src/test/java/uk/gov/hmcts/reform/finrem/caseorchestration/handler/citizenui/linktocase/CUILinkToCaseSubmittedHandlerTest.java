package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.linktocase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.*;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen.SignInConfirmationCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTIFICATION_EVENT_ID;

@ExtendWith(MockitoExtension.class)
class CUILinkToCaseSubmittedHandlerTest {

    private static final String TASK_DESCRIPTION =
        "Send sign in confirmation email: [APPLICANT]";

    @Mock
    private EvidenceManagementDeleteService evidenceManagementDeleteService;

    @Mock
    private RetryExecutor retryExecutor;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Mock
    private SignInConfirmationCorresponder signInConfirmationCorresponder;

    @Mock
    private CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

    private TestHandler handler;

    @BeforeEach
    void setUp() {
        handler = new TestHandler(
            finremCaseDetailsMapper,
            evidenceManagementDeleteService,
            retryExecutor,
            signInConfirmationCorresponder,
            correspondenceEventAuditOrchestrationService
        );
    }

    @Test
    void shouldPublishNotificationAndReconcileAuditsWhenPublishSucceeds() {
        FinremCaseDetails caseDetails = caseDetails();
        FinremCallbackRequest callbackRequest =
            callbackRequest(caseDetails, EventType.LINK_APPLICANT_TO_CASE);

        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails)
            .build();

        when(signInConfirmationCorresponder.buildCorrespondenceEvent(
            caseDetails,
            AUTH_TOKEN,
            NotificationParty.CITIZEN_APPLICANT
        )).thenReturn(event);

        when(correspondenceEventAuditOrchestrationService.publishEvent(
            event,
            TASK_DESCRIPTION
        )).thenReturn(true);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(signInConfirmationCorresponder)
            .buildCorrespondenceEvent(caseDetails, AUTH_TOKEN, NotificationParty.CITIZEN_APPLICANT);
        verify(correspondenceEventAuditOrchestrationService)
            .publishEvent(event, TASK_DESCRIPTION);
        verify(correspondenceEventAuditOrchestrationService)
            .reconcileAndPersistAudits(
                caseDetails,
                event,
                "markPendingNotificationsAsSent"
            );

        assertThat(event.getEventId())
            .isEqualTo(EventType.LINK_APPLICANT_TO_CASE.getCcdType());
        assertThat(event.getNotificationTrackerId())
            .isEqualTo(NOTIFICATION_EVENT_ID);
    }

    @Test
    void shouldNotReconcileAuditsWhenPublishFails() {
        FinremCaseDetails caseDetails = caseDetails();
        FinremCallbackRequest callbackRequest = callbackRequest(caseDetails, EventType.LINK_APPLICANT_TO_CASE);

        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails)
            .build();

        when(signInConfirmationCorresponder.buildCorrespondenceEvent(caseDetails,
            AUTH_TOKEN,
            NotificationParty.CITIZEN_APPLICANT
        )).thenReturn(event);

        when(correspondenceEventAuditOrchestrationService.publishEvent(
            event,
            TASK_DESCRIPTION
        )).thenReturn(false);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(correspondenceEventAuditOrchestrationService)
            .publishEvent(event, TASK_DESCRIPTION);

        verify(correspondenceEventAuditOrchestrationService, never())
            .reconcileAndPersistAudits(any(), any(), anyString());
    }

    public static FinremCaseDetails caseDetails() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.getNotificationAuditWrapper()
            .setNotificationEventId(NOTIFICATION_EVENT_ID);

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(12345L)
            .data(caseData)
            .build();

        caseDetails.setCaseType(CaseType.CONTESTED);

        return caseDetails;
    }

    public static FinremCallbackRequest callbackRequest(
        FinremCaseDetails caseDetails,
        EventType eventType
    ) {
        return FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventType(eventType)
            .build();
    }

    private static final class TestHandler extends CUILinkToCaseSubmittedHandler {

        private TestHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, EvidenceManagementDeleteService evidenceManagementDeleteService,
                            RetryExecutor retryExecutor, SignInConfirmationCorresponder signInConfirmationCorresponder,
                            CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService) {
            super(
                finremCaseDetailsMapper,
                evidenceManagementDeleteService,
                retryExecutor,
                signInConfirmationCorresponder,
                correspondenceEventAuditOrchestrationService
            );
        }

        @Override
        public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
            return callbackType == CallbackType.SUBMITTED
                && caseType == CaseType.CONTESTED
                && eventType == EventType.LINK_APPLICANT_TO_CASE;
        }

        @Override
        protected NotificationParty notificationParty() {
            return NotificationParty.CITIZEN_APPLICANT;
        }
    }

    public static AccessCodeCollection accessCode(UUID id, String userIdamId, LocalDateTime usedAt) {
        return AccessCodeCollection.builder()
            .id(id)
            .value(AccessCodeEntry.builder().userIdamID(userIdamId).usedAt(usedAt).build())
            .build();
    }
}
