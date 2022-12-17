package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum CaseType {

    CONSENTED("FinancialRemedyMVP2"),
    CONTESTED("FinancialRemedyContested"),
    @JsonEnumDefaultValue
    UNKNOWN("unknown");

    private final String ccdType;

    @JsonValue
    public String getCcdType() {
        return ccdType;
    }

    public static CaseType forValue(String value) {
        return Arrays.stream(CaseType.values())
            .filter(option -> value.equalsIgnoreCase(option.getCcdType()))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}