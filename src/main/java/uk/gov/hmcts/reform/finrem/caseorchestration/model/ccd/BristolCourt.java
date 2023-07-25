package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum BristolCourt implements CourtList {
    BRISTOL_CIVIL_AND_FAMILY_JUSTICE_CENTRE("FR_bristolList_1"),
    GLOUCESTER_AND_CHELTENHAM_COUNTY_AND_FAMILY_COURT("FR_bristolList_2"),
    SWINDON_COMBINED_COURT("FR_bristolList_3"),
    SALISBURY_LAW_COURTS("FR_bristolList_4"),
    BATH_LAW_COURTS("FR_bristolList_5"),
    WESTON_SUPER_MARE_COUNTY_AND_FAMILY_COURT("FR_bristolList_6"),
    BRISTOL_MAGISTRATES_COURT("FR_bristolList_7"),
    SWINDON_MAGISTRATES_COURT("FR_bristolList_8");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static BristolCourt getBristolCourt(String ccdType) {
        return Arrays.stream(BristolCourt.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public String getSelectedCourtId() {
        return id;
    }
}
