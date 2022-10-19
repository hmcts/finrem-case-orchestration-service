package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum MiamPreviousAttendance {
    FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_1("FR_ms_MIAMPreviousAttendanceChecklist_Value_1",
        "In the 4 months prior to making the application, the person attended a MIAM or participated in another "
            + "form of non-court dispute resolution relating to the same or substantially the same dispute"),
    FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_2("FR_ms_MIAMPreviousAttendanceChecklist_Value_2",
        "At the time of making the application, the person is participating in another form of non-court dispute "
            + "resolution relating to the same or substantially the same dispute"),
    FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_3("FR_ms_MIAMPreviousAttendanceChecklist_Value_3",
        "In the 4 months prior to making the application, the person filed a relevant family application "
            + "confirming that a MIAM exemption applied and that application related to the same or substantially the"
            + " same dispute"),
    FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_4("FR_ms_MIAMPreviousAttendanceChecklist_Value_4",
        "The application would be made in existing proceedings which are continuing and the prospective "
            + "applicant attended a MIAM before initiating those proceedings"),
    FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_5("FR_ms_MIAMPreviousAttendanceChecklist_Value_5",
        "The application would be made in existing proceedings which are continuing and a MIAM exemption applied"
            + " to the application for those proceedings");

    private final String value;
    private final String text;

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public static MiamPreviousAttendance forValue(String value) {
        return Arrays.stream(MiamPreviousAttendance.values())
            .filter(option -> value.equalsIgnoreCase(option.getValue()))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
