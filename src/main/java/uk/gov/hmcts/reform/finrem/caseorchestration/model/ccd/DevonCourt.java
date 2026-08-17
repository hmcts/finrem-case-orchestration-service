package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_devonList", generate = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum DevonCourt implements CourtList {
    @CCD(label = "PLYMOUTH COMBINED COURT")
    PLYMOUTH("FR_devonList_1"),
    @CCD(label = "EXETER COMBINED COURT CENTRE")
    EXETER("FR_devonList_2"),
    @CCD(label = "TAUNTON CROWN, COUNTY AND FAMILY COURT")
    TAUNTON("FR_devonList_3"),
    @CCD(label = "TORQUAY AND NEWTON ABBOT COUNTY AND FAMILY COURT")
    TORQUAY("FR_devonList_4"),
    @CCD(label = "BARNSTAPLE MAGISTRATES, COUNTY AND FAMILY COURT")
    BARNSTAPLE("FR_devonList_5"),
    @CCD(label = "TRURO COUNTY COURT AND FAMILY COURT")
    TRURO("FR_devonList_6"),
    @CCD(label = "YEOVIL COUNTY, FAMILY AND MAGISTRATES COURT")
    YEOVIL("FR_devonList_7"),
    @CCD(label = "BODMIN COUNTY COURT AND FAMILY COURT")
    BODMIN("FR_devonList_8");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static DevonCourt getDevonCourt(String ccdType) {
        return Arrays.stream(DevonCourt.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public String getSelectedCourtId() {
        return id;
    }
}
