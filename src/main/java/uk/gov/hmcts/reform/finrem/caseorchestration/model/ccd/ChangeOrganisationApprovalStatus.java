package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ChangeOrganisationApprovalStatus {

    PENDING(0),
    APPROVED(1),
    REJECTED(2);

    int value;

    ChangeOrganisationApprovalStatus(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}
