package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum State {

    CASE_ADDED("caseAdded"),
    NEW_PAPER_CASE("newPaperCase"),
    AWAITING_HWF_DECISION("awaitingHWFDecision"),
    AWAITING_PAYMENT("awaitingPayment"),
    AWAITING_PAYMENT_RESPONSE("awaitingPaymentResponse"),
    APPLICATION_SUBMITTED("applicationSubmitted"),
    APPLICATION_ISSUED("applicationIssued"),
    REFERRED_TO_JUDGE("referredToJudge"),
    ORDER_MADE("orderMade"),
    CONSENT_ORDER_APPROVED("consentOrderApproved"),
    CONSENT_ORDER_MADE("consentOrderMade"),
    AWAITING_RESPONSE("awaitingResponse"),
    RESPONSE_RECEIVED("responseReceived"),
    AWAITING_INFO("awaitingInfo"),
    INFO_RECEIVED("infoReceived"),
    CLOSE("close"),
    GATE_KEEPING_AND_ALLOCATION("gateKeepingAndAllocation"),
    SCHEDULING_AND_HEARING("schedulingAndHearing"),
    JUDGE_DRAFT_ORDER("judgeDraftOrder"),
    SOLICITOR_DRAFT_ORDER("solicitorDraftOrder"),
    REVIEW_ORDER("reviewOrder"),
    DRAFT_ORDER_NOT_APPROVED("draftOrderNotApproved"),
    SCHEDULE_RAISE_DIRECTIONS_ORDER("scheduleRaiseDirectionsOrder"),
    ORDER_DRAWN("orderDrawn"),
    ORDER_SENT("orderSent"),
    CONSENTED_ORDER_SUBMITTED("consentedOrderSubmitted"),
    AWAITING_JUDICIARY_RESPONSE_CONSENT("awaitingJudiciaryResponseConsent"),
    CONSENTED_ORDER_ASSIGN_JUDGE("consentedOrderAssignJudge"),
    CONSENTED_ORDER_APPROVED("consentedOrderApproved"),
    CONSENTED_ORDER_NOT_APPROVED("consentedOrderNotApproved"),
    GENERAL_APPLICATION("generalApplication"),
    GENERAL_APPLICATION_AWAITING_JUDICIARY_RESPONSE("generalApplicationAwaitingJudiciaryResponse"),
    GENERAL_APPLICATION_OUTCOME("generalApplicationOutcome"),
    APPLICATION_DRAFTED("applicationDrafted");

    private final String stateId;

    @JsonValue
    public String getStateId() {
        return stateId;
    }

    public static State forValue(String value) {
        return Arrays.stream(State.values())
            .filter(option -> value.equalsIgnoreCase(option.getStateId()))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
