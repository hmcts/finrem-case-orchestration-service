package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum CaseDocumentParty {
    APPLICANT("applicant"),
    RESPONDENT("respondent"),
    INTERVENER_ONE("intervener1"),
    INTERVENER_TWO("intervener2"),
    INTERVENER_THREE("intervener3"),
    INTERVENER_FOUR("intervener4"),
    CASE("case");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static CaseDocumentParty forValue(String value) {
        return Arrays.stream(CaseDocumentParty.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
