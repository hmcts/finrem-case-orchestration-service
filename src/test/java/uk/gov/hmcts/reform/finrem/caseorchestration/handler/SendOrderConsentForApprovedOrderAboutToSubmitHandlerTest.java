package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.CONSENT_SEND_ORDER_FOR_APPROVED_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SendOrderConsentForApprovedOrderAboutToSubmitHandlerTest {

    private static final String AUTH_TOKEN = "TOKEN-:";

    @InjectMocks
    private SendOrderConsentForApprovedOrderAboutToSubmitHandler handler;

    @Mock
    private ConsentOrderPrintService service;
    @Mock
    private NotificationService notificationService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.CONSENT_SEND_ORDER_FOR_APPROVED_ORDER);
    }

    @Test
    void givenBothPartiesGetLetters_whenHandle_thenPacksStoredInOrderAndNothingPrinted() {
        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(service.getApplicantLetterPack(caseDetails, caseDetailsBefore, CONSENT_SEND_ORDER_FOR_APPROVED_ORDER, AUTH_TOKEN))
            .thenReturn(List.of(document("appCoverLetter.pdf"), document("order.pdf")));
        when(service.getRespondentLetterPack(caseDetails, caseDetailsBefore, CONSENT_SEND_ORDER_FOR_APPROVED_ORDER, AUTH_TOKEN))
            .thenReturn(List.of(document("order.pdf"), document("resCoverLetter.pdf")));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertEquals(List.of("appCoverLetter.pdf", "order.pdf"), filenames(response.getData().getConsentOrderApplicantLetterPack()));
        assertEquals(List.of("order.pdf", "resCoverLetter.pdf"), filenames(response.getData().getConsentOrderRespondentLetterPack()));
        verify(service, never()).sendConsentOrderToBulkPrint(any(), any(), any(), any());
    }

    @Test
    void givenBothSolicitorsDigital_whenHandle_thenNoPacksBuiltOrStored() {
        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertNull(response.getData().getConsentOrderApplicantLetterPack());
        assertNull(response.getData().getConsentOrderRespondentLetterPack());
        verify(service, never()).getApplicantLetterPack(any(), any(), any(), any());
        verify(service, never()).getRespondentLetterPack(any(), any(), any(), any());
        verify(service, never()).sendConsentOrderToBulkPrint(any(), any(), any(), any());
    }

    @Test
    void givenPacksFromPreviousRun_whenSolicitorsNowDigital_thenPacksCleared() {
        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        caseDetails.getData().setConsentOrderApplicantLetterPack(List.of(DocumentCollectionItem.fromCaseDocument(document("old.pdf"))));
        caseDetails.getData().setConsentOrderRespondentLetterPack(List.of(DocumentCollectionItem.fromCaseDocument(document("old.pdf"))));
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertNull(response.getData().getConsentOrderApplicantLetterPack());
        assertNull(response.getData().getConsentOrderRespondentLetterPack());
    }

    private FinremCallbackRequest callbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(CONSENT_SEND_ORDER_FOR_APPROVED_ORDER)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CaseType.CONSENTED)
                .data(new FinremCaseData()).build())
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CaseType.CONSENTED)
                .data(new FinremCaseData()).build())
            .build();
    }

    private CaseDocument document(String filename) {
        return CaseDocument.builder().documentFilename(filename).build();
    }

    private List<String> filenames(List<DocumentCollectionItem> pack) {
        return pack.stream().map(item -> item.getValue().getDocumentFilename()).toList();
    }
}
