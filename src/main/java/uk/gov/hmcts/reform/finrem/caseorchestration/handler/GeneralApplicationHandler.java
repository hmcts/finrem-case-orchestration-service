package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;

import java.util.List;

public interface GeneralApplicationHandler {
    default String getLabel(GeneralApplicationItems items, int index) {
        return "General Application "
            + index
            + " - Received from - "
            + (items.getGeneralApplicationReceivedFrom() != null
            && !items.getGeneralApplicationReceivedFrom().isEmpty()
            ? items.getGeneralApplicationReceivedFrom()
            : items.getGeneralApplicationSender().getValue().getCode())
            + " - Created Date - "
            + items.getGeneralApplicationCreatedDate()
            + " - Hearing Required - "
            + items.getGeneralApplicationHearingRequired();
    }

    default DynamicListElement getDynamicListElements(String code, String label) {
        return DynamicListElement.builder()
            .code(code)
            .label(label)
            .build();
    }

    default DynamicList generateAvailableGeneralApplicationAsDynamicList(List<DynamicListElement> dynamicListElement) {
        return DynamicList.builder()
            .value(dynamicListElement.get(0))
            .listItems(dynamicListElement)
            .build();
    }
}
