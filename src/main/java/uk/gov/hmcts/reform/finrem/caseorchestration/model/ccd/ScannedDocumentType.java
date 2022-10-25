package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum ScannedDocumentType {
    CHERISHED("cherished"),
    OTHER("other"),
    FORM("form"),
    COVERSHEET("coversheet");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ScannedDocumentType forValue(String value) {
        return Arrays.stream(ScannedDocumentType.values())
            .filter(option -> value.equalsIgnoreCase(option.getValue()))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
