package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum BedfordshireCourt implements CourtList {
    PETERBOROUGH("FR_bedfordshireList_1"),
    CAMBRIDGE("FR_bedfordshireList_2"),
    BURY("FR_bedfordshireList_3"),
    NORWICH("FR_bedfordshireList_4"),
    IPSWICH("FR_bedfordshireList_5"),
    CHELMSFORD("FR_bedfordshireList_6"),
    SOUTHEND("FR_bedfordshireList_7"),
    BEDFORD("FR_bedfordshireList_8"),
    LUTON("FR_bedfordshireList_9"),
    HERTFORD("FR_bedfordshireList_10"),
    WATFORD("FR_bedfordshireList_11");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static BedfordshireCourt getBedfordshireCourt(String ccdType) {
        return Arrays.stream(BedfordshireCourt.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public String getSelectedCourtId() {
        return id;
    }
}
