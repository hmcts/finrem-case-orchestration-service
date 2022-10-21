package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum VariationTypeOfDocument {

    ORIGINAL_ORDER("originalOrder"),
    OTHER_DOCUMENTS("Other");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static VariationTypeOfDocument forValue(String value) {
        return Arrays.stream(VariationTypeOfDocument.values())
            .filter(option -> value.equalsIgnoreCase(option.getValue()))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
