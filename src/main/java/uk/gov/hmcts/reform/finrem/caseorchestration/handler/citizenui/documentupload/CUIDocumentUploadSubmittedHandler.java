package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.documentupload;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremSubmittedCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryErrorHandler;

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

        Optional<SendCorrespondenceEvent> optionalEvent = buildSendCorrespondenceEvent(callbackRequest, userAuthorisation);

        if (optionalEvent.isEmpty()) {
            log.warn("{} - {}", callbackRequest.getCaseDetails().getCaseIdAsString(), noRecipientWarningMessage());
            return submittedResponse();
        }

        SendCorrespondenceEvent event = optionalEvent.get();
        event.setEventId(callbackRequest.getEventType().getCcdType());
        event.setNotificationTrackerId(callbackRequest.getCaseDetails().getData().getNotificationAuditWrapper().getNotificationEventId());

        boolean success = correspondenceEventAuditOrchestrationService.publishEvent(
            event,
            correspondenceTaskDescription(),
            notificationFailureHandler(event)
        );
        if (success) {
            correspondenceEventAuditOrchestrationService.reconcileAndPersistAudits(
                callbackRequest.getCaseDetails(),
                event,
                markAuditsActionName()
            );
        }

        return submittedResponse();
    }

    protected RetryErrorHandler notificationFailureHandler(SendCorrespondenceEvent event) {
        return (exception, actionName, caseId) -> {
            // no-op: CUI flow intentionally returns a standard submitted response.
        };
    }

    protected abstract Optional<SendCorrespondenceEvent> buildSendCorrespondenceEvent(FinremCallbackRequest callbackRequest,
                                                                                       String userAuthorisation);

    protected abstract String noRecipientWarningMessage();

    protected abstract String correspondenceTaskDescription();

    protected abstract String markAuditsActionName();
}
