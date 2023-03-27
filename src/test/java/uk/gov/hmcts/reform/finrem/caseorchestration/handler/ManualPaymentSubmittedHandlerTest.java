package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ManualPaymentDocumentService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class ManualPaymentSubmittedHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    @InjectMocks
    private ManualPaymentSubmittedHandler handler;
    @Mock
    private ManualPaymentDocumentService service;
    @Mock
    private BulkPrintService printService;

    @Test
    void givenContestedPaperCase_whenCaseTypeIsConsented_thenHandlerWillNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.MANUAL_PAYMENT),
            is(false));
    }

    @Test
    void givenContestedPaperCase_whenCallbackIsAboutToSubmit_thenHandlerWillNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.MANUAL_PAYMENT),
            is(false));
    }

    @Test
    void givenContestedPaperCase_whenWrongEvent_thenHandlerWillNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    void givenContestedPaperCase_whenAllCorrect_thenHandlerWillHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.MANUAL_PAYMENT),
            is(true));
    }

    @Test
    void givenContestedPaperCase_whenManualPaymentEventInvoke_thenSendToBulkPrinte() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        finremCallbackRequest.getCaseDetails().getData().setPaperApplication(YesOrNo.YES);
        when(service.generateManualPaymentLetter(finremCallbackRequest.getCaseDetails(),
            AUTH_TOKEN, APPLICANT)).thenReturn(caseDocument());
        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(printService).sendDocumentForPrint(any(CaseDocument.class), any(FinremCaseDetails.class));
    }

    @Test
    void givenContestedCase_whenManualPaymentEventInvoke_thenDoNotSendToBulkPrint() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(printService, never()).sendDocumentForPrint(any(CaseDocument.class), any(FinremCaseDetails.class));
    }


    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.ISSUE_APPLICATION)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(FinremCaseData.builder().ccdCaseType(CONTESTED).build()).build())
            .build();
    }
}