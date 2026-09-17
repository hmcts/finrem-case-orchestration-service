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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen.CUIDocumentsUploadedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

@Slf4j
public abstract class CUIDocumentUploadSubmittedHandler extends FinremSubmittedCallbackHandler {

    private final CUIDocumentsUploadedCorresponder cuiDocumentsUploadedCorresponder;
    private final CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

    protected CUIDocumentUploadSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                EvidenceManagementDeleteService evidenceManagementDeleteService,
                                                RetryExecutor retryExecutor,
                                                CUIDocumentsUploadedCorresponder cuiDocumentsUploadedCorresponder,
                                                CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService) {
        super(finremCaseDetailsMapper, evidenceManagementDeleteService, retryExecutor);
        this.cuiDocumentsUploadedCorresponder = cuiDocumentsUploadedCorresponder;
        this.correspondenceEventAuditOrchestrationService = correspondenceEventAuditOrchestrationService;
    }

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
        return cuiDocumentsUploadedCorresponder.buildCorrespondenceEvent(
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

    protected abstract NotificationParty notificationParty();

    private String getNotificationPartyLabel() {
        return notificationParty().getRole();
    }
}
