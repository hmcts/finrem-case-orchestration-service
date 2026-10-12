package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public abstract class CorrespondenceSubmittedHandler extends FinremSubmittedCallbackHandler {

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
        log.info(CallbackHandlerLogger.submitted(callbackRequest));

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
            log.warn("{} - Failed to publish correspondence event. eventId: {}, description: {}",
                callbackRequest.getCaseDetails().getCaseIdAsString(),
                event.getEventId(),
                correspondenceTaskDescription()
            );

            return submittedResponse();
        }

        correspondenceEventAuditOrchestrationService
            .reconcileAndPersistAudits(
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
