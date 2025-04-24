package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ManageHearingsAction {
    ADD_HEARING("addHearing");

    @JsonValue
    private final String value;

    public static ManageHearingsAction forValue(String value) {
        for (ManageHearingsAction action : values()) {
            if (action.value.equals(value)) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
