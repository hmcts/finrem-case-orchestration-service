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
public class AssignToJudgeApplicantCorresponderTest {

    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AssignedToJudgeDocumentService assignedToJudgeDocumentService;

    AssignToJudgeApplicantCorresponder assignToJudgeApplicantCorresponder;

    private CaseDetails caseDetails;
    private CaseDocument caseDocument;

    protected static final String AUTHORISATION_TOKEN = "authorisationToken";

    @Before
    public void setUp() throws Exception {
        assignToJudgeApplicantCorresponder =
            new AssignToJudgeApplicantCorresponder(notificationService, bulkPrintService, assignedToJudgeDocumentService);
        caseDetails = CaseDetails.builder().build();
        caseDocument = CaseDocument.builder().build();
        when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT)).thenReturn(
            caseDocument);
    }

    @Test
    public void shouldGetDocumentToPrint() {
        CaseDocument result = assignToJudgeApplicantCorresponder.getDocumentToPrint(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        assertEquals(caseDocument, result);
        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
    }

    @Test
    public void shouldSendLetterToApplicant() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        assignToJudgeApplicantCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);
        verify(bulkPrintService).sendDocumentForPrint(caseDocument, caseDetails);
    }

    @Test
    public void shouldSendEmailToApplicant() {

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        assignToJudgeApplicantCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);
        verify(notificationService).sendAssignToJudgeConfirmationEmailToApplicantSolicitor(caseDetails);
    }
}