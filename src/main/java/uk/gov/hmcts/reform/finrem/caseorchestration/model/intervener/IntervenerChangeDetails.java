package uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerDetails;

@RequiredArgsConstructor
public class IntervenerChangeDetails {
    private IntervenerType intervenerType;
    private IntervenerAction intervenerAction;
    private IntervenerDetails intervenerDetails;

    public IntervenerType getIntervenerType() {
        return intervenerType;
    }

    public void setIntervenerType(IntervenerType intervenerType) {
        this.intervenerType = intervenerType;
    }

    public IntervenerAction getIntervenerAction() {
        return intervenerAction;
    }

    public void setIntervenerAction(IntervenerAction intervenerAction) {
        this.intervenerAction = intervenerAction;
    }

    public IntervenerDetails getIntervenerDetails() {
        return intervenerDetails;
    }

    public void setIntervenerDetails(IntervenerDetails intervenerDetails) {
        this.intervenerDetails = intervenerDetails;
    }
}
