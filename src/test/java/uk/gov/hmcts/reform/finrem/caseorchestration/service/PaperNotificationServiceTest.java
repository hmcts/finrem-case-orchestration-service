package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.generalapplication.service.RejectGeneralApplicationDocumentService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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

    @MockBean
    private AssignedToJudgeDocumentService assignedToJudgeDocumentService;
    @MockBean
    private ManualPaymentDocumentService manualPaymentDocumentService;
    @MockBean
    private BulkPrintService bulkPrintService;
    @MockBean
    private CaseDataService caseDataService;
    @MockBean
    private RejectGeneralApplicationDocumentService rejectGeneralApplicationDocumentService;

    @Test
    public void sendAssignToJudgeNotificationLetterIfIsPaperApplication() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isPaperApplication(any())).thenReturn(true);
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);

        paperNotificationService.printAssignToJudgeNotification(buildCaseDetails(), AUTH_TOKEN);

        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(any(CaseDetails.class), eq(AUTH_TOKEN), eq(APPLICANT));
        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(any(CaseDetails.class), eq(AUTH_TOKEN), eq(RESPONDENT));
        verify(bulkPrintService, times(2)).sendDocumentForPrint(any(), any(CaseDetails.class), any());
    }

    @Test
    public void shouldNotSendApplicantConsentInContestedAssignToJudgeConfirmationNotification() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);

        paperNotificationService.printConsentInContestedAssignToJudgeConfirmationNotification(buildCaseDetails(), AUTH_TOKEN);

        verify(assignedToJudgeDocumentService, never()).generateConsentInContestedAssignedToJudgeNotificationLetter(
            any(CaseDetails.class), eq(AUTH_TOKEN), eq(APPLICANT));
        verify(assignedToJudgeDocumentService).generateConsentInContestedAssignedToJudgeNotificationLetter(
            any(CaseDetails.class), eq(AUTH_TOKEN), eq(RESPONDENT));
        verify(bulkPrintService).sendDocumentForPrint(any(), any(CaseDetails.class), any());
    }

    @Test
    public void sendConsentInContestedAssignToJudgeNotificationLetterIfShouldSend() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);

        paperNotificationService.printConsentInContestedAssignToJudgeConfirmationNotification(buildCaseDetails(), AUTH_TOKEN);

        verify(assignedToJudgeDocumentService).generateConsentInContestedAssignedToJudgeNotificationLetter(any(), eq(AUTH_TOKEN), eq(APPLICANT));
        verify(assignedToJudgeDocumentService).generateConsentInContestedAssignedToJudgeNotificationLetter(any(), eq(AUTH_TOKEN), eq(RESPONDENT));
        verify(bulkPrintService, times(2)).sendDocumentForPrint(any(), any(CaseDetails.class), any());
    }

    @Test
    public void sendContestedManualPaymentLetters() {
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(caseDataService.isContestedPaperApplication(any())).thenReturn(true);
        when(caseDataService.isPaperApplication(any())).thenReturn(true);

        paperNotificationService.printManualPaymentNotification(buildCaseDetails(), AUTH_TOKEN);

        verify(manualPaymentDocumentService).generateManualPaymentLetter(any(), any(), eq(APPLICANT));
        verify(bulkPrintService).sendDocumentForPrint(any(), any(CaseDetails.class), any());
    }

    @Test
    public void shouldPrintForApplicantIfNotRepresented() {
        final String json
            = "/fixtures/refusal-order-contested.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(json, mapper);
        caseDetails.getData().put("applicantRepresented", "No");
        caseDetails.getData().remove("applicantSolicitorConsentForEmails");
        caseDetails.getData().put("paperApplication", "No");

        assertThat(paperNotificationService.shouldPrintForApplicant(caseDetails), is(true));
    }

    @Test
    public void shouldPrintForApplicantIfRepresentedButNotAgreedToEmail() {
        final String json
            = "/fixtures/refusal-order-contested.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(json, mapper);
        caseDetails.getData().put("applicantRepresented", "Yes");
        caseDetails.getData().put("applicantSolicitorConsentForEmails", "No");
        caseDetails.getData().put("paperApplication", "No");

        assertThat(paperNotificationService.shouldPrintForApplicant(caseDetails), is(true));
    }

    @Test
    public void shouldPrintForApplicantIfPaperCase() {
        final String json
            = "/fixtures/refusal-order-contested.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(json, mapper);
        caseDetails.getData().put("paperApplication", "YES");

        assertThat(paperNotificationService.shouldPrintForApplicant(caseDetails), is(true));
    }

    @Test
    public void givenValidCaseData_whenPrintApplicantRejection_thenCallBulkPrintService() {
        final String json
            = "/fixtures/refusal-order-contested.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(json, mapper);
        caseDetails.getData().put("paperApplication", "YES");
        CaseDocument caseDocument = CaseDocument.builder().documentFilename("general_application_rejected").build();

        when(rejectGeneralApplicationDocumentService.generateGeneralApplicationRejectionLetter(eq(caseDetails), any(), eq(APPLICANT)))
            .thenReturn(caseDocument);
        paperNotificationService.printApplicantRejectionGeneralApplication(caseDetails, AUTH_TOKEN);
        verify(bulkPrintService).sendDocumentForPrint(caseDocument, caseDetails, any());
    }

    @Test
    public void givenValidCaseData_whenPrintRespondentRejection_thenCallBulkPrintService() {
        final String json
            = "/fixtures/refusal-order-contested.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(json, mapper);
        caseDetails.getData().put("paperApplication", "YES");
        CaseDocument caseDocument = CaseDocument.builder().documentFilename("general_application_rejected").build();

        when(rejectGeneralApplicationDocumentService.generateGeneralApplicationRejectionLetter(eq(caseDetails), any(), eq(RESPONDENT)))
            .thenReturn(caseDocument);
        paperNotificationService.printRespondentRejectionGeneralApplication(caseDetails, AUTH_TOKEN);
        verify(bulkPrintService).sendDocumentForPrint(caseDocument, caseDetails, any());
    }
}
