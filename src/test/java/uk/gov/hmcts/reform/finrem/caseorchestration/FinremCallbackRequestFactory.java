package uk.gov.hmcts.reform.finrem.caseorchestration;

import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

public class FinremCallbackRequestFactory {

    private FinremCallbackRequestFactory() {
        // all access through static methods
    }

    public static FinremCallbackRequest fromId(Long id) {
        return from(id, FinremCaseData.builder().build());
    }

    public static FinremCallbackRequest from(FinremCaseData caseData) {
        return from(null, caseData);
    }

    public static FinremCallbackRequest from(Long id, FinremCaseData caseData) {
        return from(id, null, caseData);
    }

    public static FinremCallbackRequest from(Long id, CaseType caseType, FinremCaseData caseData) {
        return from(FinremCaseDetailsBuilderFactory.from(id, caseType, caseData));
    }

    public static FinremCallbackRequest from(Long id, FinremCaseData.FinremCaseDataBuilder caseDataBuilder) {
        return from(id, null, caseDataBuilder);
    }

    public static FinremCallbackRequest from(Long id, CaseType caseType, FinremCaseData.FinremCaseDataBuilder caseDataBuilder) {
        return from(FinremCaseDetails.builder()
            .id(id)
            .caseType(caseType)
            .data(caseDataBuilder.build()));
    }

    public static FinremCallbackRequest from(FinremCaseDetails.FinremCaseDetailsBuilder caseDetailsBuilder) {
        return FinremCallbackRequest.builder()
            .caseDetails(caseDetailsBuilder.build())
            .build();
    }

    public static FinremCallbackRequest from(EventType eventType, FinremCaseDetails.FinremCaseDetailsBuilder caseDetailsBuilder) {
        return FinremCallbackRequest.builder()
            .eventType(eventType)
            .caseDetails(caseDetailsBuilder.build())
            .build();
    }

    public static FinremCallbackRequest create() {
        return FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetailsBuilderFactory.from().build())
            .build();
    }
}
