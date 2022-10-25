package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum RespondToOrderDocumentType {
    APPLICANT_LETTER_EMAIL("ApplicantLetterEmail"),
    RESPONDENT_LETTER_EMAIL("RespondentLetterEmail"),
    AMENDED_CONSENT_ORDER("AmendedConsentOrder"),
    STATEMENT_REPORT("StatementReport"),
    OTHER("Other");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static RespondToOrderDocumentType forValue(String value) {
        return Arrays.stream(RespondToOrderDocumentType.values())
            .filter(option -> value.equalsIgnoreCase(option.getValue()))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
