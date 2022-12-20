package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssignToJudgeRespondentCorresponderTest {

    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AssignedToJudgeDocumentService assignedToJudgeDocumentService;

    AssignToJudgeRespondentCorresponder assignToJudgeRespondentCorresponder;

    private CaseDetails caseDetails;
    private CaseDocument caseDocument;

    protected static final String AUTHORISATION_TOKEN = "authorisationToken";

    @Before
    public void setUp() throws Exception {
        assignToJudgeRespondentCorresponder =
            new AssignToJudgeRespondentCorresponder(notificationService, bulkPrintService, assignedToJudgeDocumentService);
        caseDetails = CaseDetails.builder().build();
        caseDocument = CaseDocument.builder().build();
        when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT)).thenReturn(
            caseDocument);
    }

    @Test
    public void shouldGetDocumentToPrint() {
        CaseDocument result = assignToJudgeRespondentCorresponder.getDocumentToPrint(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        assertEquals(caseDocument, result);
        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    public void shouldSendLetterToRespondent() {
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        assignToJudgeRespondentCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);
        verify(bulkPrintService).sendDocumentForPrint(caseDocument, caseDetails);
    }

    @Test
    public void shouldSendEmailToRespondent() {

        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        assignToJudgeRespondentCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);
        verify(notificationService).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(caseDetails);
    }
}