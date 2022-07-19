package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum CaseType {

    CONSENTED("FinancialRemedyMVP2"),
    CONTESTED("FinancialRemedyContested");

    private final String ccdType;

    public String getCcdType() {
        return ccdType;
    }

    public static CaseType getCaseType(String ccdType) {
        return Arrays.stream(CaseType.values())
            .filter(caseTypeValue -> caseTypeValue.ccdType.equals(ccdType))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}