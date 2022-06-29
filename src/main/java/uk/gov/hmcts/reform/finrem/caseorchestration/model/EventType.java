package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum EventType {

    SEND_ORDER("FR_sendOrder"),
    PREPARE_FOR_HEARING("FR_prepareForHearing"),
    CLOSE("FR_close"),
    MANAGE_CASE_DOCUMENTS("FR_manageCaseDocuments"),
    NONE("");

    private final String ccdType;

    public String getCcdType() {
        return ccdType;
    }

    public static EventType getEventType(String ccdType) {
        return Arrays.stream(EventType.values())
            .filter(eventTypeValue -> eventTypeValue.ccdType.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
