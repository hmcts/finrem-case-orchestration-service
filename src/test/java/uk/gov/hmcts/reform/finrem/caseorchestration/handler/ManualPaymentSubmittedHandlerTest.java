package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ManualPaymentDocumentService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ManualPaymentSubmittedHandlerTest {

    @InjectMocks
    private ManualPaymentSubmittedHandler handler;
    @Mock
    private ManualPaymentDocumentService manualPaymentDocumentService;
    @Mock
    private BulkPrintService bulkPrintService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.MANUAL_PAYMENT);
    }

    @Test
    void givenContestedPaperCase_whenManualPaymentEventInvoke_thenSendToBulkPrinte() {
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from();
        finremCallbackRequest.getCaseDetails().getData().setPaperApplication(YesOrNo.YES);
        when(manualPaymentDocumentService.generateManualPaymentLetter(finremCallbackRequest.getCaseDetails(),
            AUTH_TOKEN, APPLICANT)).thenReturn(caseDocument());
        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(bulkPrintService).sendDocumentForPrint(any(CaseDocument.class), any(FinremCaseDetails.class), any(), anyString());
    }

    @Test
    void givenContestedCase_whenManualPaymentEventInvoke_thenDoNotSendToBulkPrint() {
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from();
        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(bulkPrintService, never())
            .sendDocumentForPrint(any(CaseDocument.class), any(FinremCaseDetails.class), any(), anyString());
    }
}
