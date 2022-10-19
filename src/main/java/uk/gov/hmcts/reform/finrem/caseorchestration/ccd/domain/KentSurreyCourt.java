package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum KentSurreyCourt implements CourtList {
    FR_kent_surreyList_1("FR_kent_surrey_hc_list_1"),
    FR_kent_surreyList_2("FR_kent_surrey_hc_list_2"),
    FR_kent_surreyList_3("FR_kent_surrey_hc_list_3"),
    FR_kent_surreyList_4("FR_kent_surrey_hc_list_4"),
    FR_kent_surreyList_5("FR_kent_surrey_hc_list_5"),
    FR_kent_surreyList_6("FR_kent_surrey_hc_list_6"),
    FR_kent_surreyList_7("FR_kent_surrey_hc_list_7"),
    FR_kent_surreyList_8("FR_kent_surrey_hc_list_8"),
    FR_kent_surreyList_9("FR_kent_surrey_hc_list_9"),
    FR_kent_surreyList_10("FR_kent_surrey_hc_list_10"),
    CONSENTED_FR_kent_surreyList_1("FR_kent_surreyList_1"),
    CONSENTED_FR_kent_surreyList_2("FR_kent_surreyList_2"),
    CONSENTED_FR_kent_surreyList_3("FR_kent_surreyList_3"),
    CONSENTED_FR_kent_surreyList_4("FR_kent_surreyList_4"),
    CONSENTED_FR_kent_surreyList_5("FR_kent_surreyList_5"),
    CONSENTED_FR_kent_surreyList_6("FR_kent_surreyList_6"),
    CONSENTED_FR_kent_surreyList_7("FR_kent_surreyList_7"),
    CONSENTED_FR_kent_surreyList_8("FR_kent_surreyList_8"),
    CONSENTED_FR_kent_surreyList_9("FR_kent_surreyList_9"),
    CONSENTED_FR_kent_surreyList_10("FR_kent_surreyList_10");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static KentSurreyCourt getKentSurreyCourt(String ccdType) {
        return Arrays.stream(KentSurreyCourt.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public String getSelectedCourtId() {
        return id;
    }
}
