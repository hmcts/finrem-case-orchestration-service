package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BenefitPayment;

import java.util.Arrays;

@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public enum ManageHearingsAction {
    ADD_HEARING("Add_Hearing");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ManageHearingsAction forValue(String value) {
        return Arrays.stream(ManageHearingsAction.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
