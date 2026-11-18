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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing.ManageHearingsCorresponder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        List<String> errors = sendHearingCorrespondence(callbackRequest, userAuthorisation, caseDetails);

        if (errors.isEmpty()) {
            return submittedResponse();
        }
        return submittedResponse(
            toConfirmationHeader("Manage Hearings completed with error"),
            toConfirmationBody(errors.toArray(new String[0]))
        );
    }

    private List<String> sendHearingCorrespondence(FinremCallbackRequest callbackRequest,
                                                   String userAuthorisation,
                                                   FinremCaseDetails caseDetails) {
        FinremCaseData finremCaseData = callbackRequest.getFinremCaseData();
        ManageHearingsAction actionSelection = finremCaseData.getManageHearingsWrapper().getManageHearingsActionSelection();

        List<String> errors = new ArrayList<>();
        List<SendCorrespondenceEvent> correspondenceEvents = manageHearingsCorresponder
            .buildCorrespondenceEventsIfNeeded(actionSelection, callbackRequest, userAuthorisation);

        for (SendCorrespondenceEvent event : correspondenceEvents) {
            log.info("Sending hearing correspondence for {} action. Party: {}. Case reference: {}",
                actionSelection.getDescription(), event.describeNotificationParties(), finremCaseData.getCcdCaseId());
            event.setEventId(callbackRequest.getEventType().getCcdType());
            event.setNotificationTrackerId(finremCaseData.getNotificationAuditWrapper().getNotificationEventId());

            String error = publishEvent(event);
            if (isNotBlank(error)) {
                errors.add(error);
            }
        }

        if (!correspondenceEvents.isEmpty()) {
            correspondenceEventAuditOrchestrationService.reconcileAndPersistAudits(
                caseDetails,
                "markPendingNotificationsAsSent",
                correspondenceEvents.toArray(new SendCorrespondenceEvent[0])
            );
        }

        return errors;
    }

    private String publishEvent(SendCorrespondenceEvent event) {
        String notifyingPartyInString = event.describeNotificationParties();

        AtomicReference<String> error = new AtomicReference<>();
        correspondenceEventAuditOrchestrationService.publishEvent(
            event,
            "sending hearing correspondence %s (%s)".formatted(
                event.getNotificationTrackerId(),
                event.describeNotificationParties()
            ),
            () -> error.set(format("Notification to %s has failed. Please send notification to %s manually.",
                    notifyingPartyInString, notifyingPartyInString))
        );
        return error.get();
    }
}
