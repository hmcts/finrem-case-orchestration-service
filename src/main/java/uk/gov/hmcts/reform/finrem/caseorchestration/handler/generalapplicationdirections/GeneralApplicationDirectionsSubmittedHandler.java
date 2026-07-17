package uk.gov.hmcts.reform.finrem.caseorchestration.handler.generalapplicationdirections;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing.ManageHearingsCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.GENERAL_APPLICATION_DIRECTIONS_MH;

@Slf4j
@Service
public class GeneralApplicationDirectionsSubmittedHandler extends FinremSubmittedCallbackHandler {

    private final ManageHearingsCorresponder manageHearingsCorresponder;
    private final GeneralApplicationDirectionsService generalApplicationDirectionsService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public GeneralApplicationDirectionsSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                        EvidenceManagementDeleteService evidenceManagementDeleteService,
                                                        ManageHearingsCorresponder manageHearingsCorresponder,
                                                        GeneralApplicationDirectionsService generalApplicationDirectionsService,
                                                        RetryExecutor retryExecutor,
                                                        ApplicationEventPublisher applicationEventPublisher) {
        super(finremCaseDetailsMapper, evidenceManagementDeleteService, retryExecutor);
        this.manageHearingsCorresponder = manageHearingsCorresponder;
        this.generalApplicationDirectionsService = generalApplicationDirectionsService;
        this.applicationEventPublisher = applicationEventPublisher;
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

        // Hearings are optional, so send hearing correspondence if a hearing was added in the event.
        if (generalApplicationDirectionsService.isHearingRequired(finremCaseDetails)) {
            List<SendCorrespondenceEvent> events = manageHearingsCorresponder
                .buildHearingCorrespondenceEventsIfNeeded(callbackRequest, userAuthorisation);

            for (SendCorrespondenceEvent event : events) {
                retryExecutor.runWithRetrySuppressException(
                    () -> applicationEventPublisher.publishEvent(event),
                    "%s (Event: %s)".formatted(event.describe(), GENERAL_APPLICATION_DIRECTIONS_MH.getCcdType()),
                    finremCaseDetails.getCaseIdAsString()
                );
            }
        }
        return submittedResponse();
    }
}
