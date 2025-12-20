package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum BarristerParty {
    APPLICANT("applicant"),
    RESPONDENT("respondent"),
    INTERVENER1("intervener1"),
    INTERVENER2("intervener2"),
    INTERVENER3("intervener3"),
    INTERVENER4("intervener4");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static BarristerParty forValue(String value) {
        return java.util.Arrays.stream(BarristerParty.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    /**
     * Returns the {@link BarristerParty} for the specified intervener index.
     *
     * <p>The index represents the intervener number (for example, 1–4).</p>
     *
     * @param index the intervener index
     * @return the corresponding {@link BarristerParty}
     */
    public static BarristerParty getIntervenerBarristerByIndex(int index) {
        return forValue("intervener" + index);
    }
}
