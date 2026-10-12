package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTIFICATION_EVENT_ID;

@ExtendWith(MockitoExtension.class)
class CorrespondenceSubmittedHandlerTest {

    private static final String TASK_DESCRIPTION = "Send correspondence";

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Mock
    private EvidenceManagementDeleteService evidenceManagementDeleteService;

    @Mock
    private RetryExecutor retryExecutor;

    @Mock
    private CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

    private TestHandler handler;

    @BeforeEach
    void setUp() {
        handler = new TestHandler(
            finremCaseDetailsMapper,
            evidenceManagementDeleteService,
            retryExecutor,
            correspondenceEventAuditOrchestrationService
        );
    }

    @Test
    void givenCorrespondenceEventPublishSucceeds_whenHandlingSubmittedCallback_thenPublishesAndReconcilesAudits() {
        FinremCaseDetails caseDetails = caseDetailsWithNotificationEventId();

        FinremCallbackRequest callbackRequest = callbackRequest(
            caseDetails,
            EventType.CUI_APPLICANT_DOCUMENT_UPLOAD
        );

        SendCorrespondenceEvent correspondenceEvent =
            SendCorrespondenceEvent.builder()
                .caseDetails(caseDetails)
                .build();

        handler.setCorrespondenceEvent(correspondenceEvent);

        when(correspondenceEventAuditOrchestrationService.publishEvent(
            correspondenceEvent,
            TASK_DESCRIPTION
        )).thenReturn(true);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(correspondenceEventAuditOrchestrationService)
            .publishEvent(
                correspondenceEvent,
                TASK_DESCRIPTION
            );

        verify(correspondenceEventAuditOrchestrationService)
            .reconcileAndPersistAudits(
                caseDetails,
                correspondenceEvent,
                "markPendingNotificationsAsSent"
            );

        assertThat(correspondenceEvent.getEventId())
            .isEqualTo(
                EventType.CUI_APPLICANT_DOCUMENT_UPLOAD.getCcdType()
            );

        assertThat(correspondenceEvent.getNotificationTrackerId())
            .isEqualTo(NOTIFICATION_EVENT_ID);
    }

    @Test
    void givenCorrespondenceEventPublishFails_whenHandlingSubmittedCallback_thenDoesNotReconcileAudits() {
        FinremCaseDetails caseDetails = caseDetailsWithNotificationEventId();

        FinremCallbackRequest callbackRequest = callbackRequest(
            caseDetails,
            EventType.CUI_APPLICANT_DOCUMENT_UPLOAD
        );

        SendCorrespondenceEvent correspondenceEvent =
            SendCorrespondenceEvent.builder()
                .caseDetails(caseDetails)
                .build();

        handler.setCorrespondenceEvent(correspondenceEvent);

        when(correspondenceEventAuditOrchestrationService.publishEvent(
            correspondenceEvent,
            TASK_DESCRIPTION
        )).thenReturn(false);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(correspondenceEventAuditOrchestrationService)
            .publishEvent(
                correspondenceEvent,
                TASK_DESCRIPTION
            );

        verify(correspondenceEventAuditOrchestrationService, never())
            .reconcileAndPersistAudits(
                any(),
                any(),
                anyString()
            );
    }

    private static FinremCaseDetails caseDetailsWithNotificationEventId() {
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

    private static FinremCallbackRequest callbackRequest(
        FinremCaseDetails caseDetails,
        EventType eventType
    ) {
        return FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventType(eventType)
            .build();
    }

    private static final class TestHandler
        extends CorrespondenceSubmittedHandler {

        private SendCorrespondenceEvent correspondenceEvent;

        private TestHandler(
            FinremCaseDetailsMapper finremCaseDetailsMapper,
            EvidenceManagementDeleteService evidenceManagementDeleteService,
            RetryExecutor retryExecutor,
            CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService
        ) {
            super(
                finremCaseDetailsMapper,
                evidenceManagementDeleteService,
                retryExecutor,
                correspondenceEventAuditOrchestrationService
            );
        }

        void setCorrespondenceEvent(
            SendCorrespondenceEvent correspondenceEvent
        ) {
            this.correspondenceEvent = correspondenceEvent;
        }

        @Override
        protected SendCorrespondenceEvent buildSendCorrespondenceEvent(
            FinremCallbackRequest callbackRequest,
            String userAuthorisation
        ) {
            return correspondenceEvent;
        }

        @Override
        protected String correspondenceTaskDescription() {
            return TASK_DESCRIPTION;
        }

        @Override
        public boolean canHandle(
            CallbackType callbackType,
            CaseType caseType,
            EventType eventType
        ) {
            return true;
        }
    }
}
