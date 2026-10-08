package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managehearings;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremAboutToSubmitCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationAuditService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing.ManageHearingsCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingActionService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator.validateRequiredPostalAddresses;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ContestedStatus.PREPARE_FOR_HEARING;

@Slf4j
@Service
public class ManageHearingsAboutToSubmitHandler extends FinremAboutToSubmitCallbackHandler {

    private final ManageHearingActionService manageHearingActionService;
    private final NotificationAuditService notificationAuditService;
    private final ManageHearingsCorresponder manageHearingsCorresponder;
    private final PartyService partyService;

    public ManageHearingsAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                              ManageHearingActionService manageHearingActionService,
                                              NotificationAuditService notificationAuditService,
                                              ManageHearingsCorresponder manageHearingsCorresponder,
                                              PartyService partyService) {
        super(finremCaseDetailsMapper);
        this.manageHearingActionService = manageHearingActionService;
        this.notificationAuditService = notificationAuditService;
        this.manageHearingsCorresponder = manageHearingsCorresponder;
        this.partyService = partyService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.MANAGE_HEARINGS.equals(eventType);
    }

    /**
     * Handles the 'About to Submit' callback for managing hearings.
     * When a hearing is added (ManageHearingsAction.ADD_HEARING), the case state is explicitly set to PREPARE_FOR_HEARING.
     * Other hearing actions, when built, will keep the case in the same state.
     *
     * @param callbackRequest the request containing case details
     * @param userAuthorisation the user authorisation token
     * @return a response containing updated case data
     */
    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();

        FinremCaseData finremCaseData = finremCaseDetails.getData();
        ManageHearingsWrapper hearingsWrapper = finremCaseData.getManageHearingsWrapper();
        ManageHearingsAction actionSelection = hearingsWrapper.getManageHearingsActionSelection();

        if (ManageHearingsAction.ADD_HEARING.equals(actionSelection)
            || (YesOrNo.YES.equals(hearingsWrapper.getIsRelistSelected()))) {
            manageHearingActionService.performAddHearing(finremCaseDetails, userAuthorisation);
            finremCaseData.setState(PREPARE_FOR_HEARING.getId());
        }

        if (ManageHearingsAction.ADJOURN_OR_VACATE_HEARING.equals(actionSelection)) {
            manageHearingActionService.performAdjournOrVacateHearing(finremCaseDetails, userAuthorisation);
        }

        manageHearingActionService.updateTabData(finremCaseData);

        List<SendCorrespondenceEvent> sendCorrespondenceEvents = manageHearingsCorresponder.buildCorrespondenceEventIfNeeded(
            actionSelection,
            callbackRequest,
            userAuthorisation
        );

        List<String> errors = validatePostalAddresses(finremCaseData, actionSelection, sendCorrespondenceEvents);
        if (!errors.isEmpty()) {
            return responseWithoutWarnings(finremCaseData, errors);
        }
        createNotificationAuditRows(callbackRequest, sendCorrespondenceEvents
            .toArray(new SendCorrespondenceEvent[0]));

        return response(finremCaseData);
    }

    private List<String> validatePostalAddresses(FinremCaseData finremCaseData,
                                                 ManageHearingsAction actionSelection,
                                                 List<SendCorrespondenceEvent> sendCorrespondenceEvents) {
        return new ArrayList<>(validateRequiredPostalAddresses(finremCaseData, EventType.MANAGE_HEARINGS,
            shouldValidateApplicantAddress(finremCaseData, actionSelection, sendCorrespondenceEvents),
            shouldValidateRespondentAddress(finremCaseData, actionSelection, sendCorrespondenceEvents)));
    }

    private boolean shouldValidateApplicantAddress(FinremCaseData finremCaseData, ManageHearingsAction actionSelection,
                                                   List<SendCorrespondenceEvent> sendCorrespondenceEvents) {
        ManageHearingsWrapper hearingsWrapper = finremCaseData.getManageHearingsWrapper();

        if (actionSelection == ManageHearingsAction.ADD_HEARING) {
            return isApplicantNotified(sendCorrespondenceEvents)
                && partyService.isApplicantPartySelected(finremCaseData, Objects.requireNonNull(hearingsWrapper.getWorkingHearing()));
        } else {
            return isApplicantNotified(sendCorrespondenceEvents);
        }
    }

    private boolean shouldValidateRespondentAddress(FinremCaseData finremCaseData, ManageHearingsAction actionSelection,
                                                    List<SendCorrespondenceEvent> sendCorrespondenceEvents) {
        ManageHearingsWrapper hearingsWrapper = finremCaseData.getManageHearingsWrapper();

        if (actionSelection == ManageHearingsAction.ADD_HEARING) {
            return isRespondentNotified(sendCorrespondenceEvents)
                && partyService.isRespondentPartySelected(finremCaseData, Objects.requireNonNull(hearingsWrapper.getWorkingHearing()));
        } else {
            return isRespondentNotified(sendCorrespondenceEvents);
        }
    }

    private boolean isApplicantNotified(List<SendCorrespondenceEvent> sendCorrespondenceEvents) {
        return isPartyNotified(sendCorrespondenceEvents, NotificationParty.APPLICANT);
    }

    private boolean isRespondentNotified(List<SendCorrespondenceEvent> sendCorrespondenceEvents) {
        return isPartyNotified(sendCorrespondenceEvents, NotificationParty.RESPONDENT);
    }

    private boolean isPartyNotified(List<SendCorrespondenceEvent> sendCorrespondenceEvents,
                                    NotificationParty party) {
        return ofNullable(sendCorrespondenceEvents)
            .orElse(List.of())
            .stream()
            .map(SendCorrespondenceEvent::getNotificationParties)
            .filter(Objects::nonNull)
            .anyMatch(parties -> parties.contains(party));
    }

    private void createNotificationAuditRows(FinremCallbackRequest callbackRequest,
                                             SendCorrespondenceEvent... events) {
        if (events == null) {
            return;
        }
        Arrays.stream(events)
            .filter(Objects::nonNull)
            .forEach(event -> notificationAuditService.createAuditsForCorrespondence(
                event,
                callbackRequest.getEventType()
            ));
    }
}
