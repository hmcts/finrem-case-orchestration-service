package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;

import java.util.List;

public interface IntervenerHandler {

    default DynamicRadioListElement getDynamicRadioListElements(String code, String label) {
        return DynamicRadioListElement.builder()
            .code(code)
            .label(label)
            .build();
    }

    default DynamicRadioList getDynamicRadioList(List<DynamicRadioListElement> dynamicRadioListElement) {
        return DynamicRadioList.builder()
            .value(dynamicRadioListElement.get(0))
            .listItems(dynamicRadioListElement)
            .build();
    }
}
