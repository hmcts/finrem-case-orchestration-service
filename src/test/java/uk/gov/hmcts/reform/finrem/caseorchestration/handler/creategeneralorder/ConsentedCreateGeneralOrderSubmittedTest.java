package uk.gov.hmcts.reform.finrem.caseorchestration.handler.creategeneralorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.generalorder.FinremGeneralOrderRaisedCorresponder;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.ASSIGN_DOCUMENT_CATEGORIES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.GENERAL_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.GENERAL_ORDER_CONSENT_IN_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class ConsentedCreateGeneralOrderSubmittedTest {

    @InjectMocks
    private ConsentedCreateGeneralOrderSubmittedHandler handler;

    @Mock
    private FinremCaseDetailsMapper mapper;

    @Mock
    private FinremGeneralOrderRaisedCorresponder corresponder;

    @ParameterizedTest
    @MethodSource
    void testCanHandle(CallbackType callbackType, CaseType caseType, EventType eventType, boolean expected) {
        assertThat(handler.canHandle(callbackType, caseType, eventType)).isEqualTo(expected);
    }

    private static Stream<Arguments> testCanHandle() {
        return Stream.of(
            // Consented
            Arguments.of(ABOUT_TO_START, CONSENTED, GENERAL_ORDER, false),
            Arguments.of(MID_EVENT, CONSENTED, GENERAL_ORDER, false),
            Arguments.of(ABOUT_TO_SUBMIT, CONSENTED, GENERAL_ORDER, false),
            Arguments.of(SUBMITTED, CONSENTED, GENERAL_ORDER, true),

            // Contested
            Arguments.of(ABOUT_TO_START, CONTESTED, GENERAL_ORDER, false),
            Arguments.of(MID_EVENT, CONTESTED, GENERAL_ORDER, false),
            Arguments.of(ABOUT_TO_SUBMIT, CONTESTED, GENERAL_ORDER, false),
            Arguments.of(SUBMITTED, CONTESTED, GENERAL_ORDER, false),
            Arguments.of(ABOUT_TO_START, CONTESTED, GENERAL_ORDER_CONSENT_IN_CONTESTED, false),
            Arguments.of(MID_EVENT, CONTESTED, GENERAL_ORDER_CONSENT_IN_CONTESTED, false),
            Arguments.of(ABOUT_TO_SUBMIT, CONTESTED, GENERAL_ORDER_CONSENT_IN_CONTESTED, false),
            Arguments.of(SUBMITTED, CONTESTED, GENERAL_ORDER_CONSENT_IN_CONTESTED, false),

            Arguments.of(SUBMITTED, CONSENTED, GENERAL_ORDER_CONSENT_IN_CONTESTED, false),
            Arguments.of(SUBMITTED, CONTESTED, ASSIGN_DOCUMENT_CATEGORIES, false),
            Arguments.of(SUBMITTED, CONSENTED, ASSIGN_DOCUMENT_CATEGORIES, false)
        );
    }

    @Test
    void testHandle() {
        FinremCaseDetails caseDetails = new FinremCaseDetails();
        FinremCallbackRequest request = FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        var response = handler.handle(request, "some-token");

        assertThat(response).isNotNull();
        verify(corresponder, times(1)).sendCorrespondence(caseDetails);
    }
}
