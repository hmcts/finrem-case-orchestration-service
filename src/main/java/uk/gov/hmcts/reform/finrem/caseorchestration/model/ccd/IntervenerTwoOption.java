package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum IntervenerTwoOption {
    ADD_INTERVENER_TWO("addIntervener2"),
    DELETE_INTERVENER_TWO("deleteIntervener2");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static IntervenerTwoOption forValue(String value) {
        return java.util.Arrays.stream(IntervenerTwoOption.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
