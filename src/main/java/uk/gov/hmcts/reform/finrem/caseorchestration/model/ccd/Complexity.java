package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_complexityList", generate = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum Complexity {
    @CCD(label = "Yes")
    TRUE_YES("trueYes"),
    @CCD(label = "No")
    FALSE_NO("falseNo"),
    @CCD(label = "I Don't Know")
    TRUE_DONT_KNOW("trueDontKnow");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static Complexity forValue(String value) {
        return Arrays.stream(Complexity.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
