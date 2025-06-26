package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;

import java.util.Arrays;

@RequiredArgsConstructor
public enum HearingTypeDirection {
    FDA("First Directions Appointment (FDA)"),
    FDR("Financial Dispute Resolution (FDR)"),
    FH("Final Hearing (FH)"),
    DIR("Directions (DIR)");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static HearingTypeDirection getHearingTypeDirection(String ccdType) {
        return Arrays.stream(HearingTypeDirection.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    public HearingType toHearingType() {
        return HearingType.valueOf(this.name());
    }
}
