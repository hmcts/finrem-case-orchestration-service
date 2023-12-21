package uk.gov.hmcts.reform.finrem.caseorchestration.handler;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConsentApplicationApprovedInContestedAboutToSubmitHandlerTest {

    @InjectMocks
    private ConsentApplicationApprovedInContestedAboutToSubmitHandler handler;
    @Mock
    private ConsentOrderApprovedDocumentService service;
    static final String AUTH_TOKEN = "tokien:)";

    @Test
    void canHandle() {
        assertTrue(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT,
            CaseType.CONTESTED, EventType.CONSENT_APPLICATION_APPROVED_IN_CONTESTED));
    }

    @Test
    void canNotHandle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START,
            CaseType.CONSENTED, EventType.CONSENT_APPLICATION_APPROVED_IN_CONTESTED));
    }

    @Test
    void canNotHandleWrongEventType() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START,
            CaseType.CONTESTED, EventType.CLOSE));
    }

    @Test
    void canNotHandleWrongCallbackType() {
        assertFalse(handler.canHandle(CallbackType.MID_EVENT,
            CaseType.CONTESTED, EventType.CONSENT_APPLICATION_APPROVED_IN_CONTESTED));
    }

    @Test
    void givenContestedCase_whenEventExecuted_thenSetTheLoggedInUserIfNull() {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(service).stampAndPopulateContestedConsentApprovedOrderCollection(caseDetails.getData(),
            AUTH_TOKEN, String.valueOf(caseDetails.getId()));
        verify(service).generateAndPopulateConsentOrderLetter(caseDetails, AUTH_TOKEN);
    }

    private FinremCallbackRequest buildFinremCallbackRequest() {
        FinremCaseData caseData = FinremCaseData.builder().build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).caseType(CaseType.CONTESTED).data(caseData).build();
        return FinremCallbackRequest.builder()
            .eventType(EventType.CONSENT_APPLICATION_APPROVED_IN_CONTESTED).caseDetails(caseDetails).build();
    }
}