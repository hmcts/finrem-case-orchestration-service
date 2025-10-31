package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managecasedocuments;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public enum ManageCaseDocumentsAction {
    ADD_NEW("Add_new"),
    AMEND("Amend");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ManageCaseDocumentsAction forValue(String value) {
        return Arrays.stream(ManageCaseDocumentsAction.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
