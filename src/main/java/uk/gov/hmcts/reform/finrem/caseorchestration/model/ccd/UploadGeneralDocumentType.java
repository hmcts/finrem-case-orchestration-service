package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum UploadGeneralDocumentType {
    FINAL_ORDER("Final order"),
    CONDITIONAL_ORDER("Conditional order"),
    NOTICE_OF_ACTING("Notice of Acting"),
    LETTER_EMAIL_FROM_APPLICANT("Letter/Email from Applicant"),
    LETTER_EMAIL_FROM_APPLICANT_SOLICITOR("Letter/Email from Applicant Solicitor"),
    LETTER_EMAIL_FROM_RESPONDENT("Letter/Email from Respondent"),
    LETTER_EMAIL_FROM_RESPONDENT_SOLICITOR("Letter/Email from Respondent Solicitor"),
    APPLICATION("Application"),
    DRAFT_ORDER("Draft Order"),
    STATEMENT_REPORT("Statement / Report"),
    OTHER("Other");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static UploadGeneralDocumentType getUploadGeneralDocumentType(String ccdType) {
        return Arrays.stream(UploadGeneralDocumentType.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
