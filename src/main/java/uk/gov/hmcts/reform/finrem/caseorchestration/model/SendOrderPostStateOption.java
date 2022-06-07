package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum SendOrderPostStateOption {

    PREPARE_FOR_HEARING("prepareForHearing", EventType.PREPARE_FOR_HEARING),
    CLOSE("close", EventType.CLOSE),
    ORDER_SENT("orderSent", EventType.NONE),
    NONE("", EventType.NONE);

    private final String ccdField;

    private final EventType eventToTrigger;

    public String getCcdField() {
        return ccdField;
    }

    public EventType getEventToTrigger() {
        return eventToTrigger;
    }

    public static SendOrderPostStateOption getSendOrderPostStateOption(String ccdType) {
        return Arrays.stream(SendOrderPostStateOption.values())
            .filter(option -> option.ccdField.equals(ccdType))
            .findFirst().orElse(SendOrderPostStateOption.NONE);
    }
}
