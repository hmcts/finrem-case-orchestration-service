package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;

public class PaperNotificationServiceTest extends BaseServiceTest {

    private static final String TEST_JSON = "/fixtures/refusal-order-contested.json";
    public static final String RESPONDENT_FILE_NAME = "respondentFileName";

    @Autowired
    private PaperNotificationService paperNotificationService;

    @MockBean private AssignedToJudgeDocumentService assignedToJudgeDocumentService;
    @MockBean private ManualPaymentDocumentService manualPaymentDocumentService;
    @MockBean private BulkPrintService bulkPrintService;

    @Test
    public void sendAssignToJudgeNotificationLetterIfIsPaperApplication() {
        FinremCaseDetails caseDetails = buildFinremCaseDetails();
        FinremCaseData caseData = caseDetails.getCaseData();
        caseData.setCcdCaseType(CaseType.CONSENTED);
        caseData.setPaperApplication(YesOrNo.YES);
        caseData.getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.YES);
        Document applicantDocumentUnderTest = newDocument();
        Document respondentDocumentUnderTest = getRespondentTestDocument();

        when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(any(), any(), eq(APPLICANT)))
            .thenReturn(applicantDocumentUnderTest);
        when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(any(), any(), eq(RESPONDENT)))
            .thenReturn(respondentDocumentUnderTest);
        paperNotificationService.printAssignToJudgeNotification(caseDetails, AUTH_TOKEN);

        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(any(FinremCaseDetails.class), eq(AUTH_TOKEN), eq(APPLICANT));
        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(any(FinremCaseDetails.class), eq(AUTH_TOKEN), eq(RESPONDENT));
        verify(bulkPrintService, times(1)).sendDocumentForPrint(eq(applicantDocumentUnderTest), any());
        verify(bulkPrintService, times(1)).sendDocumentForPrint(eq(respondentDocumentUnderTest), any());
    }

    @Test
    public void shouldNotSendApplicantConsentInContestedAssignToJudgeConfirmationNotification() {
        FinremCaseDetails caseDetails = buildFinremCaseDetails();
        caseDetails.getCaseData().setCcdCaseType(CaseType.CONSENTED);
        caseDetails.getCaseData().setPaperApplication(YesOrNo.NO);
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        caseDetails.getCaseData().getContactDetailsWrapper().setSolicitorAgreeToReceiveEmails(YesOrNo.YES);
        caseDetails.getCaseData().getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.YES);
        Document respondentDocumentUnderTest = getRespondentTestDocument();

        when(assignedToJudgeDocumentService.generateConsentInContestedAssignedToJudgeNotificationLetter(any(), any(), eq(RESPONDENT)))
            .thenReturn(respondentDocumentUnderTest);

        paperNotificationService.printConsentInContestedAssignToJudgeConfirmationNotification(caseDetails, AUTH_TOKEN);

        verify(assignedToJudgeDocumentService, never()).generateConsentInContestedAssignedToJudgeNotificationLetter(
            any(FinremCaseDetails.class), eq(AUTH_TOKEN), eq(APPLICANT));
        verify(assignedToJudgeDocumentService).generateConsentInContestedAssignedToJudgeNotificationLetter(
            any(FinremCaseDetails.class), eq(AUTH_TOKEN), eq(RESPONDENT));
        verify(bulkPrintService).sendDocumentForPrint(eq(respondentDocumentUnderTest), any());
    }

    @Test
    public void sendConsentInContestedAssignToJudgeNotificationLetterIfShouldSend() {
        FinremCaseDetails caseDetails = buildFinremCaseDetails();
        caseDetails.getCaseData().setCcdCaseType(CaseType.CONSENTED);
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        caseDetails.getCaseData().getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.YES);
        Document applicantDocumentToTest = newDocument();
        Document respondentDocumentToTest = getRespondentTestDocument();

        when(assignedToJudgeDocumentService.generateConsentInContestedAssignedToJudgeNotificationLetter(any(), any(), eq(APPLICANT)))
            .thenReturn(applicantDocumentToTest);
        when(assignedToJudgeDocumentService.generateConsentInContestedAssignedToJudgeNotificationLetter(any(), any(), eq(RESPONDENT)))
            .thenReturn(respondentDocumentToTest);

        paperNotificationService.printConsentInContestedAssignToJudgeConfirmationNotification(caseDetails, AUTH_TOKEN);

        verify(assignedToJudgeDocumentService).generateConsentInContestedAssignedToJudgeNotificationLetter(any(), eq(AUTH_TOKEN), eq(APPLICANT));
        verify(assignedToJudgeDocumentService).generateConsentInContestedAssignedToJudgeNotificationLetter(any(), eq(AUTH_TOKEN), eq(RESPONDENT));
        verify(bulkPrintService, times(1)).sendDocumentForPrint(eq(applicantDocumentToTest), any());
        verify(bulkPrintService, times(1)).sendDocumentForPrint(eq(respondentDocumentToTest), any());
    }

    @Test
    public void sendContestedManualPaymentLetters() {
        FinremCaseDetails caseDetails = buildFinremCaseDetails();
        caseDetails.getCaseData().setCcdCaseType(CaseType.CONTESTED);
        caseDetails.getCaseData().setPaperApplication(YesOrNo.YES);
        caseDetails.getCaseData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);
        Document applicantDocumentUnderTest = newDocument();
        Document respondentDocumentUnderTest = getRespondentTestDocument();

        when(manualPaymentDocumentService.generateManualPaymentLetter(any(), any(), eq(APPLICANT)))
            .thenReturn(applicantDocumentUnderTest);
        when(manualPaymentDocumentService.generateManualPaymentLetter(any(), any(), eq(RESPONDENT)))
            .thenReturn(respondentDocumentUnderTest);

        paperNotificationService.printManualPaymentNotification(caseDetails, AUTH_TOKEN);

        verify(manualPaymentDocumentService).generateManualPaymentLetter(any(), any(), eq(APPLICANT));
        verify(manualPaymentDocumentService).generateManualPaymentLetter(any(), any(), eq(RESPONDENT));
        verify(bulkPrintService, times(1)).sendDocumentForPrint(eq(applicantDocumentUnderTest), any());
        verify(bulkPrintService, times(1)).sendDocumentForPrint(eq(respondentDocumentUnderTest), any());
    }

    @Test
    public void shouldPrintForApplicantIfNotRepresented() throws IOException {
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(getResource(TEST_JSON), mapper);
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantSolicitorConsentForEmails(null);
        caseDetails.getCaseData().setPaperApplication(YesOrNo.NO);

        assertThat(paperNotificationService.shouldPrintForApplicant(caseDetails), is(true));
    }

    @Test
    public void shouldPrintForApplicantIfRepresentedButNotAgreedToEmail() throws IOException {
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(getResource(TEST_JSON), mapper);
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantSolicitorConsentForEmails(YesOrNo.NO);
        caseDetails.getCaseData().setPaperApplication(YesOrNo.NO);

        assertThat(paperNotificationService.shouldPrintForApplicant(caseDetails), is(true));
    }

    @Test
    public void shouldPrintForApplicantIfPaperCase() throws IOException {
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(getResource(TEST_JSON), mapper);
        caseDetails.getCaseData().setPaperApplication(YesOrNo.YES);

        assertThat(paperNotificationService.shouldPrintForApplicant(caseDetails), is(true));
    }

    private Document getRespondentTestDocument() {
        Document respondentDocumentToTest = newDocument();
        respondentDocumentToTest.setFilename(RESPONDENT_FILE_NAME);
        return respondentDocumentToTest;
    }
}
