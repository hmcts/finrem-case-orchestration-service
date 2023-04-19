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
public class IntervenersAboutToSubmitHandler extends FinremCallbackHandler {
    private final IntervenerService service;

    public IntervenersAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                           IntervenerService service) {
        super(finremCaseDetailsMapper);
        this.service =  service;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.MANAGE_INTERVENERS.equals(eventType));
    }


    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        Long caseId = callbackRequest.getCaseDetails().getId();
        log.info("Invoking contested event {}, callback {} callback for case id: {}",
            callbackRequest.getEventType(), CallbackType.ABOUT_TO_SUBMIT, caseId);
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        String selectedOperationCode = caseData.getIntervenerOptionList().getValueCode();
        log.info("selected operation choice {} for intervener {} for case id: {}",
            selectedOperationCode, caseData.getIntervenersList().getValueCode(), caseId);

        switch (selectedOperationCode) {
            case ADD_INTERVENER_ONE_CODE -> service.updateIntervenerOneDetails(callbackRequest);
            case ADD_INTERVENER_TWO_CODE -> service.updateIntervenerTwoDetails(callbackRequest);
            case ADD_INTERVENER_THREE_CODE -> service.updateIntervenerThreeDetails(callbackRequest);
            case ADD_INTERVENER_FOUR_CODE -> service.updateIntervenerFourDetails(callbackRequest);
            case DEL_INTERVENER_ONE_CODE -> service.removeIntervenerOneDetails(caseData, caseId);
            case DEL_INTERVENER_TWO_CODE -> service.removeIntervenerTwoDetails(caseData, caseId);
            case DEL_INTERVENER_THREE_CODE -> service.removeIntervenerThreeDetails(caseData, caseId);
            case DEL_INTERVENER_FOUR_CODE -> service.removeIntervenerFourDetails(caseData, caseId);
            default -> throw new IllegalArgumentException("Invalid option received for case " + caseId);
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).build();
    }



}