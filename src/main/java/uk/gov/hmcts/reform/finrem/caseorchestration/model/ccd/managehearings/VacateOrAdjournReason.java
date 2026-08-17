package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

//Note: Expecting better display values that are more appropriate for the notice.  To follow.
@ComplexType(name = "FR_vacateOrAdjournHearingReason", generate = true)
@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public enum VacateOrAdjournReason {
    @CCD(label = "Parties - Parties/case not ready to proceed")
    CASE_NOT_READY("Case_Not_Ready", "Parties - Parties/case not ready to proceed"),
    @CCD(label = "Parties - Case settled, order/application made")
    CASE_SETTLED("Case_Settled", "Parties - Case settled, order/application made"),
    @CCD(label = "Parties - Parties/legal representatives not available")
    CASE_REP_UNAVAILABLE("Legal_Rep_Unavailable", "Parties - Parties/legal representatives not available"),
    @CCD(label = "HMCTS - No courtroom available")
    COURTROOM_UNAVAILABLE("Courtroom_Unavailable", "HMCTS - No courtroom available"),
    @CCD(label = "HMCTS - Special measures not available")
    SPECIAL_MEASURES_NOT_AVAILABLE("Special_Measures_Not_Available", "HMCTS - Special measures not available"),
    @CCD(label = "HMCTS - Interpreter not available")
    INTERPRETER_UNAVAILABLE("Interpreter_Unavailable", "HMCTS - Interpreter not available"),
    @CCD(label = "Judiciary - Lack of Judicial availability")
    JUDGE_UNAVAILABLE("Judge_Unavailable", "Judiciary - Lack of Judicial availability"),
    @CCD(label = "Judiciary - Insufficient time listed or to complete hearing")
    INSUFFICIENT_TIME("Insufficient_Time", "Judiciary - Insufficient time listed or to complete hearing"),
    @CCD(label = "Other - Case stayed")
    CASE_STAYED("Case_Stayed", "Other - Case stayed"),
    @CCD(label = "Other - Adjourned generally")
    ADJOURNED("Adjourned", "Other - Adjourned generally"),
    @CCD(label = "Other - Case transferred")
    CASE_TRANSFERRED("Case_Transferred", "Other - Case transferred"),
    @CCD(label = "Other - Please specify")
    OTHER("Other", "Other - Please specify");

    private final String value;
    @Getter
    private final String displayValue;

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
