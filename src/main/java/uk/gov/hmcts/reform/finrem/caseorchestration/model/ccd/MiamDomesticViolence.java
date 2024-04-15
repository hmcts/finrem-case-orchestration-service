package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum MiamDomesticViolence {
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_1("FR_ms_MIAMDomesticViolenceChecklist_Value_1",
        "Evidence that a prospective party has been arrested for a relevant domestic abuse offence"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_2("FR_ms_MIAMDomesticViolenceChecklist_Value_2",
        "Evidence of a relevant police caution for a domestic abuse offence"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_3("FR_ms_MIAMDomesticViolenceChecklist_Value_3",
        "Evidence of relevant criminal proceedings for a domestic abuse offence which have not concluded"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_4("FR_ms_MIAMDomesticViolenceChecklist_Value_4",
        "Evidence of a relevant conviction for a domestic abuse offence"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_5("FR_ms_MIAMDomesticViolenceChecklist_Value_5",
        "A court order binding a prospective party over in connection with a domestic abuse offence"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_6("FR_ms_MIAMDomesticViolenceChecklist_Value_6",
        "A domestic abuse protection notice issued under section 24 of the Crime and Security Act 2010 "
            + "against a prospective party"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_7("FR_ms_MIAMDomesticViolenceChecklist_Value_7",
        "A domestic abuse protection notice given under section 22 of the Domestic Abuse Act 2021 against "
            + "a prospective party"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_8("FR_ms_MIAMDomesticViolenceChecklist_Value_8",
        "A relevant protective injunction"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_9("FR_ms_MIAMDomesticViolenceChecklist_Value_9",
        "An undertaking given in England and Wales under section 46 or 63E of the Family Law Act 1996 "
            + "(or given in Scotland or Northern Ireland in place of a protective injunction) by a prospective "
            + "party, provided that a cross-undertaking relating to domestic violence or abuse was not given by "
            + "another prospective party"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_10("FR_ms_MIAMDomesticViolenceChecklist_Value_10",
        "A copy of a finding of fact, made in proceedings in the United Kingdom, that there has been domestic "
            + "abuse by a prospective party"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_11("FR_ms_MIAMDomesticViolenceChecklist_Value_11",
        "An expert report produced as evidence in proceedings in the United Kingdom for the benefit of a court "
            + "or tribunal confirming that a person with whom a prospective party is or was personally connected, "
            + "was assessed as being, or at risk of being, a victim of domestic abuse by that prospective party"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_12("FR_ms_MIAMDomesticViolenceChecklist_Value_12",
        "A letter or report from an appropriate health professional confirming that- \n"
            + "(i) that professional, or another appropriate health professional, has examined a "
            + "prospective party in person by telephone or by video conferencing, and (ii) in the "
            + "reasonable professional judgment of the author or the examining appropriate health "
            + "professional, that prospective party has, or has had, injuries or a condition consistent "
            + "with being a victim of domestic abuse"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_13("FR_ms_MIAMDomesticViolenceChecklist_Value_13",
        "A letter or report from- \n"
            + "(i) the appropriate health professional who made the referral described below; \n"
            + "(ii) an appropriate health professional who has access to the medical records of the prospective party referred to below; or \n"
            + "(iii) the person to whom the referral described below was made; \n"
            + "confirming that there was a referral by an appropriate health professional of a prospective party "
            + "to a person who provides specialist support or assistance for victims of, or those at risk of, "
            + "domestic abuse"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_14("FR_ms_MIAMDomesticViolenceChecklist_Value_14",
        "A letter from any person who is a member of a multi-agency risk assessment conference (or other "
            + "suitable local safeguarding forum) confirming that a prospective party, or a person with whom "
            + "that prospective party is personally connected, is or has been at risk of harm from domestic "
            + "abuse by another prospective party"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_15("FR_ms_MIAMDomesticViolenceChecklist_Value_15",
        "A letter from an independent domestic abuse advisor (IDVA) confirming that they are or "
            + "have provided support to a prospective party"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_16("FR_ms_MIAMDomesticViolenceChecklist_Value_16",
        "A letter from an independent sexual violence advisor (ISVA) confirming that they are providing or "
            + "have provided support to a prospective party relating to sexual violence by another prospective party"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_17("FR_ms_MIAMDomesticViolenceChecklist_Value_17",
        "A letter from an officer employed by a local authority or housing association (or their equivalent "
            + "in Scotland or Northern Ireland) for the purpose of supporting tenants containing- \n"
            + "(i) a statement to the effect that, in their reasonable professional judgment, a person "
            + "with whom a prospective party is or has been personally connected to is, or is at risk of "
            + "being, a victim of domestic violence by that prospective party; (ii) a description of the "
            + "specific matters relied upon to support that judgment; and (iii) a description of the support "
            + "they provided to the victim of domestic violence or the person at risk of domestic abuse by "
            + "that prospective party"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_18("FR_ms_MIAMDomesticViolenceChecklist_Value_18",
        "A letter which- \n"
            + "(i) is from an organisation providing domestic abuse support services, which letter confirms that it- \n"
            + "(a) is situated in the United Kingdom, \n"
            + "(b) has been operating for an uninterrupted period of six months or more; and \n"
            + "(c) provided a prospective party with support in relation to that person’s needs as a victim, "
            + "or a person at risk, of domestic abuse; and \n"
            + "(ii) contains- \n"
            + "(a) a statement to the effect that, in the reasonable professional judgment of the author of "
            + "the letter, the prospective party is, or is at risk of being, a victim of domestic abuse; \n"
            + "(b) a description of the specific matters relied upon to support that judgment; \n"
            + "(c) a description of the support provided to the prospective party; and \n"
            + "(d) a statement of the reasons why the prospective party needed that support"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_19("FR_ms_MIAMDomesticViolenceChecklist_Value_19",
        "A letter or report from an organisation providing domestic abuse support services in the United Kingdom "
            + "confirming- \n"
            + "(i) that a person with whom a prospective party is or was personally connected was refused "
            + "admission to a refuge; \n"
            + "(ii) the date on which they were refused admission to the refuge; and \n"
            + "(iii) they sought admission to the refuge because of allegations of domestic violence by the "
            + "prospective party referred to in paragraph (i)"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_20("FR_ms_MIAMDomesticViolenceChecklist_Value_20",
        "A letter from a public authority confirming that a person with whom a prospective party is "
            + "or was personally connected, was assessed as being, or at risk of being, a victim of domestic "
            + "abuse by that prospective party (or a copy of that assessment)"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_21("FR_ms_MIAMDomesticViolenceChecklist_Value_21",
        "A letter from the Secretary of State for the Home Department confirming that a prospective "
            + "party has been granted leave to remain in the United Kingdom as a victim of domestic abuse"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_22("FR_ms_MIAMDomesticViolenceChecklist_Value_22",
        "Evidence which demonstrates that a prospective party has been, or is at risk of being, the "
            + "victim of domestic abuse by another prospective party in the form of abuse which relates to"
            + " financial matters"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_23("FR_ms_MIAMDomesticViolenceChecklist_Value_23",
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

    public static MiamDomesticViolence forValue(String value) {
        return Arrays.stream(MiamDomesticViolence.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
