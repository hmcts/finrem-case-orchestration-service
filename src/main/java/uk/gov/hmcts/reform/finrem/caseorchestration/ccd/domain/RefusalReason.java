package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum RefusalReason {
    FR_MS_REFUSAL_REASON_1("FR_ms_refusalReason_1"),
    FR_MS_REFUSAL_REASON_2("FR_ms_refusalReason_2"),
    FR_MS_REFUSAL_REASON_3("FR_ms_refusalReason_3"),
    FR_MS_REFUSAL_REASON_4("FR_ms_refusalReason_4"),
    FR_MS_REFUSAL_REASON_5("FR_ms_refusalReason_5"),
    FR_MS_REFUSAL_REASON_6("FR_ms_refusalReason_6"),
    FR_MS_REFUSAL_REASON_7("FR_ms_refusalReason_7"),
    FR_MS_REFUSAL_REASON_8("FR_ms_refusalReason_8"),
    FR_MS_REFUSAL_REASON_9("FR_ms_refusalReason_9"),
    FR_MS_REFUSAL_REASON_10("FR_ms_refusalReason_10"),
    FR_MS_REFUSAL_REASON_11("FR_ms_refusalReason_11"),
    FR_MS_REFUSAL_REASON_12("FR_ms_refusalReason_12"),
    FR_MS_REFUSAL_REASON_13("FR_ms_refusalReason_13"),
    FR_MS_REFUSAL_REASON_14("FR_ms_refusalReason_14");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static RefusalReason forValue(String value) {
        return Arrays.stream(RefusalReason.values())
            .filter(option -> value.equalsIgnoreCase(option.getValue()))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
