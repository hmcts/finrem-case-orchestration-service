package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class DirectionUploadOrderAboutToSubmitHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    @InjectMocks
    private DirectionUploadOrderAboutToSubmitHandler handler;

    @Mock
    private AdditionalHearingDocumentService service;

    @Test
    void handlerCanNotHandleWhenNonExpectedEventTypeReceived() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.LIST_FOR_HEARING));
    }

    @Test
    void handlerCanNotHandleWhenNonExpectedCaseTypeReceived() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.DIRECTION_UPLOAD_ORDER));
    }

    @Test
    void handlerCanNotHandleWhenNonExpectedCallbackTypeReceived() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.DIRECTION_UPLOAD_ORDER));
    }

    @Test
    void handlerCanNotHandleWhenNonExpectedInputReceived() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.CLOSE));
    }

    @Test
    void handlerCanHandleWhenExpectedInputReceived() {
        assertTrue(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.DIRECTION_UPLOAD_ORDER));
    }

    @Test
    void createAndStoreAdditionalHearingDocumentsHandleException() throws JsonProcessingException {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails finremCaseDetails = finremCallbackRequest.getCaseDetails();
        doThrow(new CourtDetailsParseException()).when(service)
            .createAndStoreAdditionalHearingDocuments(finremCaseDetails, AUTH_TOKEN);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> res = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertEquals(1, res.getErrors().size());
        assertEquals("Failed to parse court details.", res.getErrors().get(0));
        verify(service).createAndStoreAdditionalHearingDocuments(finremCallbackRequest.getCaseDetails(), AUTH_TOKEN);
    }


    @Test
    void createAndStoreAdditionalHearingDocuments() throws JsonProcessingException {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(service).createAndStoreAdditionalHearingDocuments(finremCallbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.DIRECTION_UPLOAD_ORDER)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }
}