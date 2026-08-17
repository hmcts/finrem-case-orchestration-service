package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_fl_OtherDocument", generate = true)
@RequiredArgsConstructor
public enum OtherDocumentType {
    @CCD(label = "Schedule of Assets")
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
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
