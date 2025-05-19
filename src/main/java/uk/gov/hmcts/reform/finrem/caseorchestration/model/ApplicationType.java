package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;

import java.util.Arrays;

public enum ApplicationType {

    CONSENTED("consented"),

    CONTESTED("contested");

    private final String id;

    ApplicationType(String id) {
        this.id = id;
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

    public static ApplicationType from(CaseType caseType) {
        return switch (caseType) {
            case CONSENTED -> CONSENTED;
            case CONTESTED -> CONTESTED;
            default -> throw new IllegalArgumentException(
                "Unknown enum type " + caseType + ", allowed values are " + Arrays.toString(values()));
        };
    }

    @Override
    public String toString() {
        return id;
    }
}
