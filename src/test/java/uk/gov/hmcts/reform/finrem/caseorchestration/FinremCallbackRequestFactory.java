package uk.gov.hmcts.reform.finrem.caseorchestration;

import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockingDetails;

public class FinremCallbackRequestFactory {

    private FinremCallbackRequestFactory() {
        // static factory only
    }

    /* -----------------------------------------------------------
       Core helpers
       ----------------------------------------------------------- */

    private static FinremCaseDetails.FinremCaseDetailsBuilder details(Long id,
                                                                      CaseType caseType,
                                                                      FinremCaseData data,
                                                                      State state) {
        setCaseIdAndType(id, null, data);
        return FinremCaseDetails.builder()
            .id(id)
            .caseType(caseType)
            .state(state)
            .data(data);
    }

    private static FinremCallbackRequest single(FinremCaseDetails.FinremCaseDetailsBuilder details) {
        return FinremCallbackRequest.builder()
            .caseDetails(details.build())
            .caseDetailsBefore(details.build())
            .build();
    }

    private static FinremCallbackRequest pair(EventType eventType, FinremCaseDetails.FinremCaseDetailsBuilder before,
                                              FinremCaseDetails.FinremCaseDetailsBuilder after) {
        return FinremCallbackRequest.builder()
            .eventType(eventType)
            .caseDetails(after.build())
            .caseDetailsBefore(before.build())
            .build();
    }

    /* -----------------------------------------------------------
       Public creation methods
       ----------------------------------------------------------- */

    public static FinremCallbackRequest fromId(Long id) {
        return single(details(id, null, FinremCaseData.builder().build(), null));
    }

    public static FinremCallbackRequest from() {
        return single(details(null, null, FinremCaseData.builder().build(), null));
    }

    public static FinremCallbackRequest from(FinremCaseData data) {
        return single(details(null, null, data, null));
    }

    public static FinremCallbackRequest from(Long id, FinremCaseData data) {
        return single(details(id, null, data, null));
    }

    public static FinremCallbackRequest from(Long id,
                                             FinremCaseData before,
                                             FinremCaseData after) {
        return pair(null,
            details(id, null, before, null),
            details(id, null, after, null)
        );
    }

    public static FinremCallbackRequest from(Long id,
                                             CaseType caseType,
                                             FinremCaseData data) {
        return single(details(id, caseType, data, null));
    }

    public static FinremCallbackRequest from(Long id,
                                             CaseType caseType,
                                             FinremCaseData data,
                                             State state) {
        return single(details(id, caseType, data, state));
    }

    public static FinremCallbackRequest from(Long id,
                                             FinremCaseData data,
                                             State state) {
        return single(details(id, null, data, state));
    }

    public static FinremCallbackRequest from(FinremCaseDetails.FinremCaseDetailsBuilder builder) {
        return single(builder);
    }

    public static FinremCallbackRequest from(FinremCaseDetails.FinremCaseDetailsBuilder before,
                                             FinremCaseDetails.FinremCaseDetailsBuilder after) {
        return pair(null, before, after);
    }

    public static FinremCallbackRequest from(EventType eventType,
                                             FinremCaseDetails.FinremCaseDetailsBuilder builder) {
        return FinremCallbackRequest.builder()
            .eventType(eventType)
            .caseDetails(builder.build())
            .build();
    }

    public static FinremCallbackRequest from(EventType eventType,
                                             FinremCaseDetails.FinremCaseDetailsBuilder before,
                                             FinremCaseDetails.FinremCaseDetailsBuilder after) {
        return FinremCallbackRequest.builder()
            .eventType(eventType)
            .caseDetails(after.build())
            .caseDetailsBefore(before.build())
            .build();
    }

    public static FinremCallbackRequest from(FinremCaseDetails details) {
        return FinremCallbackRequest.builder()
            .caseDetails(details)
            .build();
    }

    public static FinremCallbackRequest from(Long id,
                                             CaseType caseType,
                                             EventType eventType) {
        FinremCaseDetails.FinremCaseDetailsBuilder after =
            details(id, caseType, FinremCaseData.builder()
                .ccdCaseId(String.valueOf(id))
                .build(), null);

        FinremCaseDetails.FinremCaseDetailsBuilder before =
            details(id, caseType, FinremCaseData.builder()
                .ccdCaseId(String.valueOf(id))
                .build(), null);

        return pair(eventType, before, after);
    }

    public static FinremCallbackRequest from(Long id,
                                             CaseType caseType,
                                             EventType eventType,
                                             FinremCaseData data) {
        return FinremCallbackRequest.builder()
            .eventType(eventType)
            .caseDetails(details(id, caseType, data, null).build())
            .build();
    }

    public static FinremCallbackRequest from(Long id,
                                             CaseType caseType,
                                             EventType eventType,
                                             FinremCaseData data,
                                             FinremCaseData before) {
        return FinremCallbackRequest.builder()
            .eventType(eventType)
            .caseDetails(details(id, caseType, data, null).build())
            .caseDetailsBefore(details(id, caseType, before, null).build())
            .build();
    }

    private static void setCaseIdAndType(Long id, CaseType caseType, FinremCaseData caseData) {
        if (caseData == null || id == null) {
            return;
        }

        if (mockingDetails(caseData).isMock()) {
            lenient().when(caseData.getCcdCaseId()).thenReturn(String.valueOf(id));
        } else {
            caseData.setCcdCaseId(String.valueOf(id));
        }

        caseData.setCcdCaseType(caseType);
    }
}
