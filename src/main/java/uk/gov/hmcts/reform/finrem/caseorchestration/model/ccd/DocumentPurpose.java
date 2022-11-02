package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum DocumentPurpose {
    DRAFT_ORDER("Draft order "),
    RESUBMITTED_DRAFT_ORDER("Resubmitted Draft Order");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static DocumentPurpose forValue(String value) {
        return Arrays.stream(DocumentPurpose.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
