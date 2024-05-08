package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterAddressToType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralLetterWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralLetterService;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateGeneralLetterMidHandlerTest {

    @Mock
    private GeneralLetterService generalLetterService;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    private CreateGeneralLetterMidHandler handler;

    @Before
    public void setup() {
        handler = new CreateGeneralLetterMidHandler(finremCaseDetailsMapper, generalLetterService);
    }

    @Test
    public void whenCallbackTypeAndCaseTypeAndEventTypeMatch_thenCanHandle() {
        assertTrue(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.CREATE_GENERAL_LETTER));
    }

    @Test
    public void whenCallbackTypeOrCaseTypeOrEventTypeIncorrect_thenCannotHandle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CREATE_GENERAL_LETTER));
        assertFalse(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.CREATE_GENERAL_LETTER));
        assertFalse(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.CREATE_GENERAL_EMAIL));
    }

    @Test
    public void testHandleWithNoErrors() {
        FinremCallbackRequest request = buildFinremCallbackRequest();
        FinremCaseDetails caseDetails = request.getCaseDetails();
        when(generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails)).thenReturn(Collections.emptyList());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(request, "authToken");

        verify(generalLetterService).previewGeneralLetter("authToken", caseDetails);
        assertNotNull(response);
    }

    @Test
    public void testHandleWithErrors() {
        FinremCallbackRequest request = buildFinremCallbackRequest();
        FinremCaseDetails caseDetails = request.getCaseDetails();
        when(generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails)).thenReturn(List.of("Error"));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(request, "authToken");

        verify(generalLetterService, never()).previewGeneralLetter("authToken", caseDetails);
        assertNotNull(response);
        assertTrue(response.getErrors().contains("Error"));
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