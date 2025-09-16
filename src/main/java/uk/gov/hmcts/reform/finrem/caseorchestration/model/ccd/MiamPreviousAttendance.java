package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum MiamPreviousAttendance {
    FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_1("FR_ms_MIAMPreviousAttendanceChecklist_Value_1",
        "In the 4 months prior to making the application, the person attended a MIAM or a non-court dispute "
            + "resolution process relating to the same or substantially the same dispute; and where the applicant attended "
            + "a non-court dispute resolution process, there is evidence of that attendance in the form of written "
            + "confirmation from the dispute resolution provider. This evidence should be submitted alongside your "
            + "application, and must include the signature of the provider; or"),
    FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_2("FR_ms_MIAMPreviousAttendanceChecklist_Value_2",
        "At the time of making the application, the person is participating in "
            + "another form of non-court dispute resolution relating to the same or substantially the same dispute"),
    FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_3("FR_ms_MIAMPreviousAttendanceChecklist_Value_3",
        "In the 4 months prior to making the application, the person filed "
            + "a relevant family application confirming that a MIAM exemption applied and that application "
            + "related to the same or substantially the same dispute"),
    FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_4("FR_ms_MIAMPreviousAttendanceChecklist_Value_4",
        "The application would be made in existing proceedings which are continuing and the prospective "
            + "applicant attended a MIAM before initiating those proceedings."),
    FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_5("FR_ms_MIAMPreviousAttendanceChecklist_Value_5",
        "The application would be made in existing proceedings which are "
            + "continuing and a MIAM exemption applied to the application for those proceedings"),
    FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_6("FR_ms_MIAMPreviousAttendanceChecklist_Value_6",
        "I am unable to provide the required evidence with my application.");

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
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
