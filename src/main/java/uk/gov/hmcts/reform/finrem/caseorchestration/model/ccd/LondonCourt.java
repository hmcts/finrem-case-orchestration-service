package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum LondonCourt implements CourtList {
    CENTRAL_FAMILY_COURT("FR_londonList_1"),
    WILLESDEN_COUNTY_COURT_AND_FAMILY_COURT("FR_londonList_2"),
    UXBRIDGE_COUNTY_COURT_AND_FAMILY_COURT("FR_londonList_3"),
    EAST_LONDON_FAMILY_COURT("FR_londonList_4"),
    BRENTFORD_COUNTY_AND_FAMILY_COURT("FR_londonList_5"),
    BARNET_CIVIL_AND_FAMILY_COURTS_CENTRE("FR_londonList_6"),
    ROMFORDCOUNTY_AND_FAMILY_COURT("FR_londonList_7"),
    KINGSTON_UPON_THAMES_COUNTY_COURT_AND_FAMILY_COURT("FR_londonList_8"),
    EDMONTON_COOUNTY_COURT_AND_FAMILY_COURT("FR_londonList_9"),
    CROYDON_COUNTY_COURT_AND_FAMILY_COURT("FR_londonList_10"),
    BROMLEY_COUNTY_COURT_AND_FAMILY_COURT("FR_londonList_11"),
    MIGRATION_TEMP_HC("FR_londonList_12");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static LondonCourt getLondonCourt(String ccdType) {
        return Arrays.stream(LondonCourt.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public String getSelectedCourtId() {
        return id;
    }
}
