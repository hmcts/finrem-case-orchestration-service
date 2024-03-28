package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralLetterService;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@RunWith(MockitoJUnitRunner.class)
public class CreateGeneralLetterMidHandlerTest {

    private CreateGeneralLetterMidHandler handler;
    FinremCallbackRequest callbackRequest;
    FinremCaseDetails caseDetails;
    List<DocumentCollection> uploadedDocuments;

    @Mock
    private GeneralLetterService generalLetterService;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Before
    public void setup() {
        handler =  new CreateGeneralLetterMidHandler(finremCaseDetailsMapper, generalLetterService);
        callbackRequest = buildFinremCallbackRequest();
        caseDetails = callbackRequest.getCaseDetails();
        uploadedDocuments = List.of(DocumentCollection.builder().value(caseDocument()).build());
        caseDetails.getData().getGeneralLetterWrapper().setGeneralLetterUploadedDocuments(uploadedDocuments);
    }

    @Test
    public void givenACcdCallbackCreateGeneralLetterAboutToSubmitHandler_WhenCanHandleCalled_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.CREATE_GENERAL_LETTER),
            is(true));
    }

    @Test
    public void givenACcdCallbackCreateGeneralLetterAboutToSubmitHandlerJudge_WhenCanHandleCalled_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.CREATE_GENERAL_LETTER_JUDGE),
            is(true));
    }

    @Test
    public void givenACcdCallbackAboutToSubmit_WhenCanHandleCalled_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.CREATE_GENERAL_LETTER),
            is(false));
    }

    @Test
    public void givenACcdCallbackCallbackCreateGeneralLetterAboutToSubmitHandler_WhenHandle_thenCreatePreviewLetter() {
        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(generalLetterService).getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails);
        verify(generalLetterService).previewGeneralLetter(AUTH_TOKEN, caseDetails);
        verify(generalLetterService).validateEncryptionOnUploadedDocuments(uploadedDocuments, AUTH_TOKEN, String.valueOf(caseDetails.getId()));
    }

    @Test
    public void givenACcdCallbackCallbackCreateGeneralLetterAboutToSubmitHandler_WhenHandle_thenCreateError() {
        caseDetails.getData().getContactDetailsWrapper().setSolicitorAddress(null);
        when(generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails))
            .thenReturn(asList("Address is missing for recipient type"));
        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(generalLetterService, times(2)).getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails);
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
                .generalLetterPreview(CaseDocument.builder().build())
                .build())
            .build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).caseType(CaseType.CONSENTED).data(caseData).build();
        return FinremCallbackRequest.builder().eventType(EventType.CREATE_GENERAL_LETTER).caseDetails(caseDetails).build();
    }

}
