package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum MiamUrgencyReason {
    FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_1("FR_ms_MIAMUrgencyReasonChecklist_Value_1",
        "There is risk to the life, liberty or physical safety of the prospective applicant or "
            + "his or her family or his or her home; or"),
    FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_2("FR_ms_MIAMUrgencyReasonChecklist_Value_2",
        "Any delay caused by attending a MIAM would cause a significant risk of a miscarriage of justice; or"),
    FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_3("FR_ms_MIAMUrgencyReasonChecklist_Value_3",
        "Any delay caused by attending a MIAM would cause significant financial hardship to the prospective applicant; or"),
    FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_4("FR_ms_MIAMUrgencyReasonChecklist_Value_4",
        "Any delay caused by attending a MIAM would cause irretrievable problems in dealing with the dispute "
            + "(including the irretrievable loss of significant evidence); or"),
    FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_5("FR_ms_MIAMUrgencyReasonChecklist_Value_5",
        "There is a significant risk that in the period necessary to schedule and attend a MIAM, "
            + "proceedings relating to the dispute will be brought in another state in which a valid claim "
            + "to jurisdiction may exist, such that a court in that other State would be seized of the dispute "
            + "before a court in England and Wales.");

    private final String value;
    private final String text;

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public static MiamUrgencyReason forValue(String value) {
        return Arrays.stream(MiamUrgencyReason.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
