package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum ManchesterCourt implements CourtList {
    MANCHESTER_COURT("FR_manchester_hc_list_1"),
    STOCKPORT_COURT("FR_manchester_hc_list_2"),
    WIGAN_COURT("FR_manchester_hc_list_3"),
    CONSENTED_MANCHESTER_COURT("FR_manchesterList_1"),
    CONSENTED_STOCKPORT_COURT("FR_manchesterList_2"),
    CONSENTED_WIGAN_COURT("FR_manchesterList_3");


    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static ManchesterCourt getManchesterCourt(String ccdType) {
        return Arrays.stream(ManchesterCourt.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public String getSelectedCourtId() {
        return id;
    }
}
