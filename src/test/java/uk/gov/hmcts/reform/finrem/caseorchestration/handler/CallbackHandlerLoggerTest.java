package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_APP_DETAILS;

class CallbackHandlerLoggerTest {

    @ParameterizedTest
    @MethodSource("testAboutToStart")
    void testAboutToStart(FinremCallbackRequest request, String expectedLog) {
        assertThat(CallbackHandlerLogger.aboutToStart(request)).isEqualTo(expectedLog);
    }

    private static Stream<Arguments> testAboutToStart() {
        return Stream.of(
            Arguments.of(FinremCallbackRequestFactory.from(3575975624441189L, CaseType.CONTESTED, AMEND_APP_DETAILS),
                "===> Case ID 3575975624441189: Handling CONTESTED About To Start callback for event FR_amendApplicationDetails"),
            Arguments.of(FinremCallbackRequestFactory.from(5437803016442134L, CaseType.CONSENTED, EventType.DRAFT_ORDERS),
                "===> Case ID 5437803016442134: Handling CONSENTED About To Start callback for event FR_draftOrders")
        );
    }

    @ParameterizedTest
    @MethodSource("testMidEvent")
    void testMidEvent(FinremCallbackRequest request, String expectedLog) {
        assertThat(CallbackHandlerLogger.midEvent(request)).isEqualTo(expectedLog);
    }

    private static Stream<Arguments> testMidEvent() {
        return Stream.of(
            Arguments.of(FinremCallbackRequestFactory.from(3575975624441189L, CaseType.CONTESTED, AMEND_APP_DETAILS),
                "===> Case ID 3575975624441189: Handling CONTESTED Mid-Event callback for event FR_amendApplicationDetails"),
            Arguments.of(FinremCallbackRequestFactory.from(5437803016442134L, CaseType.CONSENTED, EventType.DRAFT_ORDERS),
                "===> Case ID 5437803016442134: Handling CONSENTED Mid-Event callback for event FR_draftOrders")
        );
    }

    @ParameterizedTest
    @MethodSource("testAboutToSubmit")
    void testAboutToSubmit(FinremCallbackRequest request, String expectedLog) {
        assertThat(CallbackHandlerLogger.aboutToSubmit(request)).isEqualTo(expectedLog);
    }

    private static Stream<Arguments> testAboutToSubmit() {
        return Stream.of(
            Arguments.of(FinremCallbackRequestFactory.from(3575975624441189L, CaseType.CONTESTED, AMEND_APP_DETAILS),
                "===> Case ID 3575975624441189: Handling CONTESTED About To Submit callback for event FR_amendApplicationDetails"),
            Arguments.of(FinremCallbackRequestFactory.from(5437803016442134L, CaseType.CONSENTED, EventType.DRAFT_ORDERS),
                "===> Case ID 5437803016442134: Handling CONSENTED About To Submit callback for event FR_draftOrders")
        );
    }

    @ParameterizedTest
    @MethodSource("testSubmitted")
    void testSubmitted(FinremCallbackRequest request, String expectedLog) {
        assertThat(CallbackHandlerLogger.submitted(request)).isEqualTo(expectedLog);
    }

    private static Stream<Arguments> testSubmitted() {
        return Stream.of(
            Arguments.of(FinremCallbackRequestFactory.from(3575975624441189L, CaseType.CONTESTED, AMEND_APP_DETAILS),
                "===> Case ID 3575975624441189: Handling CONTESTED Submitted callback for event FR_amendApplicationDetails"),
            Arguments.of(FinremCallbackRequestFactory.from(5437803016442134L, CaseType.CONSENTED, EventType.DRAFT_ORDERS),
                "===> Case ID 5437803016442134: Handling CONSENTED Submitted callback for event FR_draftOrders")
        );
    }
}
