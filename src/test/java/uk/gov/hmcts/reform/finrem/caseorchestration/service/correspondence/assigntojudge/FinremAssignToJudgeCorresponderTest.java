package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FinremAssignToJudgeCorresponderTest {

    FinremAssignToJudgeCorresponder assignToJudgeCorresponder;

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
        assignToJudgeCorresponder = new FinremAssignToJudgeCorresponder(notificationService, bulkPrintService, assignedToJudgeDocumentService);
        caseDetails = FinremCaseDetails.builder()
            .data(FinremCaseData.builder().build())
            .caseType(CaseType.CONTESTED)
            .build();
        caseDocument = CaseDocument.builder().build();
        when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT)).thenReturn(
            caseDocument);
        when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT)).thenReturn(
            caseDocument);
        when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE)).thenReturn(
            caseDocument);
    }

    @Test
    public void shouldGetDocumentToPrintForApplicant() {
        CaseDocument result = assignToJudgeCorresponder.getDocumentToPrint(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        assertEquals(caseDocument, result);
        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
    }

    @Test
    public void shouldGetDocumentToPrintForRespondent() {
        CaseDocument result = assignToJudgeCorresponder.getDocumentToPrint(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        assertEquals(caseDocument, result);
        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    public void shouldGetDocumentToPrintForIntervener() {
        CaseDocument result = assignToJudgeCorresponder.getDocumentToPrint(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);
        assertEquals(caseDocument, result);
        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);
    }

    @Test
    public void shouldEmailApplicantSolcitor() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);
        verify(notificationService).isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
        verify(notificationService).sendAssignToJudgeConfirmationEmailToApplicantSolicitor(caseDetails);
    }

    @Test
    public void emailRespondentSolicitor() {
        when(notificationService.isRespondentSolicitorEmailPopulated(caseDetails)).thenReturn(true);
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);
        verify(notificationService).isRespondentSolicitorEmailPopulated(caseDetails);
        verify(notificationService).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(caseDetails);
    }

    @Test
    public void emailIntervenerSolicitorShouldSendToIntervenerOne() {
        String intervenerEmail = "1SolEmail";
        caseDetails.getData().getIntervenerOne().setIntervenerSolEmail(intervenerEmail);
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper
            .builder().build();
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerOne(),
            caseDetails)).thenReturn(true);
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(caseDetails.getData().getIntervenerOne()))
            .thenReturn(dataKeysWrapper);
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerOne(),
            caseDetails);
        verify(notificationService).getCaseDataKeysForIntervenerSolicitor(caseDetails.getData().getIntervenerOne());
        verify(notificationService).sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(caseDetails, dataKeysWrapper);
    }

    @Test
    public void emailIntervenerSolicitorShouldSendToIntervenerTwo() {
        String intervenerEmail = "2SolEmail";
        caseDetails.getData().getIntervenerTwo().setIntervenerSolEmail(intervenerEmail);
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper
            .builder().build();
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerTwo(),
            caseDetails)).thenReturn(true);
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(caseDetails.getData().getIntervenerTwo()))
            .thenReturn(dataKeysWrapper);
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerTwo(),
            caseDetails);
        verify(notificationService).getCaseDataKeysForIntervenerSolicitor(caseDetails.getData().getIntervenerTwo());
        verify(notificationService).sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(caseDetails, dataKeysWrapper);
    }

    @Test
    public void emailIntervenerSolicitorShouldSendToIntervenerThree() {
        String intervenerEmail = "3SolEmail";
        caseDetails.getData().getIntervenerThree().setIntervenerSolEmail(intervenerEmail);
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper
            .builder().build();
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerThree(),
            caseDetails)).thenReturn(true);
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(caseDetails.getData().getIntervenerThree()))
            .thenReturn(dataKeysWrapper);
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerThree(),
            caseDetails);
        verify(notificationService).getCaseDataKeysForIntervenerSolicitor(caseDetails.getData().getIntervenerThree());
        verify(notificationService).sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(caseDetails, dataKeysWrapper);
    }

    @Test
    public void emailIntervenerSolicitorShouldSendToIntervenerFour() {
        String intervenerEmail = "4SolEmail";
        caseDetails.getData().getIntervenerFour().setIntervenerSolEmail(intervenerEmail);
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper
            .builder().build();
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerFour(),
            caseDetails)).thenReturn(true);
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(caseDetails.getData().getIntervenerFour()))
            .thenReturn(dataKeysWrapper);
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerFour(),
            caseDetails);
        verify(notificationService).getCaseDataKeysForIntervenerSolicitor(caseDetails.getData().getIntervenerFour());
        verify(notificationService).sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(caseDetails, dataKeysWrapper);
    }

    @Test
    public void shouldSendLetterToApplicantAndRespondentAndIntervenerSolicitor() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorEmailPopulated(caseDetails)).thenReturn(false);

        caseDetails.getData().getIntervenerOne().setIntervenerName("intervenerName");
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerOne(),
            caseDetails)).thenReturn(false);

        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);

        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);


        verify(bulkPrintService).sendDocumentForPrint(caseDocument, caseDetails, CCDConfigConstant.APPLICANT, AUTHORISATION_TOKEN);
        verify(bulkPrintService).sendDocumentForPrint(caseDocument, caseDetails, CCDConfigConstant.RESPONDENT, AUTHORISATION_TOKEN);
        verify(bulkPrintService).sendDocumentForPrint(caseDocument, caseDetails, IntervenerConstant.INTERVENER_ONE, AUTHORISATION_TOKEN);
    }
}
