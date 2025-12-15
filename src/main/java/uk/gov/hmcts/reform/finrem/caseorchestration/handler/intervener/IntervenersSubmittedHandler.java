package uk.gov.hmcts.reform.finrem.caseorchestration.handler.intervener;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.intervener.IntervenerAddedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.intervener.IntervenerRemovedCorresponder;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_FOUR_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_ONE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_THREE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_TWO_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_FOUR_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_ONE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_THREE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_TWO_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_TWO;

@Slf4j
@Service
public class IntervenersSubmittedHandler extends FinremCallbackHandler {
    private final IntervenerService intervenerService;
    private final IntervenerAddedCorresponder intervenerAddedCorresponder;
    private final IntervenerRemovedCorresponder intervenerRemovedCorresponder;

    public IntervenersSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                       IntervenerService intervenerService, IntervenerAddedCorresponder intervenerAddedCorresponder,
                                       IntervenerRemovedCorresponder intervenerRemovedCorresponder) {
        super(finremCaseDetailsMapper);
        this.intervenerService = intervenerService;
        this.intervenerAddedCorresponder = intervenerAddedCorresponder;
        this.intervenerRemovedCorresponder = intervenerRemovedCorresponder;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.MANAGE_INTERVENERS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));
        Long caseId = callbackRequest.getCaseDetails().getId();

        FinremCaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        String intervenerType = caseData.getIntervenersList().getValueCode();
        String selectedOperationCode;
        boolean wasIntervenerSolicitorRemoved = intervenerService.checkIfAnyIntervenerSolicitorRemoved(caseData, caseDataBefore);

        switch (intervenerType) {
            case INTERVENER_ONE -> selectedOperationCode =
                wasIntervenerSolicitorRemoved ? DEL_INTERVENER_ONE_CODE :
                    caseData.getIntervenerOptionList().getValueCode();
            case INTERVENER_TWO -> selectedOperationCode =
                wasIntervenerSolicitorRemoved ? DEL_INTERVENER_TWO_CODE :
                    caseData.getIntervenerOptionList().getValueCode();
            case INTERVENER_THREE -> selectedOperationCode =
                wasIntervenerSolicitorRemoved ? DEL_INTERVENER_THREE_CODE :
                    caseData.getIntervenerOptionList().getValueCode();
            case INTERVENER_FOUR -> selectedOperationCode =
                wasIntervenerSolicitorRemoved ? DEL_INTERVENER_FOUR_CODE :
                    caseData.getIntervenerOptionList().getValueCode();
            default -> selectedOperationCode = caseData.getIntervenerOptionList().getValueCode();
        }

        switch (selectedOperationCode) {
            case ADD_INTERVENER_ONE_CODE ->
                caseData.setCurrentIntervenerChangeDetails(intervenerService.setIntervenerAddedChangeDetails(caseData.getIntervenerOne()));
            case ADD_INTERVENER_TWO_CODE ->
                caseData.setCurrentIntervenerChangeDetails(intervenerService.setIntervenerAddedChangeDetails(caseData.getIntervenerTwo()));
            case ADD_INTERVENER_THREE_CODE ->
                caseData.setCurrentIntervenerChangeDetails(intervenerService.setIntervenerAddedChangeDetails(caseData.getIntervenerThree()));
            case ADD_INTERVENER_FOUR_CODE ->
                caseData.setCurrentIntervenerChangeDetails(intervenerService.setIntervenerAddedChangeDetails(caseData.getIntervenerFour()));
            case DEL_INTERVENER_ONE_CODE ->
                caseData.setCurrentIntervenerChangeDetails(intervenerService.setIntervenerRemovedChangeDetails(caseDataBefore.getIntervenerOne()));
            case DEL_INTERVENER_TWO_CODE ->
                caseData.setCurrentIntervenerChangeDetails(intervenerService.setIntervenerRemovedChangeDetails(caseDataBefore.getIntervenerTwo()));
            case DEL_INTERVENER_THREE_CODE ->
                caseData.setCurrentIntervenerChangeDetails(intervenerService.setIntervenerRemovedChangeDetails(caseDataBefore.getIntervenerThree()));
            case DEL_INTERVENER_FOUR_CODE ->
                caseData.setCurrentIntervenerChangeDetails(intervenerService.setIntervenerRemovedChangeDetails(caseDataBefore.getIntervenerFour()));
            default -> throw new IllegalArgumentException("Invalid option received for case " + caseId);
        }

        if (caseData.getCurrentIntervenerChangeDetails().getIntervenerAction().equals(IntervenerAction.ADDED)) {
            log.info("sending Added Intervener Correspondence for case id: {}", caseId);
            intervenerAddedCorresponder.sendCorrespondence(callbackRequest.getCaseDetails(), userAuthorisation);

        } else if (caseData.getCurrentIntervenerChangeDetails().getIntervenerAction().equals(IntervenerAction.REMOVED)) {
            log.info("sending Removed Intervener Correspondence for case id: {}", caseId);
            intervenerRemovedCorresponder.sendCorrespondence(callbackRequest.getCaseDetails(), userAuthorisation);
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).build();
    }
}
