package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper.DocumentWarningsHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasUploadingDocuments;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.UPLOAD_APPROVED_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UploadApprovedOrderConsentedAboutToSubmitHandlerTest {

    @Mock
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private DocumentWarningsHelper documentWarningsHelper;
    @InjectMocks
    private UploadApprovedOrderConsentedAboutToSubmitHandler underTest;

    @Test
    void canHandle() {
        assertCanHandle(underTest, ABOUT_TO_SUBMIT, CONSENTED, UPLOAD_APPROVED_ORDER);
    }

    @Test
    void givenUploadConsentedApproveOrder_whenHandle_thenSetLatestConsentOrderAndCallAddGeneratedDocs() {
        CaseDocument uploadApproveOrder = caseDocument(DOC_URL, DOC_FILE_NAME, BINARY_URL);

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .consentOrderWrapper(
                ConsentOrderWrapper.builder().uploadApprovedConsentOrder(uploadApproveOrder).build()
            )
            .build();

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(Long.valueOf(CASE_ID)).data(finremCaseData).build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(finremCaseDetails).build();

        CaseDocument convertedCaseDocument = caseDocument("convertedUrl", "converted.pdf");
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any(), any())).thenReturn(convertedCaseDocument);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            underTest.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getData().getLatestConsentOrder()).isEqualTo(convertedCaseDocument);
        verify(consentOrderApprovedDocumentService).addGeneratedApprovedConsentOrderDocumentsToCase(any(), any());
        verify(genericDocumentService).convertDocumentIfNotPdfAlready(any(), any(), any());
    }

    @Test
    void givenUploadConsentedApproveOrder_whenDocumentWarningExists_thenReturnWarnings() {
        CaseDocument uploadApproveOrder = caseDocument(DOC_URL, DOC_FILE_NAME, BINARY_URL);
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .consentOrderWrapper(
                ConsentOrderWrapper.builder().uploadApprovedConsentOrder(uploadApproveOrder).build()
            )
            .build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), finremCaseData);

        List<String> expectedWarnings = List.of("warning1", "warning2");
        when(documentWarningsHelper.getDocumentWarnings(eq(callbackRequest), any(), eq(AUTH_TOKEN)))
            .thenReturn(expectedWarnings);
        CaseDocument convertedCaseDocument = caseDocument("convertedUrl", "converted.pdf");
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any(), any())).thenReturn(convertedCaseDocument);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(callbackRequest, AUTH_TOKEN);

        // Verify
        assertThat(response.getWarnings()).containsExactly("warning1", "warning2");
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Function<FinremCaseData, List<HasUploadingDocuments>>> lambdaCaptor =
            ArgumentCaptor.forClass(Function.class);

        verify(documentWarningsHelper).getDocumentWarnings(eq(callbackRequest), lambdaCaptor.capture(), eq(AUTH_TOKEN));
        List<? extends HasUploadingDocuments> docs = lambdaCaptor.getValue().apply(finremCaseData);
        assertThat(docs)
            .extracting(HasUploadingDocuments::getUploadingDocuments)
            .containsExactly(List.of(convertedCaseDocument));
    }
}
