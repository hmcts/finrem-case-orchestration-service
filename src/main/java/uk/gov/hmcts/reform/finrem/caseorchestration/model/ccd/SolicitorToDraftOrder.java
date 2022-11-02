package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum SolicitorToDraftOrder {
    @JsonProperty("applicantSolicitor") APPLICANT_SOLICITOR("applicantSolicitor"),
    @JsonProperty("respondentSolicitor") RESPONDENT_SOLICITOR("respondentSolicitor");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static SolicitorToDraftOrder forValue(String value) {
        return Arrays.stream(SolicitorToDraftOrder.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
