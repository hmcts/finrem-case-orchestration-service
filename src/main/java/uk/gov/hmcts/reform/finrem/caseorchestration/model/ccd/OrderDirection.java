package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum OrderDirection {
    ORDER_ACCEPTED_AS_DRAFTED("Order Accepted as drafted"),
    ORDER_ACCEPTED_AS_AMENDED("Order Accepted as amended"),
    ORDER_ACCEPTED_WITH_CONDITIONS("Order Accepted with conditions");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static OrderDirection forValue(String value) {
        return Arrays.stream(OrderDirection.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
