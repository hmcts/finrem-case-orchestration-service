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
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo.YES;

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
    void testHandle() {
        // Mock data for caseDetails
        FinremCaseDetails caseDetails = new FinremCaseDetails();
        FinremCaseData caseData = new FinremCaseData();
        caseData.setHelpWithFeesQuestion(YES);
        caseData.setPbaNumber("ValidPbaNumber");
        caseDetails.setData(caseData);

        // Mock behavior
        Mockito.lenient().when(pbaValidationService.isValidPBA(any(), eq("ValidPbaNumber"))).thenReturn(true);

        // Execute the method under test
        FinremCallbackRequest request = FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        var response = handler.handle(request, "userAuthorisation");

        // Verify the response
        assertThat(response).isNotNull();
        assertEquals(0, response.getErrors().size());
        assertEquals(caseData, response.getData());
    }

}

