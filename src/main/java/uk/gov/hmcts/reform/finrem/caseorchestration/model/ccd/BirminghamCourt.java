package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum BirminghamCourt implements CourtList {
    BIRMINGHAM_CIVIL_AND_FAMILY_JUSTICE_CENTRE("FR_birmingham_hc_list_1"),
    COVENTRY_COMBINED_COURT_CENTRE("FR_birmingham_hc_list_2"),
    TELFORD_COUNTY_COURT_AND_FAMILY_COURT("FR_birmingham_hc_list_3"),
    WOLVERHAMPTON_COMBINED_COURT_CENTRE("FR_birmingham_hc_list_4"),
    DUDLEY_COUNTY_COURT_AND_FAMILY_COURT("FR_birmingham_hc_list_5"),
    WALSALL_COUNTY_AND_FAMILY_COURT("FR_birmingham_hc_list_6"),
    STOKE_ON_TRENT_COMBINED_COURT("FR_birmingham_hc_list_7"),
    WORCESTER_COMBINED_COURT("FR_birmingham_hc_list_8"),
    STAFFORD_COMBINED_COURT("FR_birmingham_hc_list_9"),
    HEREFORD_COUNTY_COURT_AND_FAMILY_COURT("FR_birmingham_hc_list_10"),
    CONSENTED_BIRMINGHAM_CIVIL_AND_FAMILY_JUSTICE_CENTRE("FR_birminghamList_1"),
    CONSENTED_COVENTRY_COMBINED_COURT_CENTRE("FR_birminghamList_2"),
    CONSENTED_TELFORD_COUNTY_COURT_AND_FAMILY_COURT("FR_birminghamList_3"),
    CONSENTED_WOLVERHAMPTON_COMBINED_COURT_CENTRE("FR_birminghamList_4"),
    CONSENTED_DUDLEY_COUNTY_COURT_AND_FAMILY_COURT("FR_birminghamList_5"),
    CONSENTED_WALSALL_COUNTY_AND_FAMILY_COURT("FR_birminghamList_6"),
    CONSENTED_STOKE_ON_TRENT_COMBINED_COURT("FR_birminghamList_7"),
    CONSENTED_WORCESTER_COMBINED_COURT("FR_birminghamList_8"),
    CONSENTED_STAFFORD_COMBINED_COURT("FR_birminghamList_9"),
    CONSENTED_HEREFORD_COUNTY_COURT_AND_FAMILY_COURT("FR_birminghamList_10");


    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static BirminghamCourt getBirminghamCourt(String ccdType) {
        return Arrays.stream(BirminghamCourt.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public String getSelectedCourtId() {
        return id;
    }
}
