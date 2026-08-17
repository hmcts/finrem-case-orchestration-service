package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_VacateOrAdjournHearingAction", generate = true)
@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public enum VacateOrAdjournAction {

    @CCD(label = "Adjourn hearing")
    ADJOURN_HEARING("Adjourn_Hearing", "Adjourned"),
    @CCD(label = "Vacate hearing")
    VACATE_HEARING("Vacate_Hearing", "Vacated");

    private final String value;
    @Getter
    private final String description;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static VacateOrAdjournAction forValue(String value) {
        return Arrays.stream(VacateOrAdjournAction.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
