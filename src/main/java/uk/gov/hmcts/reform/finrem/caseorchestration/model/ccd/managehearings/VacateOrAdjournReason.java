package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public enum VacateOrAdjournReason {
    CASE_NOT_READY("Case_Not_Ready"),
    CASE_SETTLED("Case_Settled"),
    CASE_REP_UNAVAILABLE("Legal_Rep_Unavailable"),
    COURTROOM_UNAVAILABLE("Courtroom_Unavailable"),
    SPECIAL_MEASURES_NOT_AVAILABLE("Special_Measures_Not_Available"),
    INTERPRETER_UNAVAILABLE("Interpreter_Unavailable"),
    JUDGE_UNAVAILABLE("Judge_Unavailable"),
    INSUFFICIENT_TIME("Insufficient_Time"),
    CASE_STAYED("Case_Stayed"),
    ADJOURNED("Adjourned"),
    CASE_TRANSFERRED("Case_Transferred"),
    OTHER("Other");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    private static VacateOrAdjournReason forValue(String value) {
        return Arrays.stream(VacateOrAdjournReason.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
