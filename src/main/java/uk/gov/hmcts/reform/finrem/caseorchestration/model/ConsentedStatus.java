package uk.gov.hmcts.reform.finrem.caseorchestration.model;

public enum ConsentedStatus {

    AWAITING_HWF_DECISION("awaitingHWFDecision"),

    APPLICATION_SUBMITTED("applicationSubmitted"),

    APPLICATION_ISSUED("applicationIssued");

    private final String id;

    ConsentedStatus(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
