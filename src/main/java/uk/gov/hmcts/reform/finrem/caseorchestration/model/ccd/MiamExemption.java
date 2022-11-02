package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum MiamExemption {
    DOMESTIC_VIOLENCE("domesticViolence", "Domestic violence"),
    URGENCY("urgency", "Urgency"),
    PREVIOUS_MIAM_ATTENDANCE("previousMIAMattendance", "Previous MIAM attendance or previous MIAM exemption"),
    OTHER("other", "Other");

    private final String value;
    private final String text;

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public static MiamExemption forValue(String value) {
        return Arrays.stream(MiamExemption.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
