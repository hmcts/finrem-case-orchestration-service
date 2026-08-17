package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_northwalesList", generate = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum NorthWalesCourt implements CourtList {
    @CCD(label = "Wrexham County Court and Family Court")
    WREXHAM("FR_northwalesList_1"),
    @CCD(label = "Caernarfon Justice Centre")
    CAERNARFON("FR_northwalesList_2"),
    @CCD(label = "Prestatyn Justice Centre")
    PRESTATYN("FR_northwalesList_3"),
    @CCD(label = "Welshpool Civil and Family Court")
    WELSHPOOL("FR_northwalesList_4"),
    @CCD(label = "Mold County")
    MOLD("FR_northwalesList_5"),
    @CCD(label = "Llundudno Magistrates Court")
    LLUNDUDNO("FR_northwalesList_6");

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
