package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum NorthWalesCourt implements CourtList {
    FR_northwalesList_1("FR_northwalesList_1"),
    FR_northwalesList_2("FR_northwalesList_2"),
    FR_northwalesList_3("FR_northwalesList_3"),
    FR_northwalesList_4("FR_northwalesList_4"),
    FR_northwalesList_5("FR_northwalesList_5");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static NorthWalesCourt getNorthWalesCourt(String ccdType) {
        return Arrays.stream(NorthWalesCourt.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public String getSelectedCourtId() {
        return id;
    }
}
