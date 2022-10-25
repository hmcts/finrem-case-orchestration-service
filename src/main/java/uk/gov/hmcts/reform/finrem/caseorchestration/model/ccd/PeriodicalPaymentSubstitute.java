package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum PeriodicalPaymentSubstitute {
    LUMP_SUM_ORDER("lumpSumOrder"),
    PROPERTY_ADJUSTMENT_ORDER("propertyAdjustmentOrder"),
    PENSION_SHARING_ORDER("pensionSharingOrder"),
    PENSION_COMPENSATION_SHARING_ORDER("pensionCompensationSharingOrder");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static PeriodicalPaymentSubstitute forValue(String value) {
        return Arrays.stream(PeriodicalPaymentSubstitute.values())
            .filter(option -> value.equalsIgnoreCase(option.getValue()))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
