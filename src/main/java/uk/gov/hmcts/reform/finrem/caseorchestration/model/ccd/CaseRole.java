package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

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
}
