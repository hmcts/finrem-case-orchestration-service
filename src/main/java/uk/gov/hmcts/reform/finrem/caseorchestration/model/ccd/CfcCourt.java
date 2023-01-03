package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum CfcCourt implements CourtList {
    BROMLEY_COUNTY_COURT_AND_FAMILY_COURT("FR_s_CFCList_1"),
    CROYDON_COUNTY_COURT_AND_FAMILY_COURT("FR_s_CFCList_2"),
    EDMONTON_COOUNTY_COURT_AND_FAMILY_COURT("FR_s_CFCList_3"),
    KINGSTON_UPON_THAMES_COUNTY_COURT_AND_FAMILY_COURT("FR_s_CFCList_4"),
    ROMFORDCOUNTY_AND_FAMILY_COURT("FR_s_CFCList_5"),
    BARNET_CIVIL_AND_FAMILY_COURTS_CENTRE("FR_s_CFCList_6"),
    BRENTFORD_COUNTY_AND_FAMILY_COURT("FR_s_CFCList_8"),
    CENTRAL_FAMILY_COURT("FR_s_CFCList_9"),
    EAST_LONDON_FAMILY_COURT("FR_s_CFCList_11"),
    UXBRIDGE_COUNTY_COURT_AND_FAMILY_COURT("FR_s_CFCList_14"),
    WILLESDEN_COUNTY_COURT_AND_FAMILY_COURT("FR_s_CFCList_16"),
    THE_ROYAL_COURT_OF_JUSTICE("FR_s_CFCList_17"),
    MIGRATION_TEMP_HC("FR_s_londonList_12");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static CfcCourt getCfcCourt(String ccdType) {
        return Arrays.stream(CfcCourt.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public String getSelectedCourtId() {
        return id;
    }
}
