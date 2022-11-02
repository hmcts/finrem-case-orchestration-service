package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum CaseRole {
    APP_SOLICITOR("[APPSOLICITOR]"),
    RESP_SOLICITOR("[RESPSOLICITOR]"),
    CREATOR("[CREATOR]");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static CaseRole forValue(String value) {
        return Arrays.stream(CaseRole.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
