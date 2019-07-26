package uk.gov.hmcts.reform.finrem.caseorchestration.model;

public enum ConsentedStatus {

    CONSENT_ORDER_APPROVED("consentOrderApproved"),

    AWAITING_HWF_DECISION("awaitingHWFDecision"),

    APPLICATION_SUBMITTED("applicationSubmitted"),

    APPLICATION_ISSUED("applicationIssued"),

    PREPARE_FOR_HEARING("prepareForHearing"),

    AWAITING_RESPONSE("awaitingResponse"),

    CONSENT_ORDER_MADE("consentOrderMade"),

    CONSENT_ORDER_NOT_APPROVED("orderMade"),

    CASE_ADDED("caseAdded");


    private final String id;

    ConsentedStatus(String id) {
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
