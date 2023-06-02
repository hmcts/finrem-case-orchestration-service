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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;

import java.util.ArrayList;
import java.util.List;

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

        List<IntervenerWrapper> interveners = caseData.getInterveners();
        interveners.forEach(intervenerWrapper -> {

            buildDynamicIntervenerList(dynamicListElements, intervenerWrapper);
        });

        DynamicRadioList dynamicList = getDynamicRadioList(dynamicListElements);
        caseData.setIntervenersList(dynamicList);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).build();
    }

    private void buildDynamicIntervenerList(List<DynamicRadioListElement> dynamicListElements, IntervenerWrapper intervenerWrapper) {
        if (intervenerWrapper.getIntervenerName() != null) {
            var label = intervenerWrapper.getIntervenerLabel() + ": "
                + intervenerWrapper.getIntervenerName();
            dynamicListElements.add(getDynamicRadioListElements(intervenerWrapper.getIntervenerType().getTypeValue(), label));
        } else {
            dynamicListElements.add(
                getDynamicRadioListElements(intervenerWrapper.getIntervenerType().getTypeValue(),
                    intervenerWrapper.getIntervenerLabel() + ": " + DEFAULT));
        }
    }

}
