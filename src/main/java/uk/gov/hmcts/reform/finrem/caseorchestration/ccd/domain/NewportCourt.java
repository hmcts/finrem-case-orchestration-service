package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum NewportCourt implements CourtList {
    FR_newportList_1("FR_newport_hc_list_1"),
    FR_newportList_2("FR_newport_hc_list_2"),
    FR_newportList_3("FR_newport_hc_list_3"),
    FR_newportList_4("FR_newport_hc_list_4"),
    FR_newportList_5("FR_newport_hc_list_5"),
    CONSENTED_FR_newportList_1("FR_newportList_1"),
    CONSENTED_FR_newportList_2("FR_newportList_2"),
    CONSENTED_FR_newportList_3("FR_newportList_3"),
    CONSENTED_FR_newportList_4("FR_newportList_4"),
    CONSENTED_FR_newportList_5("FR_newportList_5");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static NewportCourt getNewportCourt(String ccdType) {
        return Arrays.stream(NewportCourt.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public String getSelectedCourtId() {
        return id;
    }
}
