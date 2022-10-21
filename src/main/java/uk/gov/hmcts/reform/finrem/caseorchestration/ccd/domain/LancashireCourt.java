package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum LancashireCourt implements CourtList {
    PRESTON_COURT("FR_lancashireList_1"),
    BLACKBURN_COURT("FR_lancashireList_2"),
    BLACKPOOL_COURT("FR_lancashireList_3"),
    LANCASTER_COURT("FR_lancashireList_4"),
    LEYLAND_COURT("FR_lancashireList_5"),
    REEDLEY_COURT("FR_lancashireList_6"),
    BARROW_COURT("FR_lancashireList_7"),
    CARLISLE_COURT("FR_lancashireList_8"),
    WEST_CUMBRIA_COURT("FR_lancashireList_9");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static LancashireCourt getLancashireCourt(String ccdType) {
        return Arrays.stream(LancashireCourt.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public String getSelectedCourtId() {
        return id;
    }
}
