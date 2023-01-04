package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum ContestedNatureApplication implements INatureOfApplication  {
    PERIODICAL_PAYMENT_ORDER("periodicalPaymentOrder", "Periodical Payment Order"),
    MAINTENANCE_PENDING_SUIT("Maintenance Pending Suit", "Maintenance Pendzing Suit"),
    LUMP_SUM_ORDER("Lump Sum Order", "Lump Sum Order"),
    PENSION_SHARING_ORDER("Pension Sharing Order", "Pension Sharing Order"),
    PENSION_ATTACHMENT_ORDER("Pension Attachment Order", "Pension Attachment Order"),
    PENSION_COMPENSATION_SHARING_ORDER("Pension Compensation Sharing Order",
        "Pension Compensation Sharing Order"),
    PENSION_COMPENSATION_ATTACHMENT_ORDER("Pension Compensation Attachment Order",
        "Pension Compensation Attachment Order"),
    A_SETTLEMENT_OR_A_TRANSFER_OF_PROPERTY("A settlement or a transfer of property",
        "A settlement or a transfer of property for the benefit of the child(ren)"),
    PROPERTY_ADJUSTMENT_ORDER("propertyAdjustmentOrder", "Property Adjustment Order"),
    VARIATION_ORDER("Variation Order", "Variation Order");

    private final String value;
    private final String text;

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public static ContestedNatureApplication forValue(String value) {
        return Arrays.stream(ContestedNatureApplication.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
