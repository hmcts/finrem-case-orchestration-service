package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper.DocumentWarningsHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasUploadingDocuments;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class AmendConsentOrderAboutToSubmitHandlerTest {

    @InjectMocks
    private AmendConsentOrderAboutToSubmitHandler handler;

    @Mock
    private ConsentOrderService consentOrderService;

    @Mock
    private CaseFlagsService caseFlagsService;

    @Mock
    private DocumentWarningsHelper documentWarningsHelper;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.AMEND_CONSENT_ORDER);
    }

    @Test
    void givenAnyCase_whenHandle_thenSetCaseFlagInfoAndLatestConsentOrder() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder().build());

        CaseDocument latestConsentOrder = mock(CaseDocument.class);
        when(consentOrderService.getLatestConsentOrderData(any(FinremCallbackRequest.class))).thenReturn(latestConsentOrder);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getData().getLatestConsentOrder()).isEqualTo(latestConsentOrder);

        verify(consentOrderService).getLatestConsentOrderData(callbackRequest);
        verify(caseFlagsService).setCaseFlagInformation(callbackRequest.getCaseDetails());
    }

    @Test
    @SuppressWarnings("unchecked")
    void givenAnyCase_whenHandle_thenPopulateDocumentWarnings() {
        // Arrange
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder().build());
        when(documentWarningsHelper.getDocumentWarnings(eq(callbackRequest), any(Function.class), eq(AUTH_TOKEN)))
            .thenReturn(List.of("warnings"));

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        ArgumentCaptor<Function<FinremCaseData, List<HasUploadingDocuments>>> lambdaCaptor =
            ArgumentCaptor.forClass(Function.class);
        verify(documentWarningsHelper).getDocumentWarnings(eq(callbackRequest), lambdaCaptor.capture(), eq(AUTH_TOKEN));

        assertThat(response.getWarnings()).isEqualTo(List.of("warnings"));
        CaseDocument amendedConsentOrder = mock(CaseDocument.class);
        AmendedConsentOrder expected1 = AmendedConsentOrder.builder().amendedConsentOrder(amendedConsentOrder).build();

        assertThat(
            lambdaCaptor.getValue().apply(FinremCaseData.builder().amendedConsentOrderCollection(
                List.of(
                    AmendedConsentOrderCollection.builder().value(expected1).build()
                )
        ).build())).isEqualTo(List.of(expected1));
        assertThat(lambdaCaptor.getValue().apply(FinremCaseData.builder().amendedConsentOrderCollection(List.of()).build()))
            .isEmpty();
        assertThat(lambdaCaptor.getValue().apply(FinremCaseData.builder().amendedConsentOrderCollection(null).build()))
            .isEmpty();
    }
}
