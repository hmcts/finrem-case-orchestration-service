package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
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

    public static Schedule1OrMatrimonialAndCpList forValue(String value) {
        return Arrays.stream(Schedule1OrMatrimonialAndCpList.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
