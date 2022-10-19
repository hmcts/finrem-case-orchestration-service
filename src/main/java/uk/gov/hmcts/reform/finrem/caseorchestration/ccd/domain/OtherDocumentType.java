package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum OtherDocumentType {
    SCHEDULE_OF_ASSETS("ScheduleOfAssets"),
    LETTER("Letter"),
    NOTICE_OF_ACTING("Notice of acting"),
    OTHER("Other");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static OtherDocumentType forValue(String value) {
        return Arrays.stream(OtherDocumentType.values())
            .filter(option -> value.equalsIgnoreCase(option.getValue()))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
