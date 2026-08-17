package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum ScannedDocumentType {
    @CCD(label = "Cherished")
    CHERISHED("cherished"),
    @CCD(label = "Other")
    OTHER("other"),
    @CCD(label = "Form")
    FORM("form"),
    @CCD(label = "Coversheet")
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
