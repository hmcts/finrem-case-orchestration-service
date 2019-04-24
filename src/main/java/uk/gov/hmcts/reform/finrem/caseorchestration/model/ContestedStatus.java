package uk.gov.hmcts.reform.finrem.caseorchestration.model;

public enum ContestedStatus {

    APPLICATION_SUBMITTED("applicationSubmitted"),

    APPLICATION_ISSUED("applicationIssued"),

    GATE_KEEPING_AND_ALLOCATION("gateKeepingAndAllocation");

    private final String id;

    ContestedStatus(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
