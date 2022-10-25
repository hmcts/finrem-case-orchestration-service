package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum NatureApplication5b {
    FR_NATURE_OF_APPLICATION_1("FR_nature_of_application_1"),
    FR_NATURE_OF_APPLICATION_2("FR_nature_of_application_2"),
    FR_NATURE_OF_APPLICATION_3("FR_nature_of_application_3");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static NatureApplication5b forValue(String value) {
        return Arrays.stream(NatureApplication5b.values())
            .filter(option -> value.equalsIgnoreCase(option.getValue()))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
