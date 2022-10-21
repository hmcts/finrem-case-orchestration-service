package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum PotentialAllegation {
    POTENTIAL_ALLEGATION_CHECKLIST_1("potentialAllegationChecklist_1",
        "Pre- or post-nuptial agreements"),
    POTENTIAL_ALLEGATION_CHECKLIST_2("potentialAllegationChecklist_2",
        "Complex asset or income structures"),
    POTENTIAL_ALLEGATION_CHECKLIST_3("potentialAllegationChecklist_3",
        "Assets are / were held through the medium of trusts / settlements/ family/ unquoted corporate "
            + "entities or otherwise held offshore or overseas"),
    POTENTIAL_ALLEGATION_CHECKLIST_4("potentialAllegationChecklist_4",
        "The value of family assets, trust and/or corporate entities"),
    POTENTIAL_ALLEGATION_CHECKLIST_5("potentialAllegationChecklist_5",
        "Non-disclosure of assets"),
    POTENTIAL_ALLEGATION_CHECKLIST_6("potentialAllegationChecklist_6",
        "Expert accountancy evidence will be required"),
    POTENTIAL_ALLEGATION_CHECKLIST_7("potentialAllegationChecklist_7",
        "There are substantial arguments concerning the illiquidity of assets"),
    POTENTIAL_ALLEGATION_CHECKLIST_8("potentialAllegationChecklist_8",
        "There may be substantial arguments about which assets are “matrimonial assets” or “non-matrimonial assets”"),
    POTENTIAL_ALLEGATION_CHECKLIST_9("potentialAllegationChecklist_9",
        "There may be substantial arguments about the parties’ respective contributions"),
    POTENTIAL_ALLEGATION_CHECKLIST_10("potentialAllegationChecklist_10",
        "There are/may be disputed allegations of “obvious and gross” conduct"),
    POTENTIAL_ALLEGATION_CHECKLIST_11("potentialAllegationChecklist_11",
        "The case involves an application under Schedule 1 Children Act 1989"),
    POTENTIAL_ALLEGATION_CHECKLIST_12("potentialAllegationChecklist_12",
        "The application involves a complex or novel legal argument"),
    POTENTIAL_ALLEGATION_CHECKLIST_13("potentialAllegationChecklist_13",
        "There is likely to be a need for the involvement of Intervenors"),
    POTENTIAL_ALLEGATION_CHECKLIST_14("potentialAllegationChecklist_14",
        "The case involves an insolvency issue"),
    NOT_APPLICABLE("notApplicable", "Not applicable");

    private final String value;
    private final String text;

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public static PotentialAllegation forValue(String value) {
        return Arrays.stream(PotentialAllegation.values())
            .filter(option -> value.equalsIgnoreCase(option.getValue()))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
