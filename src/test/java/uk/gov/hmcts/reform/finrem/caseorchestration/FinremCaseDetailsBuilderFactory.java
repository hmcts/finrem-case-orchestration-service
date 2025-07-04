package uk.gov.hmcts.reform.finrem.caseorchestration;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData.FinremCaseDataBuilder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails.FinremCaseDetailsBuilder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;

public class FinremCaseDetailsBuilderFactory {

    private FinremCaseDetailsBuilderFactory() {
        // all access through static methods
    }

    public static FinremCaseDetailsBuilder from() {
        return from(null, null, (FinremCaseDataBuilder) null);
    }

    public static FinremCaseDetailsBuilder from(Long id) {
        return from(id, null, (FinremCaseDataBuilder) null);
    }

    public static FinremCaseDetailsBuilder from(CaseType caseType) {
        return from(null, caseType, (FinremCaseDataBuilder) null);
    }

    public static FinremCaseDetailsBuilder from(Long id, CaseType caseType) {
        return from(id, caseType, (FinremCaseDataBuilder) null);
    }

    public static FinremCaseDetailsBuilder from(Long id, CaseType caseType, FinremCaseDataBuilder caseDataBuilder) {
        return from(id, caseType, caseDataBuilder == null ? FinremCaseData.builder().ccdCaseType(caseType).build()
            : caseDataBuilder.ccdCaseType(caseType).build());
    }

    public static FinremCaseDetailsBuilder from(Long id, FinremCaseDataBuilder caseDataBuilder) {
        return from(id, null, caseDataBuilder.build());
    }

    public static FinremCaseDetailsBuilder from(CaseType caseType, FinremCaseDataBuilder caseDataBuilder) {
        return from((Long) null, caseType, (caseDataBuilder == null ? FinremCaseData.builder() : caseDataBuilder)
            .ccdCaseType(caseType).build());
    }

    public static FinremCaseDetailsBuilder from(FinremCaseDataBuilder caseDataBuilder) {
        return from((Long) null, null, caseDataBuilder.build());
    }

    public static FinremCaseDetailsBuilder from(Long id, CaseType caseType, FinremCaseData caseData) {
        return from(id, caseType, caseData, null);
    }

    public static FinremCaseDetailsBuilder from(String id, CaseType caseType, FinremCaseData caseData) {
        return from(id, caseType, caseData, null);
    }

    public static FinremCaseDetailsBuilder from(String id, CaseType caseType, FinremCaseData caseData, State state) {
        return from(Long.parseLong(id), caseType, caseData, state);
    }

    public static FinremCaseDetailsBuilder from(Long id, CaseType caseType, FinremCaseData caseData, State state) {
        if (caseData != null) {
            caseData.setCcdCaseType(caseType); // synchronise caseType to caseData
        }
        return FinremCaseDetails.builder().id(id).caseType(caseType).data(caseData).state(state);
    }
}
