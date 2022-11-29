package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum SolUploadDocumentType {
    OTHER("Other"),
    NOTICE_OF_ACTING("Notice of Acting"),
    LETTER_EMAIL("Letter / Email"),
    SCHEDULE_OF_ASSETS("Schedule Of Assets"),
    UPDATED_D81("Updated D81"),
    AMEND_CONSENT_ORDER("Amended Consent Order");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static SolUploadDocumentType getSolUploadDocumentType(String ccdType) {
        return Arrays.stream(SolUploadDocumentType.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
