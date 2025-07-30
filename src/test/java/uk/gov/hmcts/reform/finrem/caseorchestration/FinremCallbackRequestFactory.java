package uk.gov.hmcts.reform.finrem.caseorchestration;

import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;

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
        return from(id, null, caseData, null);
    }

    public static FinremCallbackRequest from(Long id, FinremCaseData caseData, State state) {
        return from(id, null, caseData, state);
    }

    public static FinremCallbackRequest from(Long id, CaseType caseType, FinremCaseData caseData) {
        return from(FinremCaseDetailsBuilderFactory.from(id, caseType, caseData));
    }

    public static FinremCallbackRequest from(Long id, CaseType caseType, FinremCaseData caseData, State state) {
        return from(FinremCaseDetailsBuilderFactory.from(id, caseType, caseData, state));
    }

    public static FinremCallbackRequest from(FinremCaseDetails.FinremCaseDetailsBuilder caseDetailsBuilder) {
        return from(caseDetailsBuilder, caseDetailsBuilder);
    }

    public static FinremCallbackRequest from(FinremCaseDetails.FinremCaseDetailsBuilder caseDetailsBeforeBuilder,
                                             FinremCaseDetails.FinremCaseDetailsBuilder caseDetailsBuilder) {
        return FinremCallbackRequest.builder()
            .caseDetails(caseDetailsBuilder.build())
            .caseDetailsBefore(caseDetailsBeforeBuilder.build())
            .build();
    }

    public static FinremCallbackRequest from(EventType eventType, FinremCaseDetails.FinremCaseDetailsBuilder caseDetailsBuilder) {
        return FinremCallbackRequest.builder()
            .eventType(eventType)
            .caseDetails(caseDetailsBuilder.build())
            .build();
    }

    public static FinremCallbackRequest from(FinremCaseDetails caseDetails) {
        return FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }

    public static FinremCallbackRequest from(Long id, CaseType caseType, EventType eventType) {
        return FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .id(id)
                .caseType(caseType)
                .build())
            .eventType(eventType)
            .build();
    }

    public static FinremCallbackRequest create() {
        return FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetailsBuilderFactory.from().build())
            .build();
    }

    public static FinremCallbackRequest create(Long id, CaseType caseType, EventType eventType, FinremCaseData caseData) {
        caseData.setCcdCaseType(caseType);
        return FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .id(id)
                .caseType(caseType)
                .data(caseData)
                .build())
            .eventType(eventType)
            .build();
    }

    public static FinremCallbackRequest create(Long id, CaseType caseType, EventType eventType, FinremCaseData caseData,
                                               FinremCaseData caseDataBefore) {
        caseData.setCcdCaseType(caseType);
        caseDataBefore.setCcdCaseType(caseType);

        return FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .id(id)
                .caseType(caseType)
                .data(caseData)
                .build())
            .caseDetailsBefore(FinremCaseDetails.builder()
                .id(id)
                .caseType(caseType)
                .data(caseDataBefore)
                .build())
            .eventType(eventType)
            .build();
    }
}
