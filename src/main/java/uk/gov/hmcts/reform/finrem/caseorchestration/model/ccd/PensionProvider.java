package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_fl_PensionProvider", generate = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum PensionProvider {
    @CCD(label = "The Court")
    THE_COURT("theCourt"),
    @CCD(label = "Applicant Solicitor")
    APPLICANT_SOLICITOR("applicantSolicitor"),
    @CCD(label = "Respondent Solicitor")
    RESPONDENT_SOLICITOR("respondentSolicitor"),
    @CCD(label = "Other")
    OTHER("other");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static PensionProvider forValue(String value) {
        return Arrays.stream(PensionProvider.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
