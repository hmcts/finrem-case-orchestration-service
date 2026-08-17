package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_fl_ApplicantAndRespondentEvidenceParty", generate = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum ApplicantAndRespondentEvidenceParty {
    @CCD(label = "Applicant")
    APPLICANT("applicant"),
    @CCD(label = "Respondent")
    RESPONDENT("respondent");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ApplicantAndRespondentEvidenceParty forValue(String value) {
        return Arrays.stream(ApplicantAndRespondentEvidenceParty.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
