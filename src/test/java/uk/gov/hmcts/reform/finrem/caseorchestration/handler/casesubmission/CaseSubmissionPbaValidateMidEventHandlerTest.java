package uk.gov.hmcts.reform.finrem.caseorchestration.handler.casesubmission;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PBAValidationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo.NO;

@ExtendWith(MockitoExtension.class)
class CaseSubmissionPbaValidateMidEventHandlerTest {

    @Mock
    private PBAValidationService pbaValidationService;

    @InjectMocks
    private CaseSubmissionPbaValidateMidEventHandler handler;

    @Test
    void testCanHandle() {
        assertTrue(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.APPLICATION_PAYMENT_SUBMISSION));
        assertTrue(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.APPLICATION_PAYMENT_SUBMISSION));
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.APPLICATION_PAYMENT_SUBMISSION));
    }

    @Test
    void testHandle_ValidPbaNumber_thenHandlerCanHandle() {
        FinremCaseDetails caseDetails = new FinremCaseDetails();
        FinremCaseData caseData = new FinremCaseData();
        caseData.getPaymentDetailsWrapper().setHelpWithFeesQuestion(NO);
        caseData.getPaymentDetailsWrapper().setPbaNumber("ValidPbaNumber");
        caseDetails.setData(caseData);

        Mockito.when(pbaValidationService.isValidPBA("userAuthorisation", "ValidPbaNumber")).thenReturn(true);

        FinremCallbackRequest request = FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        var response = handler.handle(request, "userAuthorisation");

        assertThat(response.getErrors()).isEmpty();
        assertEquals(caseData, response.getData());
    }

    @Test
    void testHandle_InvalidPbaNumber_thenReturnError() {
        FinremCaseDetails caseDetails = new FinremCaseDetails();
        FinremCaseData caseData = new FinremCaseData();
        caseData.getPaymentDetailsWrapper().setHelpWithFeesQuestion(NO);
        caseData.getPaymentDetailsWrapper().setPbaNumber("InvalidPbaNumber");
        caseDetails.setData(caseData);

        Mockito.when(pbaValidationService.isValidPBA("userAuthorisation", "InvalidPbaNumber")).thenReturn(false);

        FinremCallbackRequest request = FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        var response = handler.handle(request, "userAuthorisation");

        assertThat(response).isNotNull();
        assertThat(response.getErrors().size(), is(1));
        assertThat(response.getErrors().get(0)).isEqualTo("PBA Account Number is not valid, please enter a valid one.");
    }

}

