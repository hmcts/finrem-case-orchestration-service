package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum NatureOfApplicationSchedule {

    INTERIM_CHILD_PERIODICAL_PAYMENTS("Interim child periodical payments", "Interim child periodical payments"),
    LUMP_SUM_ORDER("Lump Sum Order", "Lump Sum Order"),
    A_SETTLEMENT_OR_A_TRANSFER_OF_PROPERTY("A settlement or a transfer of property",
        "A settlement or a transfer of property for the benefit of the child(ren)"),
    PERIODICAL_PAYMENT_ORDER("periodicalPaymentOrder", "Periodical Payment Order"),
    VARIATION_ORDER("variationOrder", "Variation Order");

    private final String value;
    private final String text;

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public static NatureOfApplicationSchedule forValue(String value) {
        return Arrays.stream(NatureOfApplicationSchedule.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
