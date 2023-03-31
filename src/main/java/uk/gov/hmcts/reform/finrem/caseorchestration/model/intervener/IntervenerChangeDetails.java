package uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IntervenerChangeDetails {

    public enum IntervenerType {
        INTERVENER_ONE,
        INTERVENER_TWO,
        INTERVENER_THREE,
        INTERVENER_FOUR
    }

    public enum IntervenerAction {
        ADDED,
        REMOVED,
        ADDED_SOL
    }

    private final IntervenerType intervenerType;
    private final IntervenerAction intervenerAction;

    private IntervenerDetails intervenerDetails;

    public IntervenerType getIntervenerType() {
        return intervenerType;
    }

    public IntervenerAction getIntervenerAction() {
        return intervenerAction;
    }

    public IntervenerDetails getIntervenerDetails() {
        return intervenerDetails;
    }

    public void setIntervenerDetails(IntervenerDetails intervenerDetails) {
        this.intervenerDetails = intervenerDetails;
    }
}
