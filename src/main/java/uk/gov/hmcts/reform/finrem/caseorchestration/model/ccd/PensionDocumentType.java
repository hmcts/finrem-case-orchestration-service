package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum PensionDocumentType {
    FORM_P1("Form P1"),
    FORM_P2("Form P2"),
    FORM_PPF("Form PPF"),
    FORM_PPF1("Form PPF1"),
    FORM_PPF2("Form PPF2");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static PensionDocumentType forValue(String value) {
        return Arrays.stream(PensionDocumentType.values())
            .filter(option -> value.equalsIgnoreCase(option.getValue()))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
