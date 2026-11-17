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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentSendOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.CONSENT_SEND_ORDER_FOR_APPROVED_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SendOrderConsentForApprovedOrderAboutToSubmitHandlerTest {

    @InjectMocks
    private SendOrderConsentForApprovedOrderAboutToSubmitHandler handler;

    @Mock
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    @Mock
    private NotificationService notificationService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.CONSENT_SEND_ORDER_FOR_APPROVED_ORDER);
    }

    @Test
    void givenBothPartiesGetLetters_whenHandle_thenCoverLettersStored() {
        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseDocument applicantCoverLetter = document("appCoverLetter.pdf");
        CaseDocument respondentCoverLetter = document("respCoverLetter.pdf");
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(consentOrderApprovedDocumentService.getApprovedConsentOrderCoverLetter(caseDetails, AUTH_TOKEN, APPLICANT))
            .thenReturn(applicantCoverLetter);
        when(consentOrderApprovedDocumentService.getApprovedConsentOrderCoverLetter(caseDetails, AUTH_TOKEN, RESPONDENT))
            .thenReturn(respondentCoverLetter);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertEquals(applicantCoverLetter, response.getData().getConsentSendOrderWrapper().getConsentOrderApprovedCoverLetterApp());
        assertEquals(respondentCoverLetter, response.getData().getConsentSendOrderWrapper().getConsentOrderApprovedCoverLetterResp());
    }

    @Test
    void givenNotPaperApplication_whenHandle_thenNoCoverLettersStored() {
        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        // The service returns null for non-paper applications (the mock's default)

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertNull(response.getData().getConsentSendOrderWrapper().getConsentOrderApprovedCoverLetterApp());
        assertNull(response.getData().getConsentSendOrderWrapper().getConsentOrderApprovedCoverLetterResp());
        verify(consentOrderApprovedDocumentService).getApprovedConsentOrderCoverLetter(caseDetails, AUTH_TOKEN, APPLICANT);
        verify(consentOrderApprovedDocumentService).getApprovedConsentOrderCoverLetter(caseDetails, AUTH_TOKEN, RESPONDENT);
    }

    @Test
    void givenBothSolicitorsDigital_whenHandle_thenNoCoverLettersGenerated() {
        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertNull(response.getData().getConsentSendOrderWrapper().getConsentOrderApprovedCoverLetterApp());
        assertNull(response.getData().getConsentSendOrderWrapper().getConsentOrderApprovedCoverLetterResp());
        verify(consentOrderApprovedDocumentService, never()).getApprovedConsentOrderCoverLetter(any(), any(), any());
    }

    @Test
    void givenApplicantDigitalAndRespondentByPost_whenHandle_thenOnlyRespondentCoverLetterStored() {
        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseDocument respondentCoverLetter = document("respCoverLetter.pdf");
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(consentOrderApprovedDocumentService.getApprovedConsentOrderCoverLetter(caseDetails, AUTH_TOKEN, RESPONDENT))
            .thenReturn(respondentCoverLetter);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertNull(response.getData().getConsentSendOrderWrapper().getConsentOrderApprovedCoverLetterApp());
        assertEquals(respondentCoverLetter, response.getData().getConsentSendOrderWrapper().getConsentOrderApprovedCoverLetterResp());
        verify(consentOrderApprovedDocumentService, never()).getApprovedConsentOrderCoverLetter(caseDetails, AUTH_TOKEN, APPLICANT);
    }

    @Test
    void givenCoverLettersFromPreviousRun_whenSolicitorsNowDigital_thenCoverLettersCleared() {
        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        caseDetails.getData().setConsentSendOrderWrapper(ConsentSendOrderWrapper.builder()
            .consentOrderApprovedCoverLetterApp(document("oldApp.pdf"))
            .consentOrderApprovedCoverLetterResp(document("oldResp.pdf"))
            .build());
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertNull(response.getData().getConsentSendOrderWrapper().getConsentOrderApprovedCoverLetterApp());
        assertNull(response.getData().getConsentSendOrderWrapper().getConsentOrderApprovedCoverLetterResp());
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
}
