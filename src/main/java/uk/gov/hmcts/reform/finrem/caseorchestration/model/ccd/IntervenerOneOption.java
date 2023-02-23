package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum IntervenerOneOption {
    ADD_INTERVENER_ONE("addIntervener1"),
    DELETE_INTERVENER_ONE("deleteIntervener1");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static IntervenerOneOption forValue(String value) {
        return java.util.Arrays.stream(IntervenerOneOption.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
