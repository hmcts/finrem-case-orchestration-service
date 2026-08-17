package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum GeneralLetterAddressToType {
    @CCD(label = "Applicant Solicitor")
    APPLICANT_SOLICITOR("applicantSolicitor"),
    @CCD(label = "Respondent Solicitor")
    RESPONDENT_SOLICITOR("respondentSolicitor"),
    @CCD(label = "Respondent")
    RESPONDENT("respondent"),
    @CCD(label = "Other")
    OTHER("other"),
    @CCD(label = "Applicant")
    APPLICANT("applicant");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static GeneralLetterAddressToType forValue(String value) {
        return Arrays.stream(GeneralLetterAddressToType.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
