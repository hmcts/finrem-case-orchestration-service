package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managehearings;

import com.ibm.icu.text.ListFormatter;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing.ManageHearingsCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        if (ManageHearingsAction.ADD_HEARING.equals(actionSelection)) {
            ofNullable(
                manageHearingsCorresponder.buildHearingCorrespondenceEventIfNeeded(callbackRequest, userAuthorisation)
            ).ifPresent(event -> this.publishEvent(
                "Send hearing correspondence", event, errors));
        } else if (ManageHearingsAction.ADJOURN_OR_VACATE_HEARING.equals(actionSelection)) {
            ofNullable(
                manageHearingsCorresponder.buildAdjournedOrVacatedHearingCorrespondenceEventIfNeeded(callbackRequest, userAuthorisation)
            ).ifPresent(event -> this.publishEvent(
                "Send adjourned or vacate hearing correspondence", event, errors)
            );
        }

        if (errors.isEmpty()) {
            return submittedResponse();
        }
        return submittedResponse(
            toConfirmationHeader("Manage Hearings completed with error"),
            toConfirmationBody(errors.toArray(new String[0]))
        );
    }

    private String describeNotificationParties(SendCorrespondenceEvent event) {
        return ListFormatter.getInstance(Locale.ENGLISH).format(event.getNotificationParties()
            .stream().map(this::describeNotificationParty).sorted().toList());
    }

    private String describeNotificationParty(NotificationParty notificationParty) {
        // only
        return switch (notificationParty) {
            case APPLICANT -> "applicant";
            case RESPONDENT -> "respondent";
            case INTERVENER_ONE -> "intervener 1";
            case INTERVENER_TWO -> "intervener 2";
            case INTERVENER_THREE -> "intervener 3";
            case INTERVENER_FOUR -> "intervener 4";
            default -> throw new IllegalStateException("Unexpected value: " + notificationParty);
        };
    }

    private void publishEvent(String eventDescription, SendCorrespondenceEvent event, List<String> errors) {
        String notifyingPartyInString = describeNotificationParties(event);
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
