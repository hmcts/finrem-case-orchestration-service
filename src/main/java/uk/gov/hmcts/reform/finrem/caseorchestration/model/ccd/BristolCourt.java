package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_bristolList", generate = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum BristolCourt implements CourtList {
    @CCD(label = "BRISTOL CIVIL AND FAMILY JUSTICE CENTRE")
    BRISTOL_CIVIL_AND_FAMILY_JUSTICE_CENTRE("FR_bristolList_1"),
    @CCD(label = "GLOUCESTER AND CHELTENHAM COUNTY AND FAMILY COURT")
    GLOUCESTER_AND_CHELTENHAM_COUNTY_AND_FAMILY_COURT("FR_bristolList_2"),
    @CCD(label = "SWINDON COMBINED COURT")
    SWINDON_COMBINED_COURT("FR_bristolList_3"),
    @CCD(label = "SALISBURY LAW COURTS")
    SALISBURY_LAW_COURTS("FR_bristolList_4"),
    @CCD(label = "BATH LAW COURTS")
    BATH_LAW_COURTS("FR_bristolList_5"),
    @CCD(label = "WESTON SUPER MARE COUNTY AND FAMILY COURT")
    WESTON_SUPER_MARE_COUNTY_AND_FAMILY_COURT("FR_bristolList_6"),
    @CCD(label = "BRISTOL MAGISTRATES COURT")
    BRISTOL_MAGISTRATES_COURT("FR_bristolList_7"),
    @CCD(label = "SWINDON MAGISTRATES COURT")
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
