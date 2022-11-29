package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum DorsetCourt implements CourtList {
    FR_DORSET_LIST_1("FR_dorsetList_1"),
    FR_DORSET_LIST_2("FR_dorsetList_2"),
    FR_DORSET_LIST_3("FR_dorsetList_3"),
    FR_DORSET_LIST_4("FR_dorsetList_4"),
    FR_DORSET_LIST_5("FR_dorsetList_5"),
    FR_DORSET_LIST_6("FR_dorsetList_6"),
    FR_DORSET_LIST_7("FR_dorsetList_7"),
    FR_DORSET_LIST_8("FR_dorsetList_8");

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
