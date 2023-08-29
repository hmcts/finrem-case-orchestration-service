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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataConsented;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class UploadApprovedOrderConsentedAboutToSubmitHandlerTest {

    @Mock
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
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
        CaseDocument uploadApproveOrder = CaseDocument.builder().documentFilename("testUploadAppOrder").build();
        FinremCaseDataConsented finremCaseData =
            FinremCaseDataConsented.builder().uploadApprovedConsentOrder(uploadApproveOrder).build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(1L).data(finremCaseData).build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(finremCaseDetails).build();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataConsented> response =
            uploadApprovedOrderConsentedAboutToSubmitHandler.handle(callbackRequest, "auth");

        assertThat(response.getData().getLatestConsentOrder(), is(uploadApproveOrder));
        verify(consentOrderApprovedDocumentService, times(1))
            .addGeneratedApprovedConsentOrderDocumentsToCase(any(), any());

    }
}