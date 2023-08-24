package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Features {

    ASSIGN_CASE_ACCESS("assign_case_access"),
    CASEWORKER_NOTICE_OF_CHANGE("caseworker-notice-of-change"),
    PAYMENT_REQUEST_USING_CASE_TYPE("pba_case_type"),
    USE_USER_TOKEN("use_user_token"),
    SOLICITOR_NOTICE_OF_CHANGE("solicitor_notice_of_change"),
    SEND_LETTER_RECIPIENT_CHECK("send_letter_recipient_check"),
    SECURE_DOC_ENABLED("secure_doc_enabled"),
    INTERVENER_ENABLED("intervener_enabled");
    private final String name;
}
