package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
