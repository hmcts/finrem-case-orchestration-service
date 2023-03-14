package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum DevonCourt implements CourtList {
    PLYMOUTH("FR_devonList_1"),
    EXETER("FR_devonList_2"),
    TAUNTON("FR_devonList_3"),
    TORQUAY("FR_devonList_4"),
    BARNSTAPLE("FR_devonList_5"),
    TRURO("FR_devonList_6"),
    YEOVIL("FR_devonList_7"),
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
