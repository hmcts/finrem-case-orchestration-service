package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum MiamOtherGrounds {
    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_1("FR_ms_MIAMOtherGroundsChecklist_Value_1",
        "The applicant is bankrupt evidenced by an application by the prospective applicant for a bankruptcy "
            + "order;"),
    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_2("FR_ms_MIAMOtherGroundsChecklist_Value_2",
        "The applicant is bankrupt evidenced by a petition by a creditor of the prospective applicant for a "
            + "bankruptcy order"),
    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_3("FR_ms_MIAMOtherGroundsChecklist_Value_3",
        "The applicant is bankrupt evidenced by a bankruptcy order in respect of the prospective applicant."),
    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_4("FR_ms_MIAMOtherGroundsChecklist_Value_4",
        "The prospective applicant does not have sufficient contact details for any of the prospective "
            + "respondents to enable a family mediator to contact any of the prospective respondents for the purpose "
            + "of scheduling the MIAM."),
    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_5("FR_ms_MIAMOtherGroundsChecklist_Value_5",
        "The application would be made without notice (Paragraph 5.1 of Practice Direction 18A sets out the "
            + "circumstances in which applications may be made without notice.)"),
    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_6("FR_ms_MIAMOtherGroundsChecklist_Value_6",
        "(i) the prospective applicant is or all of the prospective respondents are subject to a disability or "
            + "other inability that would prevent attendance at a MIAM unless appropriate facilities can be offered by"
            + " an authorised mediator;  .....cont"),
    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_7("FR_ms_MIAMOtherGroundsChecklist_Value_7",
        "The prospective applicant or all of the prospective respondents cannot attend a MIAM because he or she "
            + "is, or they are, as the case may be (i) in prison or any other institution in which he or she is or "
            + "they are required to be detained; ... cont"),
    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_8("FR_ms_MIAMOtherGroundsChecklist_Value_8",
        "The prospective applicant or all of the prospective respondents are not habitually resident in "
            + "England and Wales."),
    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_9("FR_ms_MIAMOtherGroundsChecklist_Value_9",
        "A child is one of the prospective parties by virtue of Rule 12.3(1)."),
    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_10("FR_ms_MIAMOtherGroundsChecklist_Value_10",
        "(i) the prospective applicant has contacted as many authorised family mediators as have an office "
            + "within fifteen miles of his or her home (or three of them if there are three or more), and all of them"
            + " have stated that they are not available ...cont"),
    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_11("FR_ms_MIAMOtherGroundsChecklist_Value_11",
        "There is no authorised family mediator with an office within fifteen miles of the prospective "
            + "applicant’s home.");

    private final String value;
    private final String text;

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public static MiamOtherGrounds forValue(String value) {
        return Arrays.stream(MiamOtherGrounds.values())
            .filter(option -> value.equalsIgnoreCase(option.getValue()))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
