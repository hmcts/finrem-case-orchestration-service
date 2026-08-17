package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_lancashireList", generate = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum LancashireCourt implements CourtList {
    @CCD(label = "PRESTON DESIGNATED FAMILY COURT")
    PRESTON_COURT("FR_lancashireList_1"),
    @CCD(label = "BLACKBURN FAMILY COURT")
    BLACKBURN_COURT("FR_lancashireList_2"),
    @CCD(label = "BLACKPOOL FAMILY COURT")
    BLACKPOOL_COURT("FR_lancashireList_3"),
    @CCD(label = "LANCASTER COURTHOUSE")
    LANCASTER_COURT("FR_lancashireList_4"),
    @CCD(label = "LEYLAND FAMILY HEARING CENTRE")
    LEYLAND_COURT("FR_lancashireList_5"),
    @CCD(label = "REEDLEY FAMILY HEARING CENTRE")
    REEDLEY_COURT("FR_lancashireList_6"),
    @CCD(label = "BARROW IN FURNESS COUNTY AND FAMILY COURT")
    BARROW_COURT("FR_lancashireList_7"),
    @CCD(label = "CARLISLE COMBINED COURT")
    CARLISLE_COURT("FR_lancashireList_8"),
    @CCD(label = "WEST CUMBRIA COURTHOUSE")
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
