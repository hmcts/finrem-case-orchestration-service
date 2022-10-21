package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum LiverpoolCourt implements CourtList {
    LIVERPOOL_CIVIL_FAMILY_COURT("FR_liverpool_hc_list_1"),
    CHESTER_CIVIL_FAMILY_JUSTICE("FR_liverpool_hc_list_2"),
    CREWE_COUNTY_FAMILY_COURT("FR_liverpool_hc_list_3"),
    ST_HELENS_COUNTY_FAMILY_COURT("FR_liverpool_hc_list_4"),
    BIRKENHEAD_COUNTY_FAMILY_COURT("FR_liverpool_hc_list_5"),
    CONSENTED_LIVERPOOL_CIVIL_FAMILY_COURT("FR_liverpoolList_1"),
    CONSENTED_CHESTER_CIVIL_FAMILY_JUSTICE("FR_liverpoolList_2"),
    CONSENTED_CREWE_COUNTY_FAMILY_COURT("FR_liverpoolList_3"),
    CONSENTED_ST_HELENS_COUNTY_FAMILY_COURT("FR_liverpoolList_4"),
    CONSENTED_BIRKENHEAD_COUNTY_FAMILY_COURT("FR_liverpoolList_5");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static LiverpoolCourt getLiverpoolCourt(String ccdType) {
        return Arrays.stream(LiverpoolCourt.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public String getSelectedCourtId() {
        return id;
    }
}
