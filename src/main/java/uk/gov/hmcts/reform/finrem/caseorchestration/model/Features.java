package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Features {

    CONTESTED_COURT_DETAILS_MIGRATION("contested_court_details_migration"),
    AUTOMATE_ASSIGN_JUDGE("automate_assign_judge"),
    AUTOMATE_SEND_ORDER("automate_send_order");

    private final String name;
}
