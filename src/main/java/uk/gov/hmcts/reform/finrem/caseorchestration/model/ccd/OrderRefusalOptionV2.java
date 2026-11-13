package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum OrderRefusalOptionV2 {
    INSUFFICIENT_INFO_A("Insufficient information provided – A"),
    INSUFFICIENT_INFO_B("Insufficient information provided – B"),
    INSUFFICIENT_INFO_C("Insufficient information provided – C"),
    INSUFFICIENT_INFO_D("Insufficient information provided – D"),
    INSUFFICIENT_INFO_E("Insufficient information provided – E"),
    PENSION_ANNEX("Pension annex"),
    RESPONDENT_LEGAL_ADVICE("Respondent – independent legal advice"),
    D81_INCOMPLETE("The D81 incomplete"),
    HEARING_FIXED_FOR_FIRST_AVAILABLE_DATE("Hearing fixed for first available date"),
    ORDER_DOES_NOT_APPEAR_FAIR("Order does not appear fair"),
    PENSION_PROPERTY_VALUES_NOT_CLEAR("Pension/Property values not clear"),
    APPLICATION_FOR_VARIATION_ORDER_RECONSIDERED("Application for a consent/variation order reconsidered"),
    APPLICATION_NOW_CONTESTED("Application now contested"),
    OTHER_PLEASE_SPECIFY("Other (please specify)");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static OrderRefusalOptionV2 getOrderRefusalOptionV2(String ccdType) {
        return Arrays.stream(OrderRefusalOptionV2.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
