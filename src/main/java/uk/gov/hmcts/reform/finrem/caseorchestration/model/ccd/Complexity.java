package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum Complexity {
    TRUE_YES("trueYes"),
    FALSE_NO("falseNo"),
    TRUE_DONT_KNOW("trueDontKnow");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static Complexity forValue(String value) {
        return Arrays.stream(Complexity.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
