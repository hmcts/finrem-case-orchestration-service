package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_dorsetList", generate = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum DorsetCourt implements CourtList {
    @CCD(label = "BOURNEMOUTH AND POOLE COUNTY COURT AND FAMILY COURT")
    BOURNEMOUTH("FR_dorsetList_1"),
    @CCD(label = "WEYMOUTH COMBINED COURT")
    WEYMOUTH("FR_dorsetList_2"),
    @CCD(label = "WINCHESTER COMBINED COURT CENTRE")
    WINCHESTER("FR_dorsetList_3"),
    @CCD(label = "PORTSMOUTH COMBINED COURT CENTRE")
    PORTSMOUTH("FR_dorsetList_4"),
    @CCD(label = "SOUTHAMPTON COMBINED COURT CENTRE")
    SOUTHAMPTON("FR_dorsetList_5"),
    @CCD(label = "ALDERSHOT JUSTICE CENTRE")
    ALDERSHOT("FR_dorsetList_6"),
    @CCD(label = "BASINGSTOKE COUNTY AND FAMILY COURT")
    BASINGSTOKE("FR_dorsetList_7"),
    @CCD(label = "NEWPORT (ISLE OF WIGHT) COMBINED COURT")
    ISLE_OF_WIGHT("FR_dorsetList_8");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static DorsetCourt getDorsetCourt(String ccdType) {
        return Arrays.stream(DorsetCourt.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public String getSelectedCourtId() {
        return id;
    }
}
