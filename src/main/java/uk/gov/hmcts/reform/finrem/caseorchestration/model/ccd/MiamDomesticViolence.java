package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum MiamDomesticViolence {
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_1("FR_ms_MIAMDomesticViolenceChecklist_Value_1",
        "Evidence that a prospective party has been arrested for a relevant domestic violence offence;"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_2("FR_ms_MIAMDomesticViolenceChecklist_Value_2",
        "Evidence of a relevant police caution for a domestic violence offence;"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_3("FR_ms_MIAMDomesticViolenceChecklist_Value_3",
        "Evidence of relevant criminal proceedings for a domestic violence offence which have not concluded;"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_4("FR_ms_MIAMDomesticViolenceChecklist_Value_4",
        "Evidence of a relevant conviction for a domestic violence offence;"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_5("FR_ms_MIAMDomesticViolenceChecklist_Value_5",
        "A court order binding a prospective party over in connection with a domestic violence offence;"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_6("FR_ms_MIAMDomesticViolenceChecklist_Value_6",
        "A domestic violence protection notice issued under section 24 of the Crime and Security Act 2010 against "
            + "a prospective party;"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_7("FR_ms_MIAMDomesticViolenceChecklist_Value_7",
        "A relevant protective injunction;"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_8("FR_ms_MIAMDomesticViolenceChecklist_Value_8",
        "An undertaking given in England and Wales under section 46 or 63E of the Family Law Act 1996 (or given "
            + "in Scotland or Northern Ireland in place of a protective injunction) by a prospective party, provided "
            + "that a cross-undertaking relating; cont..."),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_9("FR_ms_MIAMDomesticViolenceChecklist_Value_9",
        "A copy of a finding of fact, made in proceedings in the United Kingdom, that there has been domestic "
            + "violence by a prospective party;"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_10("FR_ms_MIAMDomesticViolenceChecklist_Value_10",
        "An expert report produced as evidence in proceedings in the United Kingdom for the benefit of a court or "
            + "tribunal confirming that a person with whom a prospective party is or was in a family relationship, was "
            + "assessed as being, or cont...."),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_11("FR_ms_MIAMDomesticViolenceChecklist_Value_11",
        "A letter or report from an appropriate health professional confirming that-\n(i) that professional, or "
            + "another appropriate health professional, has examined a prospective party in person; and\n(ii) in the "
            + "reasonable professional judgment cont…"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_12("FR_ms_MIAMDomesticViolenceChecklist_Value_12",
        "A letter or report from-\n(i) the appropriate health professional who made the referral described below; "
            + "\n(ii) an appropriate health professional who has access to the medical records of the prospective party"
            + " referred to below; or cont.."),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_13("FR_ms_MIAMDomesticViolenceChecklist_Value_13",
        "A letter from any person who is a member of a multi-agency risk assessment conference (or other suitable "
            + "local safeguarding forum) confirming that a prospective party, or a person with whom that prospective "
            + "party is in"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_14("FR_ms_MIAMDomesticViolenceChecklist_Value_14",
        "A family relationship, is or has been at risk of harm from domestic violence by another prospective "
            + "party;"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_15("FR_ms_MIAMDomesticViolenceChecklist_Value_15",
        "A letter from an independent domestic violence advisor confirming that they are providing support to a "
            + "prospective party;"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_16("FR_ms_MIAMDomesticViolenceChecklist_Value_16",
        "A letter from an independent sexual violence advisor confirming that they are providing support to a "
            + "prospective party relating to sexual violence by another prospective party;"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_17("FR_ms_MIAMDomesticViolenceChecklist_Value_17",
        "A letter from an officer employed by a local authority or housing association (or their equivalent in "
            + "Scotland or Northern Ireland) for the purpose of supporting tenants containing- \n(i) a statement to the"
            + " effect that,  cont …"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_18("FR_ms_MIAMDomesticViolenceChecklist_Value_18",
        "A letter which- \n(i) is from an organization providing domestic violence support services, or a "
            + "registered charity, which letter confirms that it- \n(a) is situated in England and Wales, \n(b) has "
            + "been operating for an uninterrupted cont...."),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_19("FR_ms_MIAMDomesticViolenceChecklist_Value_19",
        "A letter or report from an organisation providing domestic violence support services in the United "
            + "Kingdom confirming- \n(i) that a person with whom a prospective party is or was in a family relationship"
            + " was refused admission to a refuge; cont"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_20("FR_ms_MIAMDomesticViolenceChecklist_Value_20",
        "A letter from a public authority confirming that a person with whom a prospective party is or was in a "
            + "family relationship, was assessed as being, or at risk of being, a victim of domestic violence by that "
            + "prospective party cont…"),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_21("FR_ms_MIAMDomesticViolenceChecklist_Value_21",
        "A letter from the Secretary of State for the Home Department confirming that a prospective party has "
            + "been granted leave to remain in the United Kingdom under paragraph 289B of the Rules made by the Home "
            + "Secretary under section 3(2) cont.."),
    FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_22("FR_ms_MIAMDomesticViolenceChecklist_Value_22",
        "Evidence which demonstrates that a prospective party has been, or is at risk of being, the victim of "
            + "domestic violence by another prospective party in the form of abuse which relates to financial "
            + "matters.");

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
