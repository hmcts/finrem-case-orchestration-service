package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum ThamesValleyCourt implements CourtList {
    FR_thamesvalleyList_1("FR_thamesvalleyList_1"),
    FR_thamesvalleyList_2("FR_thamesvalleyList_2"),
    FR_thamesvalleyList_3("FR_thamesvalleyList_3"),
    FR_thamesvalleyList_4("FR_thamesvalleyList_4");

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
