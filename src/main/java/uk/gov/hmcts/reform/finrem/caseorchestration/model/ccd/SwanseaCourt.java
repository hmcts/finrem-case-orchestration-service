package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum SwanseaCourt implements CourtList {
    FR_swanseaList_1("FR_swansea_hc_list_1"),
    FR_swanseaList_2("FR_swansea_hc_list_2"),
    FR_swanseaList_3("FR_swansea_hc_list_3"),
    FR_swanseaList_4("FR_swansea_hc_list_4"),
    FR_swanseaList_5("FR_swansea_hc_list_5"),
    FR_swanseaList_6("FR_swansea_hc_list_6"),
    CONSENTED_FR_swanseaList_1("FR_swanseaList_1"),
    CONSENTED_FR_swanseaList_2("FR_swanseaList_2"),
    CONSENTED_FR_swanseaList_3("FR_swanseaList_3"),
    CONSENTED_FR_swanseaList_4("FR_swanseaList_4"),
    CONSENTED_FR_swanseaList_5("FR_swanseaList_5"),
    CONSENTED_FR_swanseaList_6("FR_swanseaList_6");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static SwanseaCourt getSwanseaCourt(String ccdType) {
        return Arrays.stream(SwanseaCourt.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public String getSelectedCourtId() {
        return id;
    }
}
