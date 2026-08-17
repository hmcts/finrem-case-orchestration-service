package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_thamesvalleyList", generate = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum ThamesValleyCourt implements CourtList {
    @CCD(label = "OXFORD COMBINED COURT CENTRE")
    OXFORD("FR_thamesvalleyList_1"),
    @CCD(label = "READING COUNTY COURT AND FAMILY COURT")
    READING("FR_thamesvalleyList_2"),
    @CCD(label = "MILTON KEYNES COUNTY COURT AND FAMILY COURT")
    MILTON_KEYNES("FR_thamesvalleyList_3"),
    @CCD(label = "SLOUGH COUNTY COURT AND FAMILY COURT")
    SLOUGH("FR_thamesvalleyList_4");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static ThamesValleyCourt getThamesValleyCourt(String ccdType) {
        return Arrays.stream(ThamesValleyCourt.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public String getSelectedCourtId() {
        return id;
    }
}
