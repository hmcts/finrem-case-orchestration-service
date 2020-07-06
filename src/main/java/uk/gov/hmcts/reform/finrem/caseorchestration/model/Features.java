package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Features {

    APPROVED_CONSENT_ORDER_NOTIFICATION_LETTER("approved_consent_order_notification_letter"),
    HWF_SUCCESSFUL_NOTIFICATION_LETTER("hwf_successful_notification_letter"),
    ASSIGNED_TO_JUDGE_NOTIFICATION_LETTER("assigned_to_judge_notification_letter"),
    CONSENT_ORDER_NOT_APPROVED_APPLICANT_DOCUMENT_GENERATION("consent_order_not_approved_applicant_document_generation"),
    CONTESTED_COURT_DETAILS_MIGRATION("contested_court_details_migration");

    private final String name;
}
