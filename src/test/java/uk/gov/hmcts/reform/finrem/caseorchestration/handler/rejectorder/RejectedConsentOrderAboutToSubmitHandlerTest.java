package uk.gov.hmcts.reform.finrem.caseorchestration.handler.rejectorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalOption;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.RefusalOrderDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.RefusedConsentOrderDocumentCategoriser;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State.AWAITING_RESPONSE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class RejectedConsentOrderAboutToSubmitHandlerTest {

    @InjectMocks
    private RejectedConsentOrderAboutToSubmitHandler handler;
    @Mock
    private RefusalOrderDocumentService refusalOrderDocumentService;
    @Mock
    private RefusedConsentOrderDocumentCategoriser categoriser;
    @Mock
    private ConsentOrderPrintService consentOrderPrintService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.REJECT_ORDER);
    }

    @Test
    void givenFreeTextRefusalReason_whenHandled_thenDoNotPostOrderAndDoNotSetState() {

        OrderRefusalOption anyNonHearingRelatedOption = OrderRefusalOption.APPLICATION_FOR_VARIATION_ORDER_RECONSIDERED;
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .orderRefusalOnScreen(populatedOrderRefusalOrderHolder(
                anyNonHearingRelatedOption,
                "Some other reason"))
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(finremCaseData);
        when(refusalOrderDocumentService.processConsentOrderNotApproved(callbackRequest.getCaseDetails(), AUTH_TOKEN))
            .thenReturn(finremCaseData);

        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertionsForProcessingAndCategorisation(callbackRequest, response);
        assertAll(
            () -> assertThat(response.getData().getState()).isNull(),
            // thenDoNotPostOrder bit:
            () -> verifyNoInteractions(consentOrderPrintService)
        );
    }

    @Test
    void givenHearingRefusalReason_whenHandled_thenDoNotPostOrderAndDoNotSetState() {
        FinremCaseData caseData = FinremCaseData.builder()
            .orderRefusalOnScreen(populatedOrderRefusalOrderHolder(
                OrderRefusalOption.HEARING_FIXED_FOR_FIRST_AVAILABLE_DATE,
                null))
            .build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(caseData);
        when(refusalOrderDocumentService.processConsentOrderNotApproved(callbackRequest.getCaseDetails(), AUTH_TOKEN))
            .thenReturn(caseData);

        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertionsForProcessingAndCategorisation(callbackRequest, response);
        assertAll(
            () -> assertThat(response.getData().getState()).isNull(),
            // thenDoNotPostOrder bit:
            () -> verifyNoInteractions(consentOrderPrintService));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void givenNoHearingOrValidFreeTextReason_whenHandled_thenPostOrderAndChangeState(String emptyReasonToIgnore) {
        FinremCaseData caseData = FinremCaseData.builder()
            .orderRefusalOnScreen(populatedOrderRefusalOrderHolder(
                OrderRefusalOption.TRANSFERRED_TO_APPLICANTS_HOME_COURT_OLD,
                emptyReasonToIgnore))
            .build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(caseData);
        when(refusalOrderDocumentService.processConsentOrderNotApproved(callbackRequest.getCaseDetails(), AUTH_TOKEN))
            .thenReturn(caseData);

        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertionsForProcessingAndCategorisation(callbackRequest, response);
        assertAll(() -> assertThat(response.getErrors()).isEmpty(),
            () -> assertThat(response.getData().getState()).isEqualTo(AWAITING_RESPONSE.getStateId()),
            () -> verify(consentOrderPrintService).sendConsentOrderToBulkPrint(
                callbackRequest.getCaseDetails(), callbackRequest.getCaseDetailsBefore(),
                EventType.SEND_ORDER, AUTH_TOKEN)
        );
    }

    private void assertionsForProcessingAndCategorisation(
        FinremCallbackRequest callbackRequest,
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response) {
        FinremCaseData caseData = callbackRequest.getFinremCaseData();

        assertAll(
            () -> verify(refusalOrderDocumentService)
                .processConsentOrderNotApproved(callbackRequest.getCaseDetails(), AUTH_TOKEN),
            () -> verify(categoriser).categorise(caseData),
            () -> assertThat(response.getData()).isEqualTo(caseData)
        );
    }

    private OrderRefusalHolder populatedOrderRefusalOrderHolder(OrderRefusalOption refusalOption, String
        otherReason) {
        return OrderRefusalHolder.builder()
            .orderRefusal(List.of(refusalOption))
            .orderRefusalOther(otherReason)
            .build();
    }
}
