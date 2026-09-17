package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.documentupload;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremSubmittedCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.Optional;

@Slf4j
public abstract class CUIDocumentUploadSubmittedHandler extends FinremSubmittedCallbackHandler {

    protected final NotificationService notificationService;
    private final CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

    protected CUIDocumentUploadSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                EvidenceManagementDeleteService evidenceManagementDeleteService,
                                                RetryExecutor retryExecutor,
                                                NotificationService notificationService,
                                                CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService) {
        super(finremCaseDetailsMapper, evidenceManagementDeleteService, retryExecutor);
        this.notificationService = notificationService;
        this.correspondenceEventAuditOrchestrationService = correspondenceEventAuditOrchestrationService;
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                                String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));

        final String caseId = callbackRequest.getCaseDetails().getCaseIdAsString();
        Optional<SendCorrespondenceEvent> optionalEvent = buildSendCorrespondenceEvent(
            callbackRequest, userAuthorisation
        );

        if (optionalEvent.isEmpty()) {
            logNotificationFailure(caseId);
            return submittedResponse();
        }

        SendCorrespondenceEvent event = optionalEvent.get();
        event.setEventId(callbackRequest.getEventType().getCcdType());
        event.setNotificationTrackerId(callbackRequest.getCaseDetails().getData().getNotificationAuditWrapper().getNotificationEventId());

        boolean success = correspondenceEventAuditOrchestrationService.publishEvent(event, correspondenceTaskDescription());
        if (!success) {
            logNotificationFailure(caseId);
            return submittedResponse();
        }

        correspondenceEventAuditOrchestrationService.reconcileAndPersistAudits(
            callbackRequest.getCaseDetails(),
            event,
            "markPendingNotificationsAsSent"
        );

        return submittedResponse();
    }

    private Optional<SendCorrespondenceEvent> buildSendCorrespondenceEvent(FinremCallbackRequest callbackRequest,
                                                                            String userAuthorisation) {
        return notificationService.buildCitizenUploadDocumentsNotificationEvent(
            callbackRequest.getCaseDetails(),
            userAuthorisation,
            notificationParty()
        );
    }

    private void logNotificationFailure(String caseId) {
        log.warn(
            "{} - Failed to send citizen documents uploaded email: {}", caseId, getNotificationPartyLabel()
        );
    }

    private String correspondenceTaskDescription() {
        return String.format("Send citizen documents uploaded email: %s", getNotificationPartyLabel());
    }

    protected abstract NotificationParty notificationParty();

    private String getNotificationPartyLabel() {
        return notificationParty().getRole();
    }
}
