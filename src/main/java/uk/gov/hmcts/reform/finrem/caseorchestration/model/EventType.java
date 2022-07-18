package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum EventType {

    SEND_ORDER("FR_sendOrder"),
    PREPARE_FOR_HEARING("FR_prepareForHearing"),
    UPLOAD_CASE_FILES("FR_uploadCaseFiles"),
    SOLICITOR_CREATE("FR_solicitorCreate"),
    AMEND_APP_DETAILS("FR_amendApplicationDetails"),
    AMEND_CONTESTED_APP_DETAILS("FR_amendApplication"),
    AMEND_CASE("FR_amendCase"),
    APPROVE_ORDER("FR_approveApplication"),
    REJECT_ORDER("FR_orderRefusal"),
    CLOSE("FR_close"),
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
