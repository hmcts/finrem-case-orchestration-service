package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum GeneralLetterAddressToType {
    APPLICANT_SOLICITOR("applicantSolicitor"),
    RESPONDENT_SOLICITOR("respondentSolicitor"),
    RESPONDENT("respondent"),
    OTHER("other"),
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
