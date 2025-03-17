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
        FinremCaseData caseData = new FinremCaseData();
        caseData.setHelpWithFeesQuestion(NO);
        caseData.setPbaNumber("ValidPbaNumber");
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(1L)
            .data(caseData)
            .caseType(CaseType.CONTESTED)
            .build();

        FinremCallbackRequest request = FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventType(EventType.APPLICATION_PAYMENT_SUBMISSION)
            .build();

        Mockito.when(pbaValidationService.isValidPBA("userAuthorisation", "ValidPbaNumber")).thenReturn(true);

        var response = handler.handle(request, "userAuthorisation");

        assertThat(response.getErrors()).isEmpty();
        assertEquals(caseData, response.getData());
    }

    @Test
    void testHandle_InvalidPbaNumber_thenReturnError() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setHelpWithFeesQuestion(NO);
        caseData.setPbaNumber("InvalidPbaNumber");
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(1L)
            .data(caseData)
            .caseType(CaseType.CONTESTED)
            .build();

        FinremCallbackRequest request = FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventType(EventType.APPLICATION_PAYMENT_SUBMISSION)
            .build();

        Mockito.when(pbaValidationService.isValidPBA("userAuthorisation", "InvalidPbaNumber")).thenReturn(false);

        var response = handler.handle(request, "userAuthorisation");

        assertThat(response.getErrors()).containsExactly("PBA Account Number is not valid, please enter a valid one.");
    }
}
