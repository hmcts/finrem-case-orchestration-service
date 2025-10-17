package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class AmendConsentOrderMidHandlerTest {

    private AmendConsentOrderMidHandler handler;

    @Mock
    private BulkPrintDocumentService bulkPrintDocumentService;

    @Mock
    private ConsentedApplicationHelper consentedApplicationHelper;

    @BeforeEach
    void setup() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        handler = new AmendConsentOrderMidHandler(finremCaseDetailsMapper, bulkPrintDocumentService, consentedApplicationHelper);
    }

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.AMEND_CONSENT_ORDER);
    }

    @Test
    void givenNoAmendedConsentOrderUploaded_whenHandle_thenPopulateConsentVariationOrderLabelField() {
        FinremCallbackRequest finremCallbackRequest = buildBaseCallbackRequest();
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        caseData.setAmendedConsentOrderCollection(List.of());

        handler.handle(finremCallbackRequest, AUTH_TOKEN);

        verify(consentedApplicationHelper).setConsentVariationOrderLabelField(caseData);
        verifyNoMoreInteractions(bulkPrintDocumentService);
    }

    @Test
    void givenNewlyUploadedAmendedConsentOrder_whenHandle_thenValidateAndPopulateConsentVariationOrderLabelField() {
        FinremCallbackRequest finremCallbackRequest = buildBaseCallbackRequest();
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        CaseDocument amendedConsentOrder = mock(CaseDocument.class);
        AmendedConsentOrderCollection amendedConsentOrderCollection = AmendedConsentOrderCollection.builder()
            .value(AmendedConsentOrder.builder().amendedConsentOrder(amendedConsentOrder).build()).build();
        caseData.setAmendedConsentOrderCollection(List.of(amendedConsentOrderCollection));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        verify(consentedApplicationHelper).setConsentVariationOrderLabelField(caseData);
        verifyValidateEncryptionOnUploadedDocument(amendedConsentOrder, response.getErrors());
    }

    @Test
    void givenNewlyUploadedAmendedConsentOrders_whenHandle_thenValidateAndPopulateConsentVariationOrderLabelField() {
        FinremCallbackRequest finremCallbackRequest = buildBaseCallbackRequest();
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        CaseDocument amendedConsentOrder = mock(CaseDocument.class);
        CaseDocument amendedConsentOrder2 = mock(CaseDocument.class);

        caseData.setAmendedConsentOrderCollection(new ArrayList<>(List.of(
            AmendedConsentOrderCollection.builder()
                .value(AmendedConsentOrder.builder().amendedConsentOrder(amendedConsentOrder).build()).build(),
            AmendedConsentOrderCollection.builder()
                .value(AmendedConsentOrder.builder().amendedConsentOrder(amendedConsentOrder2).build()).build()
        )));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        verify(consentedApplicationHelper).setConsentVariationOrderLabelField(caseData);
        verifyValidateEncryptionOnUploadedDocument(amendedConsentOrder, response.getErrors());
        verifyValidateEncryptionOnUploadedDocument(amendedConsentOrder2, response.getErrors());
    }

    @Test
    void givenExistingAmendedConsentOrder_whenHandle_thenOnlyValidateNewlyUploadedAndPopulateConsentVariationOrderLabelField() {
        FinremCallbackRequest finremCallbackRequest = buildBaseCallbackRequest();
        FinremCaseData caseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        CaseDocument amendedConsentOrder = mock(CaseDocument.class);
        CaseDocument amendedConsentOrder2 = mock(CaseDocument.class);

        AmendedConsentOrderCollection existing = AmendedConsentOrderCollection.builder()
            .value(AmendedConsentOrder.builder().amendedConsentOrder(amendedConsentOrder).build()).build();
        caseDataBefore.setAmendedConsentOrderCollection(List.of(existing));
        caseData.setAmendedConsentOrderCollection(new ArrayList<>(List.of(
            existing,
            AmendedConsentOrderCollection.builder()
                .value(AmendedConsentOrder.builder().amendedConsentOrder(amendedConsentOrder2).build()).build()
        )));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        verify(consentedApplicationHelper).setConsentVariationOrderLabelField(caseData);
        verifyNotValidateEncryptionOnUploadedDocument(amendedConsentOrder);
        verifyValidateEncryptionOnUploadedDocument(amendedConsentOrder2, response.getErrors());
    }

    private FinremCallbackRequest buildBaseCallbackRequest() {
        return FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), FinremCaseData.builder().build(),
            FinremCaseData.builder().build());
    }

    @SuppressWarnings("unchecked")
    private void verifyValidateEncryptionOnUploadedDocument(CaseDocument amendedConsentOrder, List<String> responseErrors) {
        ArgumentCaptor<List<String>> argumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(bulkPrintDocumentService)
            .validateEncryptionOnUploadedDocument(eq(amendedConsentOrder), eq(CASE_ID), argumentCaptor.capture(), eq(AUTH_TOKEN));
        assertThat(argumentCaptor.getValue()).isEqualTo(responseErrors);
    }

    private void verifyNotValidateEncryptionOnUploadedDocument(CaseDocument amendedConsentOrder) {
        verify(bulkPrintDocumentService, never())
            .validateEncryptionOnUploadedDocument(eq(amendedConsentOrder), eq(CASE_ID), anyList(), eq(AUTH_TOKEN));
    }
}
