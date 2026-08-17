package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_bedfordshireList", generate = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum BedfordshireCourt implements CourtList {
    @CCD(label = "PETERBOROUGH COMBINED COURT CENTRE")
    PETERBOROUGH("FR_bedfordshireList_1"),
    @CCD(label = "CAMBRIDGE COUNTY COURT AND FAMILY COURT")
    CAMBRIDGE("FR_bedfordshireList_2"),
    @CCD(label = "BURY ST EDMUNDS COUNTY COURT AND FAMILY COURT")
    BURY("FR_bedfordshireList_3"),
    @CCD(label = "NORWICH COMBINED COURT CENTRE")
    NORWICH("FR_bedfordshireList_4"),
    @CCD(label = "IPSWICH COUNTY COURT AND FAMILY HEARING CENTRE")
    IPSWICH("FR_bedfordshireList_5"),
    @CCD(label = "CHELMSFORD JUSTICE CENTRE")
    CHELMSFORD("FR_bedfordshireList_6"),
    @CCD(label = "SOUTHEND COUNTY COURT AND FAMILY COURT")
    SOUTHEND("FR_bedfordshireList_7"),
    @CCD(label = "BEDFORD COUNTY COURT AND FAMILY COURT")
    BEDFORD("FR_bedfordshireList_8"),
    @CCD(label = "LUTON JUSTICE CENTRE")
    LUTON("FR_bedfordshireList_9"),
    @CCD(label = "HERTFORD COUNTY COURT AND FAMILY COURT")
    HERTFORD("FR_bedfordshireList_10"),
    @CCD(label = "WATFORD COUNTY COURT AND FAMILY COURT")
    WATFORD("FR_bedfordshireList_11"),
    @CCD(label = "GREAT YARMOUTH MAGISTRATES AND FAMILY COURT")
    GREAT_YARMOUTH("FR_bedfordshireList_12"),
    @CCD(label = "KING'S LYNN MAGISTRATES COURT")
    KINGS_LYNN("FR_bedfordshireList_13");

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
