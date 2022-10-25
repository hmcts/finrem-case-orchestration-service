package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum JudgeTimeEstimate {
    STANDARD_TIME("standardTime"),
    ADDITIONAL_TIME("additionalTime");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static JudgeTimeEstimate forValue(String value) {
        return Arrays.stream(JudgeTimeEstimate.values())
            .filter(option -> value.equalsIgnoreCase(option.getValue()))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
