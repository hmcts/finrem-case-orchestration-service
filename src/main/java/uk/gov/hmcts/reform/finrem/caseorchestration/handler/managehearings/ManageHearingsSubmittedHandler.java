package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managehearings;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationAuditService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing.ManageHearingsCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.INTERNAL_CHANGE_UPDATE_CASE;

@Slf4j
@Service
public class ManageHearingsSubmittedHandler extends FinremCallbackHandler {

    private final RetryExecutor retryExecutor;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final ManageHearingsCorresponder manageHearingsCorresponder;

    private final CoreCaseDataService coreCaseDataService;

    private final NotificationAuditService notificationAuditService;

    public ManageHearingsSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                          ManageHearingsCorresponder manageHearingsCorresponder,
                                          RetryExecutor retryExecutor,
                                          ApplicationEventPublisher applicationEventPublisher,
                                          CoreCaseDataService coreCaseDataService,
                                          NotificationAuditService notificationAuditService) {
        super(finremCaseDetailsMapper);
        this.manageHearingsCorresponder = manageHearingsCorresponder;
        this.retryExecutor = retryExecutor;
        this.applicationEventPublisher = applicationEventPublisher;
        this.coreCaseDataService = coreCaseDataService;
        this.notificationAuditService = notificationAuditService;
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

        log.info("Beginning hearing correspondence for {} action. Case reference: {}",
            actionSelection.getDescription(), finremCaseData.getCcdCaseId());

        List<String> errors = new ArrayList<>();

        SendCorrespondenceEvent correspondenceEvent = manageHearingsCorresponder.buildCorrespondenceEventIfNeeded(
            actionSelection,
            callbackRequest,
            userAuthorisation
        );

        if (correspondenceEvent != null) {
            correspondenceEvent.setEventId(callbackRequest.getEventType().getCcdType());

            publishEvent(getEventDescription(actionSelection), correspondenceEvent, errors);

            markPendingNotificationsAsSent(caseDetails, correspondenceEvent);
        }

        if (errors.isEmpty()) {
            return submittedResponse();
        }
        return submittedResponse(
            toConfirmationHeader("Manage Hearings completed with error"),
            toConfirmationBody(errors.toArray(new String[0]))
        );
    }

    private String getEventDescription(ManageHearingsAction actionSelection) {
        return switch (actionSelection) {
            case ADD_HEARING -> "Send hearing correspondence";
            case ADJOURN_OR_VACATE_HEARING -> "Send adjourned or vacate hearing correspondence";
        };
    }

    private void publishEvent(String eventDescription, SendCorrespondenceEvent event, List<String> errors) {
        String notifyingPartyInString = event.describeNotificationParties();
        retryExecutor.runWithRetryWithHandler(
            () -> applicationEventPublisher.publishEvent(event),
            eventDescription,
            event.getCaseId(),
            (exception, actionName, caseId1) ->
                errors.add(format("Notification to %s has failed. Please send notification to %s manually.",
                    notifyingPartyInString, notifyingPartyInString))
        );
    }

    private void markPendingNotificationsAsSent(FinremCaseDetails caseDetails,
                                                SendCorrespondenceEvent correspondenceEvent) {

        Map<String, Object> updatedFields =
            notificationAuditService.updateSentAuditsList(correspondenceEvent);

        if (!updatedFields.isEmpty()) {
            retryExecutor.runWithRetrySuppressException(
                () -> coreCaseDataService.performPostSubmitCallback(
                    caseDetails.getData().getCcdCaseType(),
                    caseDetails.getId(),
                    INTERNAL_CHANGE_UPDATE_CASE.getCcdType(),
                    latestCaseDetails -> updatedFields
                ),
                "markPendingNotificationsAsSent",
                caseDetails.getCaseIdAsString()
            );
        }
    }
}
