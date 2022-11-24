package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum BristolCourt implements CourtList {
    FR_bristolList_1("FR_bristolList_1"),
    FR_bristolList_2("FR_bristolList_2"),
    FR_bristolList_3("FR_bristolList_3"),
    FR_bristolList_4("FR_bristolList_4"),
    FR_bristolList_5("FR_bristolList_5"),
    FR_bristolList_6("FR_bristolList_6");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static BristolCourt getBristolCourt(String ccdType) {
        return Arrays.stream(BristolCourt.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public String getSelectedCourtId() {
        return id;
    }
}
