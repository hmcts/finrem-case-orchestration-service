package uk.gov.hmcts.reform.finrem.caseorchestration.handler;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PaymentDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PaymentDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PaymentDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@RunWith(MockitoJUnitRunner.class)
public class ManualPaymentAboutToSubmitHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    @InjectMocks
    private ManualPaymentAboutToSubmitHandler handler;

    @Mock
    private GenericDocumentService service;

    @Test
    public void givenContestedCase_whenPaymentDocUploadedAsDoc_thenConvertToPdf() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.MANUAL_PAYMENT),
            is(true));
    }

    @Test
    public void givenConsentedCase_whenPaymentDocUploadedAsDoc_thenConvertToPdf() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.MANUAL_PAYMENT),
            is(false));
    }

    @Test
    public void givenContestedCase_whenUseIssueApplicationAndIssueDateEnteredManually_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        when(service.convertDocumentIfNotPdfAlready(isA(CaseDocument.class), eq(AUTH_TOKEN))).thenReturn(caseDocument());
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        List<PaymentDocumentCollection> copyOfPaperFormA = response.getData().getCopyOfPaperFormA();
        assertEquals(FILE_NAME, copyOfPaperFormA.get(0).getValue().getUploadedDocument().getDocumentFilename());
    }

    private FinremCallbackRequest buildCallbackRequest() {
        List<PaymentDocumentCollection> paymentList = new ArrayList<>();
        PaymentDocumentCollection payments = PaymentDocumentCollection.builder()
            .value(PaymentDocument.builder().typeOfDocument(PaymentDocumentType.COPY_OF_PAPER_FORM_A)
                .uploadedDocument(TestSetUpUtils.caseDocument(DOC_URL, DOC_FILE_NAME, BINARY_URL)).build())
            .build();
        paymentList.add(payments);
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.ISSUE_APPLICATION)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(FinremCaseData.builder().copyOfPaperFormA(paymentList).build()).build())
            .build();
    }
}