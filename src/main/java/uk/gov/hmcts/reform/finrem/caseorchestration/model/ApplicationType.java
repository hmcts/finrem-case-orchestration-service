package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import java.util.Arrays;

public enum ApplicationType {

    CONSENTED("consented"),

    CONTESTED("contested");

    private final String id;

    ApplicationType(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

    public static ApplicationType from(String value) {
        for (ApplicationType applicationType : values()) {
            if (applicationType.id.equalsIgnoreCase(value)) {
                return applicationType;
            }
        }
        throw new IllegalArgumentException(
                "Unknown enum type " + value + ", allowed values are " + Arrays.toString(values()));
    }
}
