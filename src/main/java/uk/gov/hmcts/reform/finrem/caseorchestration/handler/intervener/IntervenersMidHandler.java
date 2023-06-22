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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;

import java.util.ArrayList;
import java.util.List;

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
            case INTERVENER_ONE -> showIntervenerOption(caseDataBefore.getIntervenerOneWrapper(), dynamicListElements);
            case INTERVENER_TWO -> showIntervenerOption(caseDataBefore.getIntervenerTwoWrapper(), dynamicListElements);
            case INTERVENER_THREE -> showIntervenerOption(caseDataBefore.getIntervenerThreeWrapper(), dynamicListElements);
            case INTERVENER_FOUR -> showIntervenerOption(caseDataBefore.getIntervenerFourWrapper(), dynamicListElements);
            default -> throw new IllegalArgumentException("Invalid intervener selected for caseId " + caseId);
        }

        caseData.setIntervenerOptionList(getDynamicRadioList(dynamicListElements));
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).build();
    }

    private void showIntervenerOption(IntervenerWrapper intervenerWrapper, List<DynamicRadioListElement> dynamicListElements) {
        if (intervenerWrapper != null) {
            if (intervenerWrapper.getIntervenerName() != null) {
                dynamicListElements.add(getDynamicRadioListElements(intervenerWrapper.getAddIntervenerCode(),
                    intervenerWrapper.getUpdateIntervenerValue()));
                dynamicListElements.add(getDynamicRadioListElements(intervenerWrapper.getDeleteIntervenerCode(),
                    intervenerWrapper.getDeleteIntervenerValue()));
            } else {
                dynamicListElements.add(
                    getDynamicRadioListElements(intervenerWrapper.getAddIntervenerCode(), intervenerWrapper.getAddIntervenerValue()));
            }
        }
    }
}
