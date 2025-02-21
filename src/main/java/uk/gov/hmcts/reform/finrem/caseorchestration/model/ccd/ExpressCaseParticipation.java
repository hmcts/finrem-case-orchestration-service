package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum ExpressCaseParticipation {

    ENROLLED("Enrolled"),
    DOES_NOT_QUALIFY("Does not qualify"),
    WITHDRAWN("Withdrawn");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ExpressCaseParticipation forValue(String value) {
        return Arrays.stream(ExpressCaseParticipation.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
