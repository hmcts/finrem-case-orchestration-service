package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum InterimTypeOfHearing {
    MPS("Maintenance Pending Suit (MPS)"),
    FDA("First Directions Appointment (FDA)"),
    FDR("Financial Dispute Resolution (FDR)"),
    FH("Final Hearing (FH)"),
    DIR("Directions (DIR)");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static InterimTypeOfHearing getInterimTypeOfHearing(String ccdType) {
        return Arrays.stream(InterimTypeOfHearing.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
