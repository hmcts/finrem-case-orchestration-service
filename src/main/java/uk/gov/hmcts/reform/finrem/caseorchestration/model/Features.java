package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Features {

    CONTESTED_PRINT_DRAFT_ORDER_NOT_APPROVED("contested_print_draft_order_not_approved"),
    CONTESTED_PRINT_GENERAL_ORDER("contested_print_general_order"),
    CONTESTED_COURT_DETAILS_MIGRATION("contested_court_details_migration"),
    SHARE_A_CASE("share_a_case"),
    SEND_TO_FRC("send_to_frc");

    private final String name;
}
