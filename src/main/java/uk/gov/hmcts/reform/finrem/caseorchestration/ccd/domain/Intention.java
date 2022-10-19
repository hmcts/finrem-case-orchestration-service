package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum Intention {

    APPLY_TO_COURT_FOR("ApplyToCourtFor"),
    PROCEED_WITH_APPLICATION("ProceedWithApplication"),
    APPLY_TO_VARY("ApplyToVary"),
    APPLY_TO_DISCHARGE_PERIODICAL_PAYMENT_ORDER("ApplyToDischargePeriodicalPaymentOrder");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static Intention forValue(String value) {
        return Arrays.stream(Intention.values())
            .filter(option -> value.equalsIgnoreCase(option.getValue()))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}

