package uk.gov.hmcts.reform.finrem.caseorchestration.handler.generalapplicationdirections;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing.ManageHearingsCorresponder;

@Slf4j
@Service
public class GeneralApplicationDirectionsSubmittedHandler extends FinremCallbackHandler {

    private final ManageHearingsCorresponder manageHearingsCorresponder;
    private final GeneralApplicationDirectionsService generalApplicationDirectionsService;

    public GeneralApplicationDirectionsSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                        ManageHearingsCorresponder manageHearingsCorresponder,
                                                        GeneralApplicationDirectionsService generalApplicationDirectionsService) {
        super(finremCaseDetailsMapper);
        this.manageHearingsCorresponder = manageHearingsCorresponder;
        this.generalApplicationDirectionsService = generalApplicationDirectionsService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.GENERAL_APPLICATION_DIRECTIONS_MH.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));

        // Hearings are optional, so send hearing correspondence if a hearing was added in the event.
        if (generalApplicationDirectionsService.isHearingRequired(callbackRequest.getCaseDetails())) {
            manageHearingsCorresponder.sendHearingCorrespondence(callbackRequest, userAuthorisation);
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(callbackRequest.getCaseDetails().getData())
            .build();
    }
}
