package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

import static java.lang.String.format;

@RequiredArgsConstructor
public enum CaseRole {
    APP_SOLICITOR("[APPSOLICITOR]"),
    APP_BARRISTER("[APPBARRISTER]"),
    RESP_SOLICITOR("[RESPSOLICITOR]"),
    RESP_BARRISTER("[RESPBARRISTER]"),
    CASEWORKER("[CASEWORKER]"),
    CREATOR("[CREATOR]"),
    INTVR_SOLICITOR_1("[INTVRSOLICITOR1]"),
    INTVR_SOLICITOR_2("[INTVRSOLICITOR2]"),
    INTVR_SOLICITOR_3("[INTVRSOLICITOR3]"),
    INTVR_SOLICITOR_4("[INTVRSOLICITOR4]"),
    INTVR_BARRISTER_1("[INTVRBARRISTER1]"),
    INTVR_BARRISTER_2("[INTVRBARRISTER2]"),
    INTVR_BARRISTER_3("[INTVRBARRISTER3]"),
    INTVR_BARRISTER_4("[INTVRBARRISTER4]");

    private final String ccdCode;

    @JsonValue
    public String getCcdCode() {
        return ccdCode;
    }

    public static CaseRole forValue(String ccdCode) {
        return Arrays.stream(CaseRole.values())
            .filter(option -> option.getCcdCode().equalsIgnoreCase(ccdCode))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    /**
     * Returns the {@link CaseRole} for an intervener solicitor at the given index.
     *
     * <p>The index represents the intervener number (for example, 1, 2, 3).
     * This method constructs the corresponding case role value in the format
     * {@code [INTVRSOLICITOR{index}]}.</p>
     *
     * @param index the intervener index
     * @return the matching intervener solicitor {@link CaseRole}
     */
    public static CaseRole getIntervenerSolicitorByIndex(int index) {
        return CaseRole.forValue(format("[INTVRSOLICITOR%s]", index));
    }

    /**
     * Returns the {@link CaseRole} for an intervener barrister at the given index.
     *
     * <p>The index represents the intervener number (for example, 1, 2, 3).
     * This method constructs the corresponding case role value in the format
     * {@code [INTVRBARRISTER{index}]}.</p>
     *
     * @param index the intervener index
     * @return the matching intervener barrister {@link CaseRole}
     */
    public static CaseRole getIntervenerBarristerByIndex(int index) {
        return CaseRole.forValue(format("[INTVRBARRISTER%s]", index));
    }
}
