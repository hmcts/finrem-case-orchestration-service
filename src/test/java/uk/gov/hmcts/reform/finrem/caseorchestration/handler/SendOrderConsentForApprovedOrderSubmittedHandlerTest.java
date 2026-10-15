package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.FinremConsentOrderAvailableCorresponder;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SendOrderConsentForApprovedOrderSubmittedHandlerTest {

    @InjectMocks
    private SendOrderConsentForApprovedOrderSubmittedHandler underTest;
    @Mock
    private ConsentOrderPrintService consentOrderPrintService;
    @Mock
    private FinremConsentOrderAvailableCorresponder consentOrderAvailableCorresponder;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, CallbackType.SUBMITTED, CaseType.CONSENTED,
            EventType.CONSENT_SEND_ORDER_FOR_APPROVED_ORDER);
    }

    @Test
    void whenHandled_thenSendsLettersAndEmails() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            underTest.handle(callbackRequest, AUTH_TOKEN);

        verify(consentOrderPrintService).sendConsentOrderToBulkPrintWithStoredCoverSheets(
            callbackRequest.getCaseDetails(), callbackRequest.getCaseDetailsBefore(),
            EventType.CONSENT_SEND_ORDER_FOR_APPROVED_ORDER, AUTH_TOKEN);
        verify(consentOrderAvailableCorresponder).sendCorrespondence(callbackRequest.getCaseDetails());

        assertAll(
            () -> assertNull(response.getData()),
            () -> assertNull(response.getConfirmationHeader()),
            () -> assertNull(response.getConfirmationBody())
        );
    }
}