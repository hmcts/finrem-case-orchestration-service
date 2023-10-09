package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;


@RunWith(MockitoJUnitRunner.class)
public class UploadApprovedOrderConsentedAboutToSubmitHandlerTest {

    @Mock
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    @Mock
    private GenericDocumentService service;
    @InjectMocks
    private UploadApprovedOrderConsentedAboutToSubmitHandler uploadApprovedOrderConsentedAboutToSubmitHandler;


    @Test
    public void givenConsentedCase_whenAboutToSubmitUploadApprovedOrder_thenCanHandle() {
        assertThat(uploadApprovedOrderConsentedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.UPLOAD_APPROVED_ORDER),
            is(true));
    }

    @Test
    public void givenConsentedCase_whenSubmittedUploadApprovedOrder_thenCannotHandle() {
        assertThat(uploadApprovedOrderConsentedAboutToSubmitHandler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.UPLOAD_APPROVED_ORDER),
            is(false));
    }

    @Test
    public void givenUploadConsentedApproveOrder_whenHandle_thenSetLatestConsentOrderAndCallAddGeneratedDocs() {
        CaseDocument uploadApproveOrder = caseDocument(DOC_URL, DOC_FILE_NAME, BINARY_URL);
        FinremCaseData finremCaseData = FinremCaseData.builder().consentOrderWrapper(
                ConsentOrderWrapper.builder().uploadApprovedConsentOrder(uploadApproveOrder).build())
            .build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(1L).data(finremCaseData).build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(finremCaseDetails).build();
        when(service.convertDocumentIfNotPdfAlready(any(), any(), any())).thenReturn(caseDocument());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            uploadApprovedOrderConsentedAboutToSubmitHandler.handle(callbackRequest, "auth");

        assertThat(response.getData().getLatestConsentOrder(), is(caseDocument()));
        verify(consentOrderApprovedDocumentService).addGeneratedApprovedConsentOrderDocumentsToCase(any(), any());
        verify(service).convertDocumentIfNotPdfAlready(any(), any(), any());
    }
}