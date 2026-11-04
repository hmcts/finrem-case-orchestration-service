package uk.gov.hmcts.reform.finrem.caseorchestration.handler.corresponder;

import org.slf4j.Logger;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremSubmittedCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

/**
 * Base submitted-callback handler for correspondence events.
 *
 * <p>This abstract class encapsulates the common submitted-stage workflow:
 * <ol>
 *     <li>Log the submitted callback.</li>
 *     <li>Build a correspondence event (implemented by subclasses).</li>
 *     <li>Enrich the event with CCD event metadata and notification tracker id.</li>
 *     <li>Publish the event via the orchestration service.</li>
 *     <li>On success, reconcile and persist notification audit records.</li>
 * </ol>
 *
 * <p>Subclasses provide the concrete event payload, task description and logger.
 */
public abstract class CorrespondenceSubmittedHandler extends FinremSubmittedCallbackHandler {

    protected abstract Logger getLogger();

    private final CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

    protected CorrespondenceSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                             EvidenceManagementDeleteService evidenceManagementDeleteService,
                                             RetryExecutor retryExecutor,
                                             CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService) {
        super(finremCaseDetailsMapper, evidenceManagementDeleteService, retryExecutor);
        this.correspondenceEventAuditOrchestrationService = correspondenceEventAuditOrchestrationService;
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest, String userAuthorisation) {
        getLogger().info(CallbackHandlerLogger.submitted(callbackRequest));

        SendCorrespondenceEvent event = buildSendCorrespondenceEvent(callbackRequest, userAuthorisation);

        event.setEventId(callbackRequest.getEventType().getCcdType());

        event.setNotificationTrackerId(callbackRequest.getCaseDetails()
                .getData()
                .getNotificationAuditWrapper()
                .getNotificationEventId()
        );

        boolean success = correspondenceEventAuditOrchestrationService.publishEvent(event,
                correspondenceTaskDescription());

        if (!success) {
            getLogger().warn("{} - Failed to publish correspondence event. eventId: {}, description: {}",
                    callbackRequest.getCaseDetails().getCaseIdAsString(),
                    event.getEventId(),
                    correspondenceTaskDescription()
            );

            return submittedResponse();
        }

        correspondenceEventAuditOrchestrationService.reconcileAndPersistAudits(
                callbackRequest.getCaseDetails(),
                event,
                "markPendingNotificationsAsSent");

        return submittedResponse();
    }

    protected abstract SendCorrespondenceEvent buildSendCorrespondenceEvent(
            FinremCallbackRequest callbackRequest,
            String userAuthorisation
    );

    protected abstract String correspondenceTaskDescription();
}
