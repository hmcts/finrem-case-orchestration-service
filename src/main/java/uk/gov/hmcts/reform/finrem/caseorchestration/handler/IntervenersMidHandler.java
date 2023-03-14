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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.UPDATE_INTERVENER_FOUR_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.UPDATE_INTERVENER_ONE_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.UPDATE_INTERVENER_THREE_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.UPDATE_INTERVENER_TWO_VALUE;

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
            case INTERVENER_ONE -> showIntervenerOneOption(caseDataBefore, dynamicListElements);
            case INTERVENER_TWO -> showIntervenerTwoOption(caseDataBefore, dynamicListElements);
            case INTERVENER_THREE -> showIntervenerThreeOption(caseDataBefore, dynamicListElements);
            case INTERVENER_FOUR -> showIntervenerFourOption(caseDataBefore, dynamicListElements);
            default -> throw new IllegalArgumentException("Invalid intervener selected for caseId " + caseId);
        }

        caseData.setIntervenerOptionList(getDynamicRadioList(dynamicListElements));
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).build();
    }

    private void showIntervenerFourOption(FinremCaseData caseData, List<DynamicRadioListElement> dynamicListElements) {
        IntervenerFourWrapper intervenerFourWrapper = caseData.getIntervenerFourWrapper();
        if (intervenerFourWrapper != null && intervenerFourWrapper.getIntervener4Name() != null) {
            dynamicListElements.add(getDynamicRadioListElements(ADD_INTERVENER_FOUR_CODE, UPDATE_INTERVENER_FOUR_VALUE));
            dynamicListElements.add(getDynamicRadioListElements(DEL_INTERVENER_FOUR_CODE, DEL_INTERVENER_FOUR_VALUE));
        } else {
            dynamicListElements.add(getDynamicRadioListElements(ADD_INTERVENER_FOUR_CODE, ADD_INTERVENER_FOUR_VALUE));
        }
    }

    private void showIntervenerThreeOption(FinremCaseData caseData, List<DynamicRadioListElement> dynamicListElements) {
        IntervenerThreeWrapper intervenerThreeWrapper = caseData.getIntervenerThreeWrapper();
        if (intervenerThreeWrapper != null && intervenerThreeWrapper.getIntervener3Name() != null) {
            dynamicListElements.add(getDynamicRadioListElements(ADD_INTERVENER_THREE_CODE, UPDATE_INTERVENER_THREE_VALUE));
            dynamicListElements.add(getDynamicRadioListElements(DEL_INTERVENER_THREE_CODE, DEL_INTERVENER_THREE_VALUE));
        } else {
            dynamicListElements.add(getDynamicRadioListElements(ADD_INTERVENER_THREE_CODE, ADD_INTERVENER_THREE_VALUE));
        }
    }

    private void showIntervenerTwoOption(FinremCaseData caseData, List<DynamicRadioListElement> dynamicListElements) {
        IntervenerTwoWrapper intervenerTwoWrapper = caseData.getIntervenerTwoWrapper();
        if (intervenerTwoWrapper != null && intervenerTwoWrapper.getIntervener2Name() != null) {
            dynamicListElements.add(getDynamicRadioListElements(ADD_INTERVENER_TWO_CODE, UPDATE_INTERVENER_TWO_VALUE));
            dynamicListElements.add(getDynamicRadioListElements(DEL_INTERVENER_TWO_CODE, DEL_INTERVENER_TWO_VALUE));
        } else {
            dynamicListElements.add(getDynamicRadioListElements(ADD_INTERVENER_TWO_CODE, ADD_INTERVENER_TWO_VALUE));
        }
    }

    private void showIntervenerOneOption(FinremCaseData caseData, List<DynamicRadioListElement> dynamicListElements) {
        IntervenerOneWrapper intervenerOneWrapper = caseData.getIntervenerOneWrapper();
        if (intervenerOneWrapper != null && intervenerOneWrapper.getIntervener1Name() != null) {
            dynamicListElements.add(getDynamicRadioListElements(ADD_INTERVENER_ONE_CODE, UPDATE_INTERVENER_ONE_VALUE));
            dynamicListElements.add(getDynamicRadioListElements(DEL_INTERVENER_ONE_CODE, DEL_INTERVENER_ONE_VALUE));
        } else {
            dynamicListElements.add(getDynamicRadioListElements(ADD_INTERVENER_ONE_CODE, ADD_INTERVENER_ONE_VALUE));
        }
    }

}