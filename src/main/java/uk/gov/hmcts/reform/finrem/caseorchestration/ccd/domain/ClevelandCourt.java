package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum ClevelandCourt implements CourtList {
    FR_CLEVELAND_LIST_1("FR_cleaveland_hc_list_1"),
    FR_CLEVELAND_LIST_2("FR_cleaveland_hc_list_2"),
    FR_CLEVELAND_LIST_3("FR_cleaveland_hc_list_3"),
    FR_CLEVELAND_LIST_4("FR_cleaveland_hc_list_4"),
    FR_CLEVELAND_LIST_5("FR_cleaveland_hc_list_5"),
    FR_CLEVELAND_LIST_6("FR_cleaveland_hc_list_6"),
    FR_CLEVELAND_LIST_7("FR_cleaveland_hc_list_7"),
    FR_CLEVELAND_LIST_8("FR_cleaveland_hc_list_8");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static ClevelandCourt getCleavelandCourt(String ccdType) {
        return Arrays.stream(ClevelandCourt.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public String getSelectedCourtId() {
        return id;
    }
}
