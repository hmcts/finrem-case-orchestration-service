package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Features {

    CONSENT_ORDER_NOT_APPROVED_APPLICANT_DOCUMENT_GENERATION("consent_order_not_approved_applicant_document_generation"),
    CONTESTED_COURT_DETAILS_MIGRATION("contested_court_details_migration"),
    PRINT_GENERAL_LETTER("print_general_letter"),
    AUTOMATE_SEND_ORDER("automate_send_order");

    private final String name;
}
