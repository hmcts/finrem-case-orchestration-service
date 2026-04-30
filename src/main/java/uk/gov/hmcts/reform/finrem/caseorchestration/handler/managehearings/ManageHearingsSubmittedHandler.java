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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing.ManageHearingsCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

@Slf4j
@Service
public class ManageHearingsSubmittedHandler extends FinremCallbackHandler {

    private final RetryExecutor retryExecutor;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final ManageHearingsCorresponder manageHearingsCorresponder;

    public ManageHearingsSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                          ManageHearingsCorresponder manageHearingsCorresponder,
                                          RetryExecutor retryExecutor,
                                          ApplicationEventPublisher applicationEventPublisher) {
        super(finremCaseDetailsMapper);
        this.manageHearingsCorresponder = manageHearingsCorresponder;
        this.retryExecutor = retryExecutor;
        this.applicationEventPublisher = applicationEventPublisher;
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
        FinremCaseData finremCaseData = caseDetails.getData();
        ManageHearingsAction actionSelection = finremCaseData.getManageHearingsWrapper().getManageHearingsActionSelection();

        log.info("Beginning hearing correspondence for {} action. Case reference: {}",
            actionSelection.getDescription(), caseDetails.getCaseIdAsString());

        List<String> errors = new ArrayList<>();
        SendCorrespondenceEvent correspondenceEvent = buildCorrespondenceEvent(
            actionSelection,
            callbackRequest,
            userAuthorisation
        );
        ofNullable(correspondenceEvent)
            .ifPresent(event -> publishEvent(getEventDescription(actionSelection), event, errors));

        if (errors.isEmpty()) {
            return submittedResponse();
        }
        return submittedResponse(
            toConfirmationHeader("Manage Hearings completed with error"),
            toConfirmationBody(errors.toArray(new String[0]))
        );
    }

    private SendCorrespondenceEvent buildCorrespondenceEvent(ManageHearingsAction actionSelection,
                                                             FinremCallbackRequest callbackRequest,
                                                             String userAuthorisation) {
        return switch (actionSelection) {
            case ADD_HEARING -> manageHearingsCorresponder.buildHearingCorrespondenceEventIfNeeded(
                callbackRequest,
                userAuthorisation
            );
            case ADJOURN_OR_VACATE_HEARING -> manageHearingsCorresponder
                .buildAdjournedOrVacatedHearingCorrespondenceEventIfNeeded(
                    callbackRequest,
                    userAuthorisation
                );
        };
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
}
