package uk.gov.hmcts.reform.finrem.caseorchestration.handler.generalapplicationdirections;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing.ManageHearingsCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.GENERAL_APPLICATION_DIRECTIONS_MH;

@Slf4j
@Service
public class GeneralApplicationDirectionsSubmittedHandler extends FinremSubmittedCallbackHandler {

    private final ManageHearingsCorresponder manageHearingsCorresponder;
    private final GeneralApplicationDirectionsService generalApplicationDirectionsService;
    private final CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

    public GeneralApplicationDirectionsSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                        EvidenceManagementDeleteService evidenceManagementDeleteService,
                                                        RetryExecutor retryExecutor,
                                                        ManageHearingsCorresponder manageHearingsCorresponder,
                                                        GeneralApplicationDirectionsService generalApplicationDirectionsService,
                                                        CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService
    ) {
        super(finremCaseDetailsMapper, evidenceManagementDeleteService, retryExecutor);
        this.manageHearingsCorresponder = manageHearingsCorresponder;
        this.generalApplicationDirectionsService = generalApplicationDirectionsService;
        this.correspondenceEventAuditOrchestrationService = correspondenceEventAuditOrchestrationService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && GENERAL_APPLICATION_DIRECTIONS_MH.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();

        final List<String> errors = new ArrayList<>();

        // Hearings are optional, so send hearing correspondence if a hearing was added in the event.
        if (generalApplicationDirectionsService.isHearingRequired(finremCaseDetails)) {
            List<SendCorrespondenceEvent> events = manageHearingsCorresponder
                .buildHearingCorrespondenceEventsIfNeeded(callbackRequest, userAuthorisation);

            for (SendCorrespondenceEvent event : events) {
                String task = "Send hearing corresponder to party: %s on general application direction event"
                    .formatted(event.describeNotificationParties());
                String error = publishEvent(task, event);
                if (nonNull(error)) {
                    errors.add(error);
                }
            }
        }

        if (errors.isEmpty()) {
            return submittedResponse();
        }
        return submittedResponse(
            toConfirmationHeader("General Application Direction completed with error"),
            toConfirmationBody(errors.toArray(new String[0]))
        );
    }

    private String publishEvent(String eventDescription, SendCorrespondenceEvent event) {
        AtomicReference<String> error = new AtomicReference<>();
        correspondenceEventAuditOrchestrationService.publishEvent(event, eventDescription, () ->
            error.set("Notification to %s has failed. Please send notification manually."
                .formatted(event.describeNotificationParties()))
        );
        return error.get();
    }
}
