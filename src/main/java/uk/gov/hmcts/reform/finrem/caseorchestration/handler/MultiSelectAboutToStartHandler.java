package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MultiSelectAboutToStartHandler extends FinremCallbackHandler {

    public MultiSelectAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.MULTI_SELECT.equals(eventType));
    }


    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info("Handling contested {} about to start callback for case id: {}",
            callbackRequest.getEventType(), callbackRequest.getCaseDetails().getId());


        List<DynamicMultiSelectListElement> dynamicListElements = new ArrayList<>();

        dynamicListElements.add(getDynamicMultiSelectListElement("doc1.pdf", "doc1.pdf"));
        dynamicListElements.add(getDynamicMultiSelectListElement("doc2.pdf", "doc2.pdf"));
        dynamicListElements.add(getDynamicMultiSelectListElement("doc3.pdf", "doc3.pdf"));
        dynamicListElements.add(getDynamicMultiSelectListElement("doc4.pdf", "doc4.pdf"));
        dynamicListElements.add(getDynamicMultiSelectListElement("doc5.pdf", "doc5.pdf"));
        dynamicListElements.add(getDynamicMultiSelectListElement("doc6.pdf", "doc6.pdf"));

        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        DynamicMultiSelectList dynamicList = getDynamicMultiSelectList(dynamicListElements);
        caseData.setIntervenerDocuments(dynamicList);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(callbackRequest.getCaseDetails().getData()).build();
    }

    private DynamicMultiSelectListElement getDynamicMultiSelectListElement(String code, String label) {
        return DynamicMultiSelectListElement.builder()
            .code(code)
            .label(label)
            .build();
    }

    private DynamicMultiSelectList getDynamicMultiSelectList(List<DynamicMultiSelectListElement> dynamicMultiSelectListElement) {
        return DynamicMultiSelectList.builder()
            .value(dynamicMultiSelectListElement)
            .listItems(dynamicMultiSelectListElement)
            .build();
    }
}