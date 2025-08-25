package uk.gov.hmcts.reform.finrem.caseorchestration.model;

public enum ContestedStatus {

    APPLICATION_SUBMITTED("applicationSubmitted"),

    APPLICATION_ISSUED("applicationIssued"),

    GATE_KEEPING_AND_ALLOCATION("gateKeepingAndAllocation"),

    PREPARE_FOR_HEARING("prepareForHearing");

    private final String id;

    ContestedStatus(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

    public String getId() {
        return id;
    }
}
