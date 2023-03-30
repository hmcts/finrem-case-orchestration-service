package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum NorthWalesCourt implements CourtList {
    WREXHAM("FR_northwalesList_1"),
    CAERNARFON("FR_northwalesList_2"),
    PRESTATYN("FR_northwalesList_3"),
    WELSHPOOL("FR_northwalesList_4"),
    MOLD("FR_northwalesList_5");

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
