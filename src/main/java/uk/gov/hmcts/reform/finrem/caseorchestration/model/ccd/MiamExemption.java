package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_ms_MIAMExemptionsChecklist", generate = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum MiamExemption {
    @CCD(label = "Domestic abuse")
    DOMESTIC_VIOLENCE("domesticViolence", "Domestic violence"),
    @CCD(label = "Urgency")
    URGENCY("urgency", "Urgency"),
    @CCD(label = "Previous attendance of a MIAM or non-court dispute resolution")
    PREVIOUS_MIAM_ATTENDANCE("previousMIAMattendance", "Previous MIAM attendance or previous MIAM exemption"),
    @CCD(label = "Other")
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
