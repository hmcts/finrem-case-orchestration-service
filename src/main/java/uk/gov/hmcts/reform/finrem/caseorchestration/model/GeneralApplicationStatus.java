package uk.gov.hmcts.reform.finrem.caseorchestration.model;

public enum GeneralApplicationStatus {
    CREATED("Created"),
    REFERRED("Referred To Judge"),
    APPROVED("Approved"),
    NOT_APPROVED("Not Approved"),
    DIRECTION_APPROVED("Approved, Completed"),
    DIRECTION_NOT_APPROVED("Not Approved, Completed"),
    DIRECTION_OTHER("Other, Completed"),
    OTHER("Other");

    private final String status;

    GeneralApplicationStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }

    public String getId() {
        return status;
    }
}
