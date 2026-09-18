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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen.CitizenDocumentsUploadedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

/**
 * Base submitted-stage handler for citizen document upload events.
 *
 * <p>Subclasses provide party-specific routing (applicant/respondent), while this handler
 * performs the shared flow to:
 * <ol>
 *     <li>Build the correspondence event for the citizen upload notification</li>
 *     <li>Publish the event with retry support</li>
 *     <li>Reconcile and persist notification audits after a successful publish</li>
 * </ol>
 */
@Slf4j
public abstract class CUIDocumentUploadSubmittedHandler extends FinremSubmittedCallbackHandler {

    private final CitizenDocumentsUploadedCorresponder citizenDocumentsUploadedCorresponder;
    private final CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

    protected CUIDocumentUploadSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                EvidenceManagementDeleteService evidenceManagementDeleteService,
                                                RetryExecutor retryExecutor,
                                                CitizenDocumentsUploadedCorresponder citizenDocumentsUploadedCorresponder,
                                                CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService) {
        super(finremCaseDetailsMapper, evidenceManagementDeleteService, retryExecutor);
        this.citizenDocumentsUploadedCorresponder = citizenDocumentsUploadedCorresponder;
        this.correspondenceEventAuditOrchestrationService = correspondenceEventAuditOrchestrationService;
    }

    /**
     * Handles the SUBMITTED callback for citizen document upload notifications.
     *
     * <p>Publishes a notification event for the citizen. If publication fails, the handler logs the
     * failure and returns a standard submitted response without attempting audit reconciliation. On success,
     * pending audits are reconciled and persisted.</p>
     *
     * @param callbackRequest callback payload containing case details and event type
     * @param userAuthorisation user auth token used by downstream notification listeners
     * @return submitted callback response
     */
    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                                String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));

        final String caseId = callbackRequest.getCaseDetails().getCaseIdAsString();
        SendCorrespondenceEvent event = buildSendCorrespondenceEvent(callbackRequest, userAuthorisation);
        event.setEventId(callbackRequest.getEventType().getCcdType());
        event.setNotificationTrackerId(callbackRequest.getCaseDetails().getData().getNotificationAuditWrapper().getNotificationEventId());

        boolean success = correspondenceEventAuditOrchestrationService.publishEvent(event, correspondenceTaskDescription());
        if (!success) {
            logPublishFailure(caseId, event.getEventId());
            return submittedResponse();
        }

        correspondenceEventAuditOrchestrationService.reconcileAndPersistAudits(
            callbackRequest.getCaseDetails(),
            event,
            "markPendingNotificationsAsSent"
        );

        return submittedResponse();
    }

    private SendCorrespondenceEvent buildSendCorrespondenceEvent(FinremCallbackRequest callbackRequest,
                                                                  String userAuthorisation) {
        return citizenDocumentsUploadedCorresponder.buildCorrespondenceEvent(
            callbackRequest.getCaseDetails(),
            userAuthorisation,
            notificationParty()
        );
    }

    private void logPublishFailure(String caseId, String eventId) {
        log.warn(
            "{} - Failed to publish citizen documents uploaded email event. eventId: {}, party: {}",
            caseId,
            eventId,
            getNotificationPartyLabel()
        );
    }

    private String correspondenceTaskDescription() {
        return String.format("Send citizen documents uploaded email: %s", getNotificationPartyLabel());
    }

    /**
     * Returns the target citizen party for the handler implementation.
     *
     * @return notification party (citizen applicant or citizen respondent)
     */
    protected abstract NotificationParty notificationParty();

    private String getNotificationPartyLabel() {
        return notificationParty().getRole();
    }
}
