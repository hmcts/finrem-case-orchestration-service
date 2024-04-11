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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.generalorder.FinremGeneralOrderRaisedConsentInContestedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.generalorder.FinremGeneralOrderRaisedContestedCorresponder;

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
class  CreateGeneralOrderContestedSubmittedTest {

    @InjectMocks
    private CreateGeneralOrderContestedSubmittedHandler handler;

    @Mock
    private FinremCaseDetailsMapper mapper;

    @Mock
    private FinremGeneralOrderRaisedConsentInContestedCorresponder finremGeneralOrderRaisedConsentInContestedCorresponder;

    @Mock
    private FinremGeneralOrderRaisedContestedCorresponder finremGeneralOrderRaisedContestedCorresponder;

    @ParameterizedTest
    @MethodSource
    void testCanHandle(CallbackType callbackType, CaseType caseType, EventType eventType, boolean expected) {
        assertThat(handler.canHandle(callbackType, caseType, eventType)).isEqualTo(expected);
    }

    private static Stream<Arguments> testCanHandle() {
        return Stream.of(
            // Contested
            Arguments.of(ABOUT_TO_START, CONTESTED, GENERAL_ORDER, false),
            Arguments.of(MID_EVENT, CONTESTED, GENERAL_ORDER, false),
            Arguments.of(ABOUT_TO_SUBMIT, CONTESTED, GENERAL_ORDER, false),
            Arguments.of(SUBMITTED, CONTESTED, GENERAL_ORDER, true),
            Arguments.of(SUBMITTED, CONTESTED, GENERAL_ORDER_CONSENT_IN_CONTESTED, true),
            Arguments.of(ABOUT_TO_START, CONTESTED, GENERAL_ORDER_CONSENT_IN_CONTESTED, false),
            Arguments.of(MID_EVENT, CONTESTED, GENERAL_ORDER_CONSENT_IN_CONTESTED, false),
            Arguments.of(ABOUT_TO_SUBMIT, CONTESTED, GENERAL_ORDER_CONSENT_IN_CONTESTED, false),

            // Consented
            Arguments.of(ABOUT_TO_START, CONSENTED, GENERAL_ORDER, false),
            Arguments.of(MID_EVENT, CONSENTED, GENERAL_ORDER, false),
            Arguments.of(ABOUT_TO_SUBMIT, CONSENTED, GENERAL_ORDER, false),
            Arguments.of(SUBMITTED, CONSENTED, GENERAL_ORDER, false),

            Arguments.of(SUBMITTED, CONSENTED, GENERAL_ORDER_CONSENT_IN_CONTESTED, false),
            Arguments.of(SUBMITTED, CONTESTED, ASSIGN_DOCUMENT_CATEGORIES, false),
            Arguments.of(SUBMITTED, CONSENTED, ASSIGN_DOCUMENT_CATEGORIES, false)
        );
    }

    @Test
    void testHandleConsentInContested() {
        ConsentOrderWrapper consentOrderWrapper = ConsentOrderWrapper.builder().consentD81Question(YesOrNo.YES).build();
        FinremCaseData finremCaseData = FinremCaseData.builder().consentOrderWrapper(consentOrderWrapper).build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().caseType(CONTESTED).data(finremCaseData).build();
        FinremCallbackRequest request = FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventType(GENERAL_ORDER_CONSENT_IN_CONTESTED)
            .build();

        var response = handler.handle(request, "some-token");

        assertThat(response).isNotNull();
        verify(finremGeneralOrderRaisedConsentInContestedCorresponder, times(1)).sendCorrespondence(caseDetails);
    }

    @Test
    void testHandleContested() {
            FinremCaseDetails caseDetails = new FinremCaseDetails();
            FinremCallbackRequest request = FinremCallbackRequest.builder()
                .caseDetails(caseDetails)
                .eventType(GENERAL_ORDER)
                .build();

            var response = handler.handle(request, "some-token");

            assertThat(response).isNotNull();
        verify(finremGeneralOrderRaisedContestedCorresponder, times(1)).sendCorrespondence(caseDetails);
    }
}
