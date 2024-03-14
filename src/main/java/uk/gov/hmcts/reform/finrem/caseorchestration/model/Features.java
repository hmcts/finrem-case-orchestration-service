package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Features {

    ASSIGN_CASE_ACCESS("assign_case_access"),
    SEND_TO_FRC("send_to_frc"),
    PAYMENT_REQUEST_USING_CASE_TYPE("pba_case_type"),
    SEND_LETTER_RECIPIENT_CHECK("send_letter_recipient_check"),
    SECURE_DOC_ENABLED("secure_doc_enabled"),
    INTERVENER_ENABLED("intervener_enabled"),
    CASE_FILE_VIEW_ENABLED("case_file_view_enabled");
    private final String name;
}
