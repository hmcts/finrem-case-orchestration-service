package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum SendOrderPostStateOption {

    PREPARE_FOR_HEARING("prepareForHearing"),
    CLOSE("close"),
    ORDER_SENT("orderSent");

    private final String ccdType;

    public String getCcdType() {
        return ccdType;
    }

    public static SendOrderPostStateOption getSendOrderPostStateOption(String ccdType) {
        return Arrays.stream(SendOrderPostStateOption.values())
            .filter(option -> option.ccdType.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
