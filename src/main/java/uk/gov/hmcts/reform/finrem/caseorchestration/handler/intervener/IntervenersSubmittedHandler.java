package uk.gov.hmcts.reform.finrem.caseorchestration.handler.intervener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.intervener.IntervenerAddedCorresponder;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_FOUR_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_ONE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_THREE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_TWO_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_FOUR_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_ONE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_THREE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_TWO_CODE;

@Slf4j
@Service
public class IntervenersSubmittedHandler extends FinremCallbackHandler {
    private final IntervenerService service;

    private final IntervenerAddedCorresponder intervenerAddedCorresponder;

    public IntervenersSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                       IntervenerService service, IntervenerAddedCorresponder intervenerAddedCorresponder) {
        super(finremCaseDetailsMapper);
        this.service =  service;
        this.intervenerAddedCorresponder = intervenerAddedCorresponder;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.MANAGE_INTERVENERS.equals(eventType));
    }


    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        Long caseId = callbackRequest.getCaseDetails().getId();
        log.info("Invoking contested event {}, callback {} callback for case id: {}",
            callbackRequest.getEventType(), CallbackType.SUBMITTED, caseId);
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        String selectedOperationCode = caseData.getIntervenerOptionList().getValueCode();

        switch (selectedOperationCode) {
            case ADD_INTERVENER_ONE_CODE -> caseData.setCurrentIntervenerChangeDetails(service.setIntervenerOneAddedChangeDetails(caseData));
            case ADD_INTERVENER_TWO_CODE -> caseData.setCurrentIntervenerChangeDetails(service.setIntervenerTwoAddedChangeDetails(caseData));
            case ADD_INTERVENER_THREE_CODE -> caseData.setCurrentIntervenerChangeDetails(service.setIntervenerThreeAddedChangeDetails(caseData));
            case ADD_INTERVENER_FOUR_CODE -> caseData.setCurrentIntervenerChangeDetails(service.setIntervenerFourAddedChangeDetails(caseData));
            case DEL_INTERVENER_ONE_CODE -> caseData.setCurrentIntervenerChangeDetails(service.setIntervenerOneRemovedChangeDetails());
            case DEL_INTERVENER_TWO_CODE -> caseData.setCurrentIntervenerChangeDetails(service.setIntervenerTwoRemovedChangeDetails());
            case DEL_INTERVENER_THREE_CODE -> caseData.setCurrentIntervenerChangeDetails(service.setIntervenerThreeRemovedChangeDetails());
            case DEL_INTERVENER_FOUR_CODE -> caseData.setCurrentIntervenerChangeDetails(service.setIntervenerFourRemovedChangeDetails());
            default -> throw new IllegalArgumentException("Invalid option received for case " + caseId);
        }

        if (caseData.getCurrentIntervenerChangeDetails().getIntervenerAction().equals(IntervenerAction.ADDED)) {
            log.info("sending Added Intervener Correspondence for case id: {}", caseId);
            intervenerAddedCorresponder.sendCorrespondence(callbackRequest.getCaseDetails(), userAuthorisation);
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(caseData).build();
        //} else if (intervenerChangeDetails.getIntervenerAction().equals(IntervenerChangeDetails.IntervenerAction.REMOVED)) {
            //intervenerRemovedCorresponder
        } else {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(caseData).build();
        }

    }



}