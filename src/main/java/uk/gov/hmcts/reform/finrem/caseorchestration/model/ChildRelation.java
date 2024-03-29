package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum ChildRelation {

    MOTHER("Mother"),
    FATHER("Father"),
    STEP_MOTHER("Step mother"),
    STEP_FATHER("Step father"),
    GRAND_PARENT("Grand parent"),
    GUARDIAN("Guardian"),
    SPECIAL_GUARDIAN("Special Guardian"),
    OTHER("Other");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ChildRelation forValue(String value) {
        return Arrays.stream(ChildRelation.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
