package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum GeneralOrderAddressTo {
    APPLICANT("applicant", "Applicant"),
    APPLICANT_SOLICITOR("applicantSolicitor", "Applicant Solicitor"),
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
            .filter(option -> value.equalsIgnoreCase(option.getValue()))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
