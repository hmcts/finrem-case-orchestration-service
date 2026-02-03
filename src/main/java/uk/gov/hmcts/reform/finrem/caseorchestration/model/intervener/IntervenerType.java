package uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener;

import lombok.Getter;

@Getter
public enum IntervenerType {
    INTERVENER_ONE("intervener1", 1),
    INTERVENER_TWO("intervener2", 2),
    INTERVENER_THREE("intervener3", 3),
    INTERVENER_FOUR("intervener4", 4);

    private final String typeValue;

    private final int intervenerId;

    IntervenerType(String typeValue, int intervenerId) {
        this.typeValue = typeValue;
        this.intervenerId = intervenerId;
    }
}
