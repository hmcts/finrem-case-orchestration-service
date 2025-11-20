package uk.gov.hmcts.reform.finrem.caseorchestration;

import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;

public class FinremCallbackRequestFactory {

    private FinremCallbackRequestFactory() {
        // static factory only
    }

    /* -----------------------------------------------------------
       Core helpers
       ----------------------------------------------------------- */

    private static FinremCaseDetails.FinremCaseDetailsBuilder details(Long id,
                                                                      CaseType type,
                                                                      FinremCaseData data,
                                                                      State state) {
        if (data != null && id != null) {
            data.setCcdCaseId(String.valueOf(id));
        }
        return FinremCaseDetails.builder()
            .id(id)
            .caseType(type)
            .state(state)
            .data(data);
    }

    private static FinremCallbackRequest single(FinremCaseDetails.FinremCaseDetailsBuilder details) {
        return FinremCallbackRequest.builder()
            .caseDetails(details.build())
            .caseDetailsBefore(details.build())
            .build();
    }

    private static FinremCallbackRequest pair(FinremCaseDetails.FinremCaseDetailsBuilder before,
                                              FinremCaseDetails.FinremCaseDetailsBuilder after) {
        return FinremCallbackRequest.builder()
            .caseDetails(after.build())
            .caseDetailsBefore(before.build())
            .build();
    }

    /* -----------------------------------------------------------
       Public creation methods
       ----------------------------------------------------------- */

    public static FinremCallbackRequest fromId(Long id) {
        FinremCaseData data = FinremCaseData.builder()
            .ccdCaseId(id == null ? null : String.valueOf(id))
            .build();

        return single(details(id, null, data, null));
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
        return pair(
            details(id, null, before, null),
            details(id, null, after, null)
        );
    }

    public static FinremCallbackRequest from(Long id,
                                             CaseType type,
                                             FinremCaseData data) {
        return single(details(id, type, data, null));
    }

    public static FinremCallbackRequest from(Long id,
                                             CaseType type,
                                             FinremCaseData data,
                                             State state) {
        return single(details(id, type, data, state));
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
        return pair(before, after);
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
                                             CaseType type,
                                             EventType eventType) {
        FinremCaseData data = FinremCaseData.builder()
            .ccdCaseId(String.valueOf(id))
            .build();

        FinremCaseDetails.FinremCaseDetailsBuilder d =
            details(id, type, data, null);

        return FinremCallbackRequest.builder()
            .eventType(eventType)
            .caseDetails(d.build())
            .caseDetailsBefore(d.build())
            .build();
    }

    public static FinremCallbackRequest create() {
        return single(FinremCaseDetailsBuilderFactory.from());
    }

    public static FinremCallbackRequest create(Long id,
                                               CaseType type,
                                               EventType eventType,
                                               FinremCaseData data) {

        data.setCcdCaseType(type);

        return FinremCallbackRequest.builder()
            .eventType(eventType)
            .caseDetails(details(id, type, data, null).build())
            .build();
    }

    public static FinremCallbackRequest create(Long id,
                                               CaseType type,
                                               EventType eventType,
                                               FinremCaseData data,
                                               FinremCaseData before) {

        data.setCcdCaseType(type);
        before.setCcdCaseType(type);

        return FinremCallbackRequest.builder()
            .eventType(eventType)
            .caseDetails(details(id, type, data, null).build())
            .caseDetailsBefore(details(id, type, before, null).build())
            .build();
    }
}
