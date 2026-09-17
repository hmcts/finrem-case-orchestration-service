package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managehearings;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing.ManageHearingsCorresponder;

import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
public class ManageHearingsSubmittedHandler extends FinremCallbackHandler {

    private final ManageHearingsCorresponder manageHearingsCorresponder;

    private final CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

    public ManageHearingsSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                          ManageHearingsCorresponder manageHearingsCorresponder,
                                          CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService) {
        super(finremCaseDetailsMapper);
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

        FinremCaseData finremCaseData = callbackRequest.getFinremCaseData();
        ManageHearingsAction actionSelection = finremCaseData.getManageHearingsWrapper().getManageHearingsActionSelection();

        SendCorrespondenceEvent correspondenceEvent = manageHearingsCorresponder.buildCorrespondenceEventIfNeeded(
            actionSelection,
            callbackRequest,
            userAuthorisation
        );

        String error = null;
        if (nonNull(correspondenceEvent)) {
            log.info("Sending hearing correspondence for {} action. Case reference: {}",
                actionSelection.getDescription(), finremCaseData.getCcdCaseId());
            correspondenceEvent.setEventId(callbackRequest.getEventType().getCcdType());

            correspondenceEvent.setNotificationTrackerId(
                finremCaseData.getNotificationAuditWrapper().getNotificationEventId()
            );

            AtomicReference<String> errorMessage = new AtomicReference<>();
            boolean success = correspondenceEventAuditOrchestrationService.publishEvent(
                correspondenceEvent,
                getEventDescription(actionSelection),
                (exception, actionName, caseId) -> errorMessage.set(
                    format("Notification to %s has failed. Please send notification to %s manually.",
                        correspondenceEvent.describeNotificationParties(), correspondenceEvent.describeNotificationParties())
                )
            );

            error = errorMessage.get();
            if (success) {
                correspondenceEventAuditOrchestrationService.reconcileAndPersistAudits(
                    callbackRequest.getCaseDetails(),
                    correspondenceEvent,
                    "markPendingNotificationsAsSent"
                );
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

}
