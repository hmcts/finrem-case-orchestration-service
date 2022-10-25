package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum UploadConsentOrderDocumentType {
    CONSENT_ORDER("consentOrder"),
    COVER_ORDER("coverOrder"),
    P1("P1"),
    P2("P2"),
    PPF("PPF"),
    PPF1("PPF1"),
    PPF2("PPF2"),
    OTHER("Other");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static UploadConsentOrderDocumentType forValue(String value) {
        return Arrays.stream(UploadConsentOrderDocumentType.values())
            .filter(option -> value.equalsIgnoreCase(option.getValue()))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
