package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum DevonCourt implements CourtList {
    FR_devonList_1("FR_devonList_1"),
    FR_devonList_2("FR_devonList_2"),
    FR_devonList_3("FR_devonList_3"),
    FR_devonList_4("FR_devonList_4"),
    FR_devonList_5("FR_devonList_5"),
    FR_devonList_6("FR_devonList_6"),
    FR_devonList_7("FR_devonList_7"),
    FR_devonList_8("FR_devonList_8");

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
