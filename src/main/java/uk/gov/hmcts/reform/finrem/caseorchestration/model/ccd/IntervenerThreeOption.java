package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum IntervenerThreeOption {
    ADD_INTERVENER_THREE("addIntervener3"),
    DELETE_INTERVENER_THREE("deleteIntervener3");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static IntervenerThreeOption forValue(String value) {
        return java.util.Arrays.stream(IntervenerThreeOption.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
