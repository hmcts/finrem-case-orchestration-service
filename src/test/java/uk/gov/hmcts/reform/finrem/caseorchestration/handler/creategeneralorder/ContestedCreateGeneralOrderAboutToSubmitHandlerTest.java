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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
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
class ContestedCreateGeneralOrderAboutToSubmitHandlerTest {

    @InjectMocks
    private ContestedCreateGeneralOrderAboutToSubmitHandler handler;

    @Mock
    private FinremCaseDetailsMapper mapper;

    @Mock
    private CaseDataService caseDataService;

    @Mock
    private GeneralOrderService generalOrderService;

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
            Arguments.of(ABOUT_TO_SUBMIT, CONTESTED, GENERAL_ORDER, true),
            Arguments.of(SUBMITTED, CONTESTED, GENERAL_ORDER, false),
            Arguments.of(ABOUT_TO_START, CONTESTED, GENERAL_ORDER_CONSENT_IN_CONTESTED, false),
            Arguments.of(MID_EVENT, CONTESTED, GENERAL_ORDER_CONSENT_IN_CONTESTED, false),
            Arguments.of(ABOUT_TO_SUBMIT, CONTESTED, GENERAL_ORDER_CONSENT_IN_CONTESTED, true),
            Arguments.of(SUBMITTED, CONTESTED, GENERAL_ORDER_CONSENT_IN_CONTESTED, false),

            // Consented
            Arguments.of(ABOUT_TO_START, CONSENTED, GENERAL_ORDER, false),
            Arguments.of(MID_EVENT, CONSENTED, GENERAL_ORDER, false),
            Arguments.of(ABOUT_TO_SUBMIT, CONSENTED, GENERAL_ORDER, false),
            Arguments.of(SUBMITTED, CONSENTED, GENERAL_ORDER, false),

            Arguments.of(ABOUT_TO_SUBMIT, CONSENTED, GENERAL_ORDER_CONSENT_IN_CONTESTED, false),
            Arguments.of(ABOUT_TO_SUBMIT, CONTESTED, ASSIGN_DOCUMENT_CATEGORIES, false),
            Arguments.of(ABOUT_TO_SUBMIT, CONSENTED, ASSIGN_DOCUMENT_CATEGORIES, false)
        );
    }

    @Test
    void testHandleContestedCase() {
        FinremCaseData caseData = new FinremCaseData();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .caseType(CONTESTED)
            .data(caseData)
            .build();
        FinremCallbackRequest request = FinremCallbackRequest.builder()
            .eventType(GENERAL_ORDER)
            .caseDetails(caseDetails)
            .build();

        var response = handler.handle(request, "some-token");
        assertThat(response).isNotNull();
        verify(generalOrderService, times(1))
            .addContestedGeneralOrderToCollection(caseData);
        verifyNoMoreInteractions(generalOrderService);
    }

    @Test
    void testHandleConsentedInContestedCase() {
        FinremCaseData caseData = createCaseWithConsentOrder();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .caseType(CONTESTED)
            .data(caseData)
            .build();
        FinremCallbackRequest request = FinremCallbackRequest.builder()
            .eventType(GENERAL_ORDER_CONSENT_IN_CONTESTED)
            .caseDetails(caseDetails)
            .build();
        when(caseDataService.hasConsentOrder(caseData)).thenReturn(true);

        var response = handler.handle(request, "some-token");
        assertThat(response).isNotNull();
        verify(generalOrderService, times(1))
            .addConsentedInContestedGeneralOrderToCollection(caseData);
        verifyNoMoreInteractions(generalOrderService);
    }

    private FinremCaseData createCaseWithConsentOrder() {
        ConsentOrderWrapper consentOrderWrapper = new ConsentOrderWrapper();
        consentOrderWrapper.setConsentD81Question(YesOrNo.YES);

        return FinremCaseData.builder()
            .consentOrderWrapper(consentOrderWrapper)
            .build();
    }
}
