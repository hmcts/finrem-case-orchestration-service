package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum ConsentNatureOfApplication {
    STEP_CHILD_OR_STEP_CHILDREN("Step Child or Step Children"),
    IN_ADDITION_TO_CHILD_SUPPORT("In addition to child support"),
    DISABILITY_EXPENSES("disability expenses"),
    TRAINING("training"),
    WHEN_NOT_HABITUALLY_RESIDENT("When not habitually resident"),
    OTHER("Other");

    private final String value;

    @JsonValue
    public String getId() {
        return value;
    }

    public static ConsentNatureOfApplication forValue(String ccdType) {
        return Arrays.stream(ConsentNatureOfApplication.values())
            .filter(option -> option.value.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
