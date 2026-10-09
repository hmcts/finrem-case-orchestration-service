package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managehearings;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremSubmittedCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing.ManageHearingsCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;
import static java.util.Objects.isNull;

@Slf4j
@Service
public class ManageHearingsSubmittedHandler extends FinremSubmittedCallbackHandler {

    private final ManageHearingsCorresponder manageHearingsCorresponder;

    private final CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

    public ManageHearingsSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                          EvidenceManagementDeleteService evidenceManagementDeleteService,
                                          RetryExecutor retryExecutor,
                                          ManageHearingsCorresponder manageHearingsCorresponder,
                                          CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService) {
        super(finremCaseDetailsMapper, evidenceManagementDeleteService, retryExecutor);
        this.manageHearingsCorresponder = manageHearingsCorresponder;
        this.correspondenceEventAuditOrchestrationService = correspondenceEventAuditOrchestrationService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.MANAGE_HEARINGS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = callbackRequest.getFinremCaseData();
        ManageHearingsAction actionSelection = finremCaseData.getManageHearingsWrapper().getManageHearingsActionSelection();

        List<SendCorrespondenceEvent> correspondenceEvents = manageHearingsCorresponder.buildCorrespondenceEventIfNeeded(
            actionSelection,
            callbackRequest,
            userAuthorisation
        );

        String error = null;
        for (SendCorrespondenceEvent correspondenceEvent : correspondenceEvents) {
            correspondenceEvent.setEventId(callbackRequest.getEventType().getCcdType());

            correspondenceEvent.setNotificationTrackerId(
                finremCaseData.getNotificationAuditWrapper().getNotificationEventId()
            );

            error = publishEvent(getEventDescription(actionSelection), correspondenceEvent);
            if (isNull(error)) {
                markPendingNotificationsAsSent(caseDetails, correspondenceEvent);
            }
        }

        if (isNull(error)) {
            return submittedResponse();
        }
        return submittedResponse(
            toConfirmationHeader("Manage Hearings completed with error"),
            toConfirmationBody(error)
        );
    }

    private String getEventDescription(ManageHearingsAction actionSelection) {
        return switch (actionSelection) {
            case ADD_HEARING -> "Send hearing correspondence";
            case ADJOURN_OR_VACATE_HEARING -> "Send adjourned or vacate hearing correspondence";
        };
    }

    private String publishEvent(String eventDescription, SendCorrespondenceEvent event) {
        AtomicReference<String> error = new AtomicReference<>();
        correspondenceEventAuditOrchestrationService.publishEvent(event, eventDescription, () ->
            error.set(format("Notification to %s has failed. Please send notification to %s manually.",
                event.describeNotificationParties(), event.describeNotificationParties()))
        );
        return error.get();
    }

    private void markPendingNotificationsAsSent(FinremCaseDetails caseDetails,
                                                SendCorrespondenceEvent correspondenceEvent) {
        correspondenceEventAuditOrchestrationService.reconcileAndPersistAudits(caseDetails,
            "markPendingNotificationsAsSent", correspondenceEvent);
    }
}
