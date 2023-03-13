package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_FOUR_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_FOUR_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_ONE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_ONE_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_THREE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_THREE_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_TWO_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_TWO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_FOUR_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_FOUR_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_ONE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_ONE_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_THREE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_THREE_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_TWO_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_TWO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_TWO;

@Slf4j
@Service
public class IntervenersMidHandler extends FinremCallbackHandler implements IntervenerHandler {
    public IntervenersMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.MANAGE_INTERVENERS.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {

        Long caseId = callbackRequest.getCaseDetails().getId();
        log.info("Invoking contested {} about to mid callback for case id: {}",
            callbackRequest.getEventType(), caseId);

        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        FinremCaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();
        String valueCode = caseData.getIntervenersList().getValueCode();
        List<DynamicRadioListElement> dynamicListElements = new ArrayList<>();
        switch (valueCode) {
            case INTERVENER_ONE -> setIntervenerOptionList(caseDataBefore, dynamicListElements, ADD_INTERVENER_ONE_CODE,
                    ADD_INTERVENER_ONE_VALUE, DEL_INTERVENER_ONE_CODE, DEL_INTERVENER_ONE_VALUE);
            case INTERVENER_TWO -> setIntervenerOptionList(caseDataBefore, dynamicListElements, ADD_INTERVENER_TWO_CODE,
                    ADD_INTERVENER_TWO_VALUE, DEL_INTERVENER_TWO_CODE, DEL_INTERVENER_TWO_VALUE);
            case INTERVENER_THREE -> setIntervenerOptionList(caseDataBefore, dynamicListElements, ADD_INTERVENER_THREE_CODE,
                    ADD_INTERVENER_THREE_VALUE, DEL_INTERVENER_THREE_CODE, DEL_INTERVENER_THREE_VALUE);
            case INTERVENER_FOUR -> setIntervenerOptionList(caseDataBefore, dynamicListElements, ADD_INTERVENER_FOUR_CODE,
                    ADD_INTERVENER_FOUR_VALUE, DEL_INTERVENER_FOUR_CODE, DEL_INTERVENER_FOUR_VALUE);
            default -> throw new IllegalArgumentException("Invalid intervener selected for caseId " + caseId);
        }

        caseData.setIntervenerOptionList(getDynamicRadioList(dynamicListElements));
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).build();
    }

    private void setIntervenerOptionList(FinremCaseData caseData,
                                         List<DynamicRadioListElement> dynamicListElements, String addIntvCode,
                                         String addIntvValue, String delIntvCode, String delIntvValue) {
        dynamicListElements.add(getDynamicRadioListElements(addIntvCode, addIntvValue));

        switch (addIntvCode) {
            case ADD_INTERVENER_ONE_CODE -> {
                IntervenerOneWrapper intervenerOneWrapper = caseData.getIntervenerOneWrapper();
                if (intervenerOneWrapper != null && intervenerOneWrapper.getIntervener1Name() != null) {
                    dynamicListElements.add(getDynamicRadioListElements(delIntvCode, delIntvValue));
                }
            }
            case ADD_INTERVENER_TWO_CODE -> {
                IntervenerTwoWrapper intervenerTwoWrapper = caseData.getIntervenerTwoWrapper();
                if (intervenerTwoWrapper != null && intervenerTwoWrapper.getIntervener2Name() != null) {
                    dynamicListElements.add(getDynamicRadioListElements(delIntvCode, delIntvValue));
                }
            }
            case ADD_INTERVENER_THREE_CODE -> {
                IntervenerThreeWrapper intervenerThreeWrapper = caseData.getIntervenerThreeWrapper();
                if (intervenerThreeWrapper != null && intervenerThreeWrapper.getIntervener3Name() != null) {
                    dynamicListElements.add(getDynamicRadioListElements(delIntvCode, delIntvValue));
                }
            }
            case ADD_INTERVENER_FOUR_CODE -> {
                IntervenerFourWrapper intervenerFourWrapper = caseData.getIntervenerFourWrapper();
                if (intervenerFourWrapper != null && intervenerFourWrapper.getIntervener4Name() != null) {
                    dynamicListElements.add(getDynamicRadioListElements(delIntvCode, delIntvValue));
                }
            }
            default -> throw new IllegalArgumentException("Invalid intervener selected for caseId ");
        }
    }

}