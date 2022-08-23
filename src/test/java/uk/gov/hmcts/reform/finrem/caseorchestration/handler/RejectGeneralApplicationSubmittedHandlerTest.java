package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class RejectGeneralApplicationSubmittedHandlerTest {

    @InjectMocks
    private RejectGeneralApplicationSubmittedHandler submittedHandler;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PaperNotificationService paperNotificationService;

    private CallbackRequest callbackRequest;
    private CaseDetails caseDetails;



    @Before
    public void setup(){
        callbackRequest = CallbackRequest.builder().build();
        caseDetails = CaseDetails.builder().build();
        callbackRequest.setCaseDetails(caseDetails);
    }

    @Test
    public void givenValidCallBack_whenCanHandle_thenReturnTrue() {
        assertTrue(submittedHandler.canHandle(CallbackType.SUBMITTED,
            CaseType.CONTESTED,
            EventType.REJECT_GENERAL_APPLICATION));
    }

    @Test
    public void givenInvalidCallBack_whenCanHandle_thenReturnFalse() {
        assertFalse(submittedHandler.canHandle(CallbackType.ABOUT_TO_SUBMIT,
            CaseType.CONTESTED,
            EventType.REJECT_GENERAL_APPLICATION));
    }

    @Test
    public void givenApplicantSolicitorDigital_whenHandle_thenSendEmailToAppSolicitor() {
        when(notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails))
            .thenReturn(true);
        submittedHandler.handle(callbackRequest, AUTH_TOKEN);
        verify(notificationService).sendGeneralApplicationRejectionEmailToAppSolicitor(caseDetails);
    }

    @Test
    public void givenRespondentSolicitorDigital_whenHandle_thenSendEmailToResSolicitor() {
        when(notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails))
            .thenReturn(true);
        submittedHandler.handle(callbackRequest, AUTH_TOKEN);
        verify(notificationService).sendGeneralApplicationRejectionEmailToResSolicitor(caseDetails);
    }

    @Test
    public void givenApplicantSolicitorNotDigital_whenHandle_thenSendLetterToAppSolicitor() {
        when(notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails))
            .thenReturn(false);
        submittedHandler.handle(callbackRequest, AUTH_TOKEN);
        verify(paperNotificationService).printApplicantRejectionGeneralApplication(caseDetails, AUTH_TOKEN);
    }

    @Test
    public void givenRespondentSolicitorNotDigital_whenHandle_thenSendLetterToResSolicitor() {
        when(notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails))
            .thenReturn(false);
        submittedHandler.handle(callbackRequest, AUTH_TOKEN);
        verify(paperNotificationService).printRespondentRejectionGeneralApplication(caseDetails, AUTH_TOKEN);
    }
}
