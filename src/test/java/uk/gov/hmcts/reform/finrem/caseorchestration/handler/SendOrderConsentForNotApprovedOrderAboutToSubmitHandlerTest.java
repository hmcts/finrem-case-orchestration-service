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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderNotApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SendOrderConsentForNotApprovedOrderAboutToSubmitHandlerTest {

    @InjectMocks
    private SendOrderConsentForNotApprovedOrderAboutToSubmitHandler handler;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.SEND_ORDER);
    }

    @Test
    void givenPaperApplicantAndDigitalRespondent_whenHandle_thenOnlyApplicantCoverLetterStored() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).data(new FinremCaseData()).build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(caseDetails).build();
        CaseDocument coverLetter = CaseDocument.builder().documentFilename("coverLetter.pdf").build();

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(consentOrderNotApprovedDocumentService.getNotApprovedCoverLetter(caseDetails, AUTH_TOKEN, APPLICANT))
            .thenReturn(coverLetter);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getData().getConsentSendOrderWrapper().getConsentOrderNotApprovedCoverLetterApp()).isEqualTo(coverLetter);
        assertThat(response.getData().getConsentSendOrderWrapper().getConsentOrderNotApprovedCoverLetterResp()).isNull();
    }

    @Test
    void givenDigitalApplicantAndPaperRespondent_whenHandle_thenOnlyRespondentCoverLetterStored() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).data(new FinremCaseData()).build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(caseDetails).build();
        CaseDocument coverLetter = CaseDocument.builder().documentFilename("coverLetter.pdf").build();

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(consentOrderNotApprovedDocumentService.getNotApprovedCoverLetter(caseDetails, AUTH_TOKEN, RESPONDENT))
            .thenReturn(coverLetter);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getData().getConsentSendOrderWrapper().getConsentOrderNotApprovedCoverLetterApp()).isNull();
        assertThat(response.getData().getConsentSendOrderWrapper().getConsentOrderNotApprovedCoverLetterResp()).isEqualTo(coverLetter);
    }
}
