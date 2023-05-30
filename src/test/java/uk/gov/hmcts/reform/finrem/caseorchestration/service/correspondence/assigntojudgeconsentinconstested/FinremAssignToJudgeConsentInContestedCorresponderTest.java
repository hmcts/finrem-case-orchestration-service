package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudgeconsentinconstested;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudgeconsentincontested.FinremAssignToJudgeConsentInContestedCorresponder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@RunWith(MockitoJUnitRunner.class)
public class FinremAssignToJudgeConsentInContestedCorresponderTest {

    FinremAssignToJudgeConsentInContestedCorresponder corresponder;

    @Mock
    NotificationService notificationService;
    @Mock
    BulkPrintService bulkPrintService;
    @Mock
    AssignedToJudgeDocumentService assignedToJudgeDocumentService;

    private static final String AUTHORISATION_TOKEN = "authToken";

    private FinremCaseDetails caseDetails;

    private CaseDocument caseDocument;

    @Before
    public void setUp() throws Exception {
        corresponder = new FinremAssignToJudgeConsentInContestedCorresponder(notificationService, bulkPrintService, assignedToJudgeDocumentService);
        caseDetails = FinremCaseDetails.builder().build();
        caseDocument = CaseDocument.builder().build();
        when(assignedToJudgeDocumentService.generateConsentInContestedAssignedToJudgeNotificationLetter(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT)).thenReturn(
            caseDocument);
        when(assignedToJudgeDocumentService.generateConsentInContestedAssignedToJudgeNotificationLetter(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT)).thenReturn(
            caseDocument);
    }

    @Test
    public void shouldGetDocumentToPrintForApplicant() {
        CaseDocument result = corresponder.getDocumentToPrint(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        assertEquals(caseDocument, result);
        verify(assignedToJudgeDocumentService).generateConsentInContestedAssignedToJudgeNotificationLetter(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
    }

    @Test
    public void shouldGetDocumentToPrintForRespondent() {
        CaseDocument result = corresponder.getDocumentToPrint(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        assertEquals(caseDocument, result);
        verify(assignedToJudgeDocumentService)
            .generateConsentInContestedAssignedToJudgeNotificationLetter(
                caseDetails, AUTHORISATION_TOKEN, DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    public void shouldSendLetterToApplicantAndRespondentSolicitor() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        corresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);

        verify(assignedToJudgeDocumentService)
            .generateConsentInContestedAssignedToJudgeNotificationLetter(
                caseDetails, AUTHORISATION_TOKEN, DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        verify(assignedToJudgeDocumentService)
            .generateConsentInContestedAssignedToJudgeNotificationLetter(
                caseDetails, AUTHORISATION_TOKEN, DocumentHelper.PaperNotificationRecipient.APPLICANT);

        verify(bulkPrintService).sendDocumentForPrint(caseDocument, caseDetails, APPLICANT, AUTHORISATION_TOKEN);
        verify(bulkPrintService).sendDocumentForPrint(caseDocument, caseDetails, RESPONDENT, AUTHORISATION_TOKEN);
    }
}