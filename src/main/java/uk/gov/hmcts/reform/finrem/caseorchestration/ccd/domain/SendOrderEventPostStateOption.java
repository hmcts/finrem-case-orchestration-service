package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum SendOrderEventPostStateOption {
    PREPARE_FOR_HEARING("prepareForHearing", uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.EventType.PREPARE_FOR_HEARING),
    CLOSE("close", uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.EventType.CLOSE),
    ORDER_SENT("orderSent", uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.EventType.NONE),
    NONE("", uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.EventType.NONE);

    private final String value;
    private final uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.EventType eventToTrigger;

    @JsonValue
    public String getValue() {
        return value;
    }

    public EventType getEventToTrigger() {
        return eventToTrigger;
    }

    public static SendOrderEventPostStateOption forValue(String value) {
        return Arrays.stream(SendOrderEventPostStateOption.values())
            .filter(option -> value.equalsIgnoreCase(option.getValue()))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
