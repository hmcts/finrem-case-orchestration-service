package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum NottinghamCourt implements CourtList {

    NOTTINGHAM_COUNTY_COURT_AND_FAMILY_COURT("FR_s_NottinghamList_1"),
    DERBY_COMBINED_COURT_CENTRE("FR_s_NottinghamList_2"),
    LEICESTER_COUNTY_COURT_AND_FAMILY_COURT("FR_s_NottinghamList_3"),
    LINCOLN_COUNTY_COURT_AND_FAMILY_COURT("FR_s_NottinghamList_4"),
    NORTHAMPTON_CROWN_COUNTY_AND_FAMILY_COURT("FR_s_NottinghamList_5"),
    CHESTERFIELD_COUNTY_COURT("FR_s_nottinghamList_6"),
    MANSFIELD_MAGISTRATES_AND_COUNTY_COURT("FR_s_NottinghamList_7"),
    BOSTON_COUNTY_COURT_AND_FAMILY_COURT("FR_s_NottinghamList_8"),
    CONSENTED_NOTTINGHAM_COUNTY_COURT_AND_FAMILY_COURT("FR_nottinghamList_1"),
    CONSENTED_DERBY_COMBINED_COURT_CENTRE("FR_nottinghamList_2"),
    CONSENTED_LEICESTER_COUNTY_COURT_AND_FAMILY_COURT("FR_nottinghamList_3"),
    CONSENTED_LINCOLN_COUNTY_COURT_AND_FAMILY_COURT("FR_nottinghamList_4"),
    CONSENTED_NORTHAMPTON_CROWN_COUNTY_AND_FAMILY_COURT("FR_nottinghamList_5"),
    CONSENTED_CHESTERFIELD_COUNTY_COURT("FR_nottinghamList_6"),
    CONSENTED_MANSFIELD_MAGISTRATES_AND_COUNTY_COURT("FR_nottinghamList_7"),
    CONSENTED_BOSTON_COUNTY_COURT_AND_FAMILY_COURT("FR_nottinghamList_8");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static NottinghamCourt getNottinghamCourt(String ccdType) {
        return Arrays.stream(NottinghamCourt.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public String getSelectedCourtId() {
        return id;
    }
}
