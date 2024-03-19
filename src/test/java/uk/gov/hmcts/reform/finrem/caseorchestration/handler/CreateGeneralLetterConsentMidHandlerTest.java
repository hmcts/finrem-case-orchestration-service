package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterAddressToType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralLetterWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralLetterService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class CreateGeneralLetterConsentMidHandlerTest {

    private CreateGeneralLetterConsentMidHandler handler;

    @Mock
    private GeneralLetterService generalLetterService;
    @Mock
    private BulkPrintDocumentService service;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Before
    public void setup() {
        handler =  new CreateGeneralLetterConsentMidHandler(finremCaseDetailsMapper,
            generalLetterService, service);
    }

    @Test
    public void givenACcdCallbackCreateGeneralLetterAboutToSubmitHandler_WhenCanHandleCalled_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.CREATE_GENERAL_LETTER),
            is(true));
    }

    @Test
    public void givenACcdCallbackCreateGeneralLetterAboutToSubmitHandlerJudge_WhenCanHandleCalled_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void givenACcdCallbackAboutToSubmit_WhenCanHandleCalled_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.CREATE_GENERAL_LETTER),
            is(false));
    }

    @Test
    public void givenACcdCallbackCallbackCreateGeneralLetterAboutToSubmitHandler_WhenHandle_thenCreatePreviewLetter() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();
        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(generalLetterService).getCaseDataErrorsForCreatingPreviewOrFinalLetter(any(FinremCaseDetails.class));
        verify(generalLetterService).previewGeneralLetter(anyString(), any(FinremCaseDetails.class));
        verify(generalLetterService).validateEncryptionOnUploadedDocuments(any(), any(), any());
    }

    @Test
    public void givenACcdCallbackCallbackCreateGeneralLetterAboutToSubmitHandler_WhenHandle_thenCreateError() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();
        callbackRequest.getCaseDetails().getData().getContactDetailsWrapper().setSolicitorAddress(null);
        when(generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(callbackRequest.getCaseDetails()))
            .thenReturn(List.of("Address is missing for recipient type"));
        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(generalLetterService, times(1)).getCaseDataErrorsForCreatingPreviewOrFinalLetter(any(FinremCaseDetails.class));
        verify(generalLetterService, never()).validateEncryptionOnUploadedDocuments(any(), any(), any());
    }

    private FinremCallbackRequest buildFinremCallbackRequest() {
        FinremCaseData caseData = FinremCaseData.builder()
            .generalLetterWrapper(GeneralLetterWrapper.builder()
                .generalLetterAddressTo(GeneralLetterAddressToType.APPLICANT_SOLICITOR)
                .generalLetterRecipient("Test")
                .generalLetterRecipientAddress(Address.builder()
                    .addressLine1("line1")
                    .addressLine2("line2")
                    .country("country")
                    .postCode("AB1 1BC").build())
                .generalLetterCreatedBy("Test")
                .generalLetterBody("body")
                .generalLetterUploadedDocuments(List.of(DocumentCollection.builder().value(TestSetUpUtils.caseDocument()).build()))
                .generalLetterPreview(CaseDocument.builder().build())
                .build())
            .build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).caseType(CaseType.CONSENTED).data(caseData).build();
        return FinremCallbackRequest.builder().eventType(EventType.CREATE_GENERAL_LETTER).caseDetails(caseDetails).build();
    }

}
