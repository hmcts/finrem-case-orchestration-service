package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_fl_solicitorToDraftOrder", generate = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum SolicitorToDraftOrder {
    @CCD(label = "Applicant Solicitor")
    @JsonProperty("applicantSolicitor") APPLICANT_SOLICITOR("applicantSolicitor"),
    @CCD(label = "Respondent Solicitor")
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
