package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum DorsetCourt implements CourtList {
    BOURNEMOUTH("FR_dorsetList_1"),
    WEYMOUTH("FR_dorsetList_2"),
    WINCHESTER("FR_dorsetList_3"),
    PORTSMOUTH("FR_dorsetList_4"),
    SOUTHAMPTON("FR_dorsetList_5"),
    ALDERSHOT("FR_dorsetList_6"),
    BASINGSTOKE("FR_dorsetList_7"),
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
