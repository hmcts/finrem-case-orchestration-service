package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.documentupload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen.CitizenDocumentsUploadedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CUIDocumentUploadSubmittedHandlerTest {

    public static final String AUTH_TOKEN = "auth";
    public static final String NOTIFICATION_EVENT_ID = "notification-event-id";

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Mock
    private EvidenceManagementDeleteService evidenceManagementDeleteService;

    @Mock
    private RetryExecutor retryExecutor;

    @Mock
    private CitizenDocumentsUploadedCorresponder citizenDocumentsUploadedCorresponder;

    @Mock
    private CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

    @Test
    void shouldPublishAndReconcile_whenPublishSucceeds() {
        TestHandler handler = new TestHandler(
            finremCaseDetailsMapper,
            evidenceManagementDeleteService,
            retryExecutor,
            citizenDocumentsUploadedCorresponder,
            correspondenceEventAuditOrchestrationService
        );

        FinremCaseDetails caseDetails = caseDetails();
        FinremCallbackRequest callbackRequest = callbackRequest(caseDetails, EventType.CUI_APPLICANT_DOCUMENT_UPLOAD);
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder().caseDetails(caseDetails).build();

        when(citizenDocumentsUploadedCorresponder.buildCorrespondenceEvent(caseDetails, AUTH_TOKEN, NotificationParty.CITIZEN_APPLICANT))
            .thenReturn(event);
        when(correspondenceEventAuditOrchestrationService.publishEvent(any(), anyString())).thenReturn(true);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(citizenDocumentsUploadedCorresponder)
            .buildCorrespondenceEvent(caseDetails, AUTH_TOKEN, NotificationParty.CITIZEN_APPLICANT);
        verify(correspondenceEventAuditOrchestrationService)
            .publishEvent(event, "Send citizen documents uploaded email: CITIZEN_APPLICANT");
        verify(correspondenceEventAuditOrchestrationService)
            .reconcileAndPersistAudits(caseDetails, event, "markPendingNotificationsAsSent");

        assertThat(event.getEventId()).isEqualTo(EventType.CUI_APPLICANT_DOCUMENT_UPLOAD.getCcdType());
        assertThat(event.getNotificationTrackerId()).isEqualTo(NOTIFICATION_EVENT_ID);
    }

    @Test
    void shouldNotReconcile_whenPublishFails() {
        TestHandler handler = new TestHandler(
            finremCaseDetailsMapper,
            evidenceManagementDeleteService,
            retryExecutor,
            citizenDocumentsUploadedCorresponder,
            correspondenceEventAuditOrchestrationService
        );

        FinremCaseDetails caseDetails = caseDetails();
        FinremCallbackRequest callbackRequest = callbackRequest(caseDetails, EventType.CUI_APPLICANT_DOCUMENT_UPLOAD);
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder().caseDetails(caseDetails).build();

        when(citizenDocumentsUploadedCorresponder.buildCorrespondenceEvent(caseDetails, AUTH_TOKEN, NotificationParty.CITIZEN_APPLICANT))
            .thenReturn(event);
        when(correspondenceEventAuditOrchestrationService.publishEvent(any(), anyString())).thenReturn(false);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(citizenDocumentsUploadedCorresponder)
            .buildCorrespondenceEvent(caseDetails, AUTH_TOKEN, NotificationParty.CITIZEN_APPLICANT);
        verify(correspondenceEventAuditOrchestrationService)
            .publishEvent(event, "Send citizen documents uploaded email: CITIZEN_APPLICANT");
        verify(correspondenceEventAuditOrchestrationService, never())
            .reconcileAndPersistAudits(any(), any(), anyString());

        assertThat(event.getEventId()).isEqualTo(EventType.CUI_APPLICANT_DOCUMENT_UPLOAD.getCcdType());
        assertThat(event.getNotificationTrackerId()).isEqualTo(NOTIFICATION_EVENT_ID);
    }

    public static FinremCaseDetails caseDetails() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.getNotificationAuditWrapper().setNotificationEventId(NOTIFICATION_EVENT_ID);

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(12345L)
            .data(caseData)
            .build();
        caseDetails.setCaseType(CaseType.CONTESTED);
        return caseDetails;
    }

    public static FinremCallbackRequest callbackRequest(FinremCaseDetails caseDetails, EventType eventType) {
        return FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventType(eventType)
            .build();
    }

    private static final class TestHandler extends CUIDocumentUploadSubmittedHandler {
        private TestHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                            EvidenceManagementDeleteService evidenceManagementDeleteService,
                            RetryExecutor retryExecutor,
                            CitizenDocumentsUploadedCorresponder citizenDocumentsUploadedCorresponder,
                            CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService) {
            super(finremCaseDetailsMapper, evidenceManagementDeleteService, retryExecutor,
                citizenDocumentsUploadedCorresponder, correspondenceEventAuditOrchestrationService);
        }

        @Override
        protected NotificationParty notificationParty() {
            return NotificationParty.CITIZEN_APPLICANT;
        }

        @Override
        public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
            return true;
        }
    }
}
