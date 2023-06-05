package uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener;

public enum IntervenerType {
    INTERVENER_ONE("intervener1"),
    INTERVENER_TWO("intervener2"),
    INTERVENER_THREE("intervener3"),
    INTERVENER_FOUR("intervener4");

    private String value;

    IntervenerType(String value) {
        this.value = value;
    }

    public String getTypeValue() {
        return this.value;
    }
}
