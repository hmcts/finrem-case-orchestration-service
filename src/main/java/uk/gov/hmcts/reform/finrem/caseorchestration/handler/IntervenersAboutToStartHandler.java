package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ADDED_TO_CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.MORE_DETAILS;

@Slf4j
@Service
public class IntervenersAboutToStartHandler extends FinremCallbackHandler implements IntervenerHandler {
    private static final String DEFAULT = "not added to case yet.";
    public IntervenersAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.MANAGE_INTERVENERS.equals(eventType));
    }


    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info("Invoking contested {} about to start callback for case id: {}",
            callbackRequest.getEventType(), callbackRequest.getCaseDetails().getId());

        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        List<DynamicRadioListElement> dynamicListElements = new ArrayList<>();
        IntervenerOneWrapper intervenerOneWrapper = caseData.getIntervenerOneWrapper();
        if (intervenerOneWrapper.getIntervener1Name() != null) {
            var label =  INTERVENER_ONE + " - "
                + intervenerOneWrapper.getIntervener1Name()
                + " - " + INTERVENER_ADDED_TO_CASE + " - "
                + intervenerOneWrapper.getIntervener1DateAdded() + " - "
                + MORE_DETAILS + INTERVENER_ONE + " Tab";;
            dynamicListElements.add(getDynamicRadioListElements(INTERVENER_ONE, label));
        } else {
            dynamicListElements.add(getDynamicRadioListElements(INTERVENER_ONE, INTERVENER_ONE+" "+DEFAULT));
        }

        IntervenerTwoWrapper intervenerTwoWrapper = caseData.getIntervenerTwoWrapper();
        if (intervenerTwoWrapper.getIntervener2Name() != null) {
            var label =  INTERVENER_TWO + " - "
                + intervenerTwoWrapper.getIntervener2Name()
                + " - " + INTERVENER_ADDED_TO_CASE + " - "
                + intervenerTwoWrapper.getIntervener2DateAdded() + " - "
                + MORE_DETAILS + INTERVENER_TWO + " Tab";
            dynamicListElements.add(getDynamicRadioListElements(INTERVENER_TWO, label));
        } else {
            dynamicListElements.add(getDynamicRadioListElements(INTERVENER_TWO, INTERVENER_TWO+" "+DEFAULT));
        }

        IntervenerThreeWrapper intervenerThreeWrapper = caseData.getIntervenerThreeWrapper();
        if (intervenerThreeWrapper.getIntervener3Name() != null) {
            var label =  INTERVENER_THREE + " - "
                + intervenerThreeWrapper.getIntervener3Name()
                + " - " + INTERVENER_ADDED_TO_CASE + " - "
                + intervenerThreeWrapper.getIntervener3DateAdded() + " - "
                + MORE_DETAILS + INTERVENER_THREE + " Tab";
            dynamicListElements.add(getDynamicRadioListElements(INTERVENER_THREE, label));
        } else {
            dynamicListElements.add(getDynamicRadioListElements(INTERVENER_THREE, INTERVENER_THREE +" "+DEFAULT));
        }

        IntervenerFourWrapper intervenerFourWrapper = caseData.getIntervenerFourWrapper();
        if (intervenerFourWrapper.getIntervener4Name() != null) {
            var label =  INTERVENER_FOUR + " - "
                + intervenerFourWrapper.getIntervener4Name()
                + " - " + INTERVENER_ADDED_TO_CASE + " - "
                + intervenerFourWrapper.getIntervener4DateAdded() + " - "
                + MORE_DETAILS + INTERVENER_FOUR + " Tab";;
            dynamicListElements.add(getDynamicRadioListElements(INTERVENER_FOUR, label));
        } else {
            dynamicListElements.add(getDynamicRadioListElements(INTERVENER_FOUR, INTERVENER_FOUR +" "+ DEFAULT));
        }

        DynamicRadioList dynamicList = getDynamicRadioList(dynamicListElements);
        caseData.setIntervenersList(dynamicList);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).build();
    }

}