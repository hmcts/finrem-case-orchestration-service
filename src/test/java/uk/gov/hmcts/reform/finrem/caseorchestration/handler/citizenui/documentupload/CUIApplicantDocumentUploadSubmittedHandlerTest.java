package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.documentupload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class CUIApplicantDocumentUploadSubmittedHandlerTest {

    @InjectMocks
    private CUIApplicantDocumentUploadSubmittedHandler underTest;

    @Mock
    private NotificationService notificationService;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Mock
    private EvidenceManagementDeleteService evidenceManagementDeleteService;

    @Mock
    private RetryExecutor retryExecutor;

    @Mock
    private CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

    @Test
    void canHandleForApplicantSubmittedEvent() {
        assertCanHandle(underTest, CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.CUI_APPLICANT_DOCUMENT_UPLOAD);
    }

    @Test
    void shouldSendApplicantUploadNotification() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(12345L)
            .data(new uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData())
            .build();
        caseDetails.setCaseType(CaseType.CONTESTED);
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventType(EventType.CUI_APPLICANT_DOCUMENT_UPLOAD)
            .build();

        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails)
            .build();

        when(notificationService.buildCitizenUploadDocumentsNotificationEvent(caseDetails, "auth", NotificationParty.CUI_APPLICANT))
            .thenReturn(java.util.Optional.of(event));
        when(correspondenceEventAuditOrchestrationService.publishEvent(any(), anyString()))
            .thenReturn(true);

        underTest.handle(callbackRequest, "auth");

        verify(notificationService).buildCitizenUploadDocumentsNotificationEvent(caseDetails, "auth", NotificationParty.CUI_APPLICANT);
        verify(correspondenceEventAuditOrchestrationService).publishEvent(any(), anyString());
        verify(correspondenceEventAuditOrchestrationService)
            .reconcileAndPersistAudits(caseDetails, event, "markPendingNotificationsAsSent");
    }

    @Test
    void shouldSkipWhenNoApplicantEmailEventBuilt() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(12345L)
            .data(new uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData())
            .build();
        caseDetails.setCaseType(CaseType.CONTESTED);
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventType(EventType.CUI_APPLICANT_DOCUMENT_UPLOAD)
            .build();

        when(notificationService.buildCitizenUploadDocumentsNotificationEvent(caseDetails, "auth", NotificationParty.CUI_APPLICANT))
            .thenReturn(java.util.Optional.empty());

        underTest.handle(callbackRequest, "auth");

        verify(notificationService).buildCitizenUploadDocumentsNotificationEvent(caseDetails, "auth", NotificationParty.CUI_APPLICANT);
        verify(correspondenceEventAuditOrchestrationService, never()).publishEvent(any(), anyString());
    }
}
