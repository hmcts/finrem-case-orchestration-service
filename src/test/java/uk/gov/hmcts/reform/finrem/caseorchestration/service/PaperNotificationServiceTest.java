package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;

public class PaperNotificationServiceTest extends BaseServiceTest {

    @Autowired
    private PaperNotificationService paperNotificationService;

    @MockBean private AssignedToJudgeDocumentService assignedToJudgeDocumentService;
    @MockBean private ManualPaymentDocumentService manualPaymentDocumentService;
    @MockBean private BulkPrintService bulkPrintService;
    @MockBean private CaseDataService caseDataService;

    @Test
    public void sendAssignToJudgeNotificationLetterIfIsPaperApplication() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isPaperApplication(any())).thenReturn(true);

        paperNotificationService.printAssignToJudgeNotification(buildCaseDetails(), AUTH_TOKEN);

        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(any(CaseDetails.class), eq(AUTH_TOKEN), eq(APPLICANT));
        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(any(CaseDetails.class), eq(AUTH_TOKEN), eq(RESPONDENT));
        verify(bulkPrintService, times(2)).sendDocumentForPrint(any(), any());
    }

    @Test
    public void shouldNotSendApplicantConsentInContestedAssignToJudgeConfirmationNotification() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);

        paperNotificationService.printConsentInContestedAssignToJudgeConfirmationNotification(buildCaseDetails(), AUTH_TOKEN);

        verify(assignedToJudgeDocumentService, never()).generateConsentInContestedAssignedToJudgeNotificationLetter(
            any(CaseDetails.class), eq(AUTH_TOKEN), eq(APPLICANT));
        verify(assignedToJudgeDocumentService).generateConsentInContestedAssignedToJudgeNotificationLetter(
            any(CaseDetails.class), eq(AUTH_TOKEN), eq(RESPONDENT));
        verify(bulkPrintService).sendDocumentForPrint(any(), any());
    }

    @Test
    public void sendConsentInContestedAssignToJudgeNotificationLetterIfShouldSend() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);

        paperNotificationService.printConsentInContestedAssignToJudgeConfirmationNotification(buildCaseDetails(), AUTH_TOKEN);

        verify(assignedToJudgeDocumentService).generateConsentInContestedAssignedToJudgeNotificationLetter(any(), eq(AUTH_TOKEN), eq(APPLICANT));
        verify(assignedToJudgeDocumentService).generateConsentInContestedAssignedToJudgeNotificationLetter(any(), eq(AUTH_TOKEN), eq(RESPONDENT));
        verify(bulkPrintService, times(2)).sendDocumentForPrint(any(), any());
    }

    @Test
    public void sendContestedManualPaymentLetters() {
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(caseDataService.isContestedPaperApplication(any())).thenReturn(true);
        when(caseDataService.isPaperApplication(any())).thenReturn(true);

        paperNotificationService.printManualPaymentNotification(buildCaseDetails(), AUTH_TOKEN);

        verify(manualPaymentDocumentService).generateManualPaymentLetter(any(), any(), eq(APPLICANT));
        verify(manualPaymentDocumentService).generateManualPaymentLetter(any(), any(), eq(RESPONDENT));
        verify(bulkPrintService, times(2)).sendDocumentForPrint(any(), any());
    }
}
