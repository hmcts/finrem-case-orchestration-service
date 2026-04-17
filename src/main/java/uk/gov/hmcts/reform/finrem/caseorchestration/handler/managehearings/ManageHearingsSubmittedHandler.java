package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managehearings;

import com.ibm.icu.text.ListFormatter;
import feign.FeignException;
import feign.Request;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing.ManageHearingsCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        
        FinremCaseData finremCaseData = callbackRequest.getCaseDetails().getData();

        ManageHearingsWrapper manageHearingsWrapper = finremCaseData.getManageHearingsWrapper();

        ManageHearingsAction actionSelection = manageHearingsWrapper.getManageHearingsActionSelection();

        List<String> errors = new ArrayList<>();

        if (ManageHearingsAction.ADD_HEARING.equals(actionSelection)) {
            log.info("Beginning hearing correspondence for {} action. Case reference: {}",
                ManageHearingsAction.ADD_HEARING.getDescription(),
                callbackRequest.getCaseDetails().getCaseIdAsString());
            ofNullable(
                manageHearingsCorresponder.buildHearingCorrespondenceEventIfNeeded(callbackRequest, userAuthorisation)
            ).ifPresent(event -> this.publishEvent(
                appendNotifyingParties("Send hearing correspondence", event), event, errors));
        }

        if (ManageHearingsAction.ADJOURN_OR_VACATE_HEARING.equals(actionSelection)) {
            log.info("Beginning hearing correspondence for {} action. Case reference: {}",
                ManageHearingsAction.ADJOURN_OR_VACATE_HEARING.getDescription(),
                callbackRequest.getCaseDetails().getCaseIdAsString());
            ofNullable(
                manageHearingsCorresponder.buildAdjournedOrVacatedHearingCorrespondenceEventIfNeeded(callbackRequest, userAuthorisation)
            ).ifPresent(event -> this.publishEvent(
                appendNotifyingParties("Send adjourned or vacate hearing correspondence", event), event, errors)
            );
        }

        if (errors.isEmpty()) {
            return submittedResponse();
        }
        return submittedResponse(
            toConfirmationHeader("Manage Hearings completed with some errors"),
            toConfirmationBody(errors.toArray(new String[0]))
        );
    }

    private String appendNotifyingParties(String eventName, SendCorrespondenceEvent event) {
        String notifyingPartiesString = ListFormatter.getInstance(Locale.ENGLISH)
            .format(
                event.getNotificationParties().stream()
                    .map(NotificationParty::getDescription)
                    .sorted()
                    .toList()
            );
        return format("%s to %s", eventName, notifyingPartiesString);
    }

    private void publishEvent(String eventDescription, SendCorrespondenceEvent event, List<String> errors) {
        retryExecutor.runWithRetryWithHandler(
            () -> {
                if (eventDescription.contains("adjourned")) {
                    throw new FeignException.InternalServerError(
                        "Simulated 500",
                        Request.create(
                            Request.HttpMethod.GET,
                            "/test",
                            Map.of(),
                            null,
                            null,
                            null
                        ),
                        null,
                        null
                    );
                }
                applicationEventPublisher.publishEvent(event);
            },
            eventDescription,
            event.getCaseId(),
            (exception, actionName, caseId1) ->
                errors.add(format("Fail to %s.", eventDescription.toLowerCase()))
        );
    }
}
