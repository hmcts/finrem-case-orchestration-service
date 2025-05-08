package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum HearingType {
    MPS("Maintenance Pending Suit (MPS)"),
    FDA("First Directions Appointment (FDA)"),
    FDR("Financial Dispute Resolution (FDR)"),
    FH("Final Hearing (FH)"),
    DIR("Directions (DIR)"),
    MENTION("Mention"),
    PERMISSION_TO_APPEAL("Permission to Appeal"),
    APPLICATION_HEARING("Application Hearing"),
    RETRIAL_HEARING("Retrial Hearing"),
    PTR("Pre-Trial Review");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static HearingType getManageHearingType(String ccdType) {
        return Arrays.stream(HearingType.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
