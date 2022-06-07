package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum SendOrderPostStateOption {

    @JsonProperty("prepareForHearing")
    PREPARE_FOR_HEARING("prepareForHearing", EventType.PREPARE_FOR_HEARING),
    CLOSE("close", EventType.CLOSE),
    @JsonProperty("orderSent")
    ORDER_SENT("orderSent", EventType.NONE);

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
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }


}
