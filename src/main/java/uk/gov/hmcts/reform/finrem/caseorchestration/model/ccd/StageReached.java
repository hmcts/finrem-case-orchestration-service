package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum StageReached {
    @JsonProperty("Decree Nisi") DECREE_NISI("Decree Nisi"),
    @JsonProperty("Decree Absolute") DECREE_ABSOLUTE("Decree Absolute"),
    @JsonProperty("Petition Issued") PETITION_ISSUED("Petition Issued");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static StageReached forValue(String selectedStage) {
        return Arrays.stream(StageReached.values())
            .filter(option -> selectedStage.equalsIgnoreCase(option.getValue()))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
