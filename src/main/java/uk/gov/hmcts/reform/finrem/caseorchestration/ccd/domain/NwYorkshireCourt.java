package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum NwYorkshireCourt implements CourtList {
    HARROGATE_COURT("FR_nw_yorkshire_hc_list_1"),
    BRADFORD_COURT("FR_nw_yorkshire_hc_list_2"),
    HUDDERSFIELD_COURT("FR_nw_yorkshire_hc_list_3"),
    WAKEFIELD_COURT("FR_nw_yorkshire_hc_list_4"),
    YORK_COURT("FR_nw_yorkshire_hc_list_5"),
    SCARBOROUGH_COURT("FR_nw_yorkshire_hc_list_6"),
    LEEDS_COURT("FR_nw_yorkshire_hc_list_7"),
    PRESTON_COURT("FR_nw_yorkshire_hc_list_8"),
    CONSENTED_HARROGATE_COURT("FR_nw_yorkshireList_1"),
    CONSENTED_BRADFORD_COURT("FR_nw_yorkshireList_2"),
    CONSENTED_HUDDERSFIELD_COURT("FR_nw_yorkshireList_3"),
    CONSENTED_WAKEFIELD_COURT("FR_nw_yorkshireList_4"),
    CONSENTED_YORK_COURT("FR_nw_yorkshireList_5"),
    CONSENTED_SCARBOROUGH_COURT("FR_nw_yorkshireList_6"),
    CONSENTED_LEEDS_COURT("FR_nw_yorkshireList_7"),
    CONSENTED_PRESTON_COURT("FR_nw_yorkshireList_8");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static NwYorkshireCourt getNwYorkshireCourt(String ccdType) {
        return Arrays.stream(NwYorkshireCourt.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public String getSelectedCourtId() {
        return id;
    }
}
