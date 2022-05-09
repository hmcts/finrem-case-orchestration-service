package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Features {

    RESPONDENT_JOURNEY("respondent_journey"),
    ASSIGN_CASE_ACCESS("assign_case_access"),
    SEND_TO_FRC("send_to_frc"),
    PAYMENT_REQUEST_USING_CASE_TYPE("pba_case_type"),
    USE_USER_TOKEN("use_user_token"),

    MANAGE_BUNDLE("manage_bundle");

    private final String name;
}
