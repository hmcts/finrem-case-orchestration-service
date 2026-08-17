package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_go_Addressto", generate = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum GeneralOrderAddressTo {
    @CCD(label = "Applicant")
    APPLICANT("applicant", "Applicant"),
    @CCD(label = "Applicant Solicitor")
    APPLICANT_SOLICITOR("applicantSolicitor", "Applicant Solicitor"),
    @CCD(label = "Respondent Solicitor")
    RESPONDENT_SOLICITOR("respondentSolicitor", "Respondent Solicitor");

    private final String value;
    private final String text;

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public static GeneralOrderAddressTo forValue(String value) {
        return Arrays.stream(GeneralOrderAddressTo.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
