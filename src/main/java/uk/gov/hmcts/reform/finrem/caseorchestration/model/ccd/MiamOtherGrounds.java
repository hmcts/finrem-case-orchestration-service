package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum MiamOtherGrounds {
    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_1("FR_ms_MIAMOtherGroundsChecklist_Value_1",
        "The applicant is bankrupt evidenced by an application by "
            + "the prospective applicant for a bankruptcy order;"),
    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_2("FR_ms_MIAMOtherGroundsChecklist_Value_2",
        "The applicant is bankrupt evidenced by a petition by a "
            + "creditor of the prospective applicant for a bankruptcy order"),
    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_3("FR_ms_MIAMOtherGroundsChecklist_Value_3",
        "The applicant is bankrupt evidenced by a bankruptcy order "
            + "in respect of the prospective applicant."),
    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_4("FR_ms_MIAMOtherGroundsChecklist_Value_4",
        "The prospective applicant does not have sufficient contact "
            + "details for any of the prospective respondents to enable a family mediator to contact any"
            + " of the prospective respondents for the purpose of scheduling the MIAM."),
    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_5("FR_ms_MIAMOtherGroundsChecklist_Value_5",
        "The application would be made without notice (Paragraph 5.1 of Practice Direction 18A sets "
            + "out the circumstances in which applications may be made without notice.)"),
    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_6("FR_ms_MIAMOtherGroundsChecklist_Value_6",
        "(i) the prospective applicant is or all of the prospective "
            + "respondents are subject to a disability or other inability that would prevent attendance "
            + "at a MIAM unless appropriate facilities can be offered by an authorised mediator; (ii) "
            + "the prospective applicant has contacted as many authorised family mediators as have an "
            + "office within fifteen miles of his or her home (or three of them if there are three or more), "
            + "and all have stated that they are unable to provide such facilities; and (iii)the names, postal "
            + "addresses and telephone numbers or e-mail addresses for such authorised family mediators, and "
            + "the dates of contact, can be provided to the court if requested."),
    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_7("FR_ms_MIAMOtherGroundsChecklist_Value_7",
        "the prospective applicant or all of the prospective respondents "
            + "cannot attend a MIAM because he or she is, or they are, as the case may be (i) in prison or "
            + "any other institution in which he or she is or they are required to be detained; (ii) subject "
            + "to conditions of bail that prevent contact with the other person; or (iii) subject to a licence "
            + "with a prohibited contact requirement in relation to the other person."),
    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_8("FR_ms_MIAMOtherGroundsChecklist_Value_8",
        "The prospective applicant or all of the prospective respondents "
            + "are not habitually resident in England and Wales."),
    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_9("FR_ms_MIAMOtherGroundsChecklist_Value_9",
        "A child is one of the prospective parties."),
    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_10("FR_ms_MIAMOtherGroundsChecklist_Value_10",
        "(i) the prospective applicant has contacted as many authorised "
            + "family mediators as have an office within fifteen miles of his or her home (or three of them if "
            + "there are three or more), and all of them have stated that they are not available to conduct a MIAM "
            + "within fifteen business days of the date of contact; and (ii) the names, postal addresses and telephone numbers"
            + " or e-mail addresses for such authorised family mediators, and the dates of contact, can be provided"
            + " to the court if requested."),
    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_11("FR_ms_MIAMOtherGroundsChecklist_Value_11",
        "There is no authorised family mediator with an office within fifteen miles "
            + "of the prospective applicant’s home."),

    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_12("FR_ms_MIAMOtherGroundsChecklist_Value_12",
        "(i) the prospective applicant is not able to attend a MIAM online or by video-link and an explanation of why "
            + "this is the case is provided to the court using the text field provided; and (ii) the prospective applicant "
            + "has contacted as many authorised family mediators as have an office within fifteen miles of his or her home "
            + "(or five of them if there are five or more), and all of them have stated that they are not available to conduct"
            + " a MIAM within fifteen business days of the date of contact; and (iii) the names, postal addresses and telephone "
            + "numbers or e-mail addresses for the authorised family mediators contacted by the prospective applicant, and "
            + "the dates of contact, are provided to the court in the text field provided."),

    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_13("FR_ms_MIAMOtherGroundsChecklist_Value_13",
        "(i) the prospective applicant is not able to attend a MIAM online or by video-link and an explanation of why this "
            + "is the case is provided to the court using the text field provided; and (ii) the prospective applicant is "
            + "subject to a disability or other inability that would prevent attendance in person at a MIAM unless appropriate "
            + "facilities can be offered by an authorised mediator; and (iii) the prospective applicant has contacted as many "
            + "authorised family mediators as have an office within fifteen miles of his or her home (or five of them if there "
            + "are five or more), and all have stated that they are unable to provide such facilities; and (iv) the names, "
            + "postal addresses and telephone numbers or e-mail addresses for such authorised family mediators, and the dates "
            + "of contact, are provided to the court using the text field provided."),

    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_14("FR_ms_MIAMOtherGroundsChecklist_Value_14",
        "(i) the prospective applicant is not able to attend a MIAM online or by video-link; and (ii) there is no "
            + "authorised family mediator with an office within fifteen miles of the prospective applicant’s home; and (iii) "
            + "an explanation of why this exemption applies is provided by the prospective applicant to the court using the "
            + "text field provided."),

    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_15("FR_ms_MIAMOtherGroundsChecklist_Value_15",
        "The prospective applicant cannot attend a MIAM because the prospective applicant is (i) in prison or any "
            + "other institution in which the prospective applicant is required to be detained and facilities cannot be "
            + "made available for them to attend a MIAM online or by video link; or (ii) subject to conditions of bail "
            + "that prevent contact with the other person; or (iii) subject to a licence with a prohibited contact "
            + "requirement in relation to the other person."),
    FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_16("FR_ms_MIAMOtherGroundsChecklist_Value_16",
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

    public static MiamOtherGrounds forValue(String value) {
        return Arrays.stream(MiamOtherGrounds.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
