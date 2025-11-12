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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingActionService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ContestedStatus.PREPARE_FOR_HEARING;

@Slf4j
@Service
public class ManageHearingsAboutToSubmitHandler  extends FinremCallbackHandler {

    private final ManageHearingActionService manageHearingActionService;

    public ManageHearingsAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                              ManageHearingActionService manageHearingActionService) {
        super(finremCaseDetailsMapper);
        this.manageHearingActionService = manageHearingActionService;
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

        ManageHearingsAction actionSelection = finremCaseData
            .getManageHearingsWrapper()
            .getManageHearingsActionSelection();

        if (ManageHearingsAction.ADD_HEARING.equals(actionSelection)) {
            manageHearingActionService.performAddHearing(finremCaseDetails, userAuthorisation);
            finremCaseData.setState(PREPARE_FOR_HEARING.getId());
        }
        else if (ManageHearingsAction.VACATE_HEARING.equals(actionSelection)) {
            manageHearingActionService.performVacateHearing(finremCaseDetails);
        }

        manageHearingActionService.updateTabData(finremCaseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData).build();
    }
}
