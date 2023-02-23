package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum IntervenerFourOption {
    ADD_INTERVENER_FOUR("addIntervener4"),
    DELETE_INTERVENER_FOUR("deleteIntervener4");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static IntervenerFourOption forValue(String value) {
        return java.util.Arrays.stream(IntervenerFourOption.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
