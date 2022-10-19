package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum Provision {
    @JsonProperty("matrimonialOrCivilPartnershipProceedings")
    MATRIMONIAL_OR_CIVIL_PARTNERSHIP_PROCEEDINGS("matrimonialOrCivilPartnershipProceedings"),
    @JsonProperty("childrenAct1989")
    CHILDREN_ACT_1989("childrenAct1989");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static Provision forValue(String value) {
        return Arrays.stream(Provision.values())
            .filter(option -> value.equalsIgnoreCase(option.getValue()))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
