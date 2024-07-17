package uk.gov.hmcts.reform.finrem.caseorchestration;

import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
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
        return FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .id(id)
                .data(caseData)
                .build())
            .build();
    }

    public static FinremCallbackRequest create() {
        return FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .data(FinremCaseData.builder().build())
                .build())
            .build();
    }
}
