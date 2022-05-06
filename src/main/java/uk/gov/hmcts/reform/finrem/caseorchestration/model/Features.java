package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Features {

    RESPONDENT_JOURNEY("respondent_journey"),
    ASSIGN_CASE_ACCESS("assign_case_access"),
    CASEWORKER_NOTICE_OF_CHANGE("caseworker-notice-of-change"),
    SEND_TO_FRC("send_to_frc"),
    PAYMENT_REQUEST_USING_CASE_TYPE("pba_case_type"),
    USE_USER_TOKEN("use_user_token"),
    SOLICITOR_NOTICE_OF_CHANGE("solicitor_notice_of_change");

    private final String name;
}
