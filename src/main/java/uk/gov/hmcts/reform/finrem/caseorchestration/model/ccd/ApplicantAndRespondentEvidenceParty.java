package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum ApplicantAndRespondentEvidenceParty {
    APPLICANT("applicant"),
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
