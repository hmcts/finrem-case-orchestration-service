package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public enum VacateOrAdjournReason {
    CASE_NOT_READY("Case_Not_Ready", "Parties - Parties/case not ready to proceed"),
    CASE_SETTLED("Case_Settled", "Parties - Case settled, order/application made"),
    CASE_REP_UNAVAILABLE("Legal_Rep_Unavailable", "Parties - Parties/legal representatives not available"),
    COURTROOM_UNAVAILABLE("Courtroom_Unavailable", "HMCTS - No courtroom available"),
    SPECIAL_MEASURES_NOT_AVAILABLE("Special_Measures_Not_Available", "HMCTS - Special measures not available"),
    INTERPRETER_UNAVAILABLE("Interpreter_Unavailable", "HMCTS - Interpreter not available"),
    JUDGE_UNAVAILABLE("Judge_Unavailable", "Judiciary - Lack of Judicial availability"),
    INSUFFICIENT_TIME("Insufficient_Time", "Judiciary - Insufficient time listed or to complete hearing"),
    CASE_STAYED("Case_Stayed", "Other - Case stayed"),
    ADJOURNED("Adjourned", "Other - Adjourned generally"),
    CASE_TRANSFERRED("Case_Transferred", "Other - Case transferred"),
    OTHER("Other", "Other");

    private final String value;
    @Getter
    private final String description;

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
