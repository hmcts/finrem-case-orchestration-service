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
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);
        verify(notificationService).isRespondentSolicitorDigitalAndEmailPopulated(caseDetails);
        verify(notificationService).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(caseDetails);
    }

    @Test
    public void emailIntervenerSolicitorShouldSendToIntervenerOne() {
        String intervenerEmail = "1SolEmail";
        caseDetails.getData().getIntervenerOneWrapper().setIntervenerSolEmail(intervenerEmail);
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper
            .builder().build();
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerOneWrapper(),
            caseDetails)).thenReturn(true);
        when(notificationService.getFinremCaseDataKeysForIntervenerSolicitor(caseDetails.getData().getIntervenerOneWrapper()))
            .thenReturn(dataKeysWrapper);
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerOneWrapper(),
            caseDetails);
        verify(notificationService).getFinremCaseDataKeysForIntervenerSolicitor(caseDetails.getData().getIntervenerOneWrapper());
        verify(notificationService).sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(caseDetails, dataKeysWrapper);
    }

    @Test
    public void emailIntervenerSolicitorShouldSendToIntervenerTwo() {
        String intervenerEmail = "2SolEmail";
        caseDetails.getData().getIntervenerTwoWrapper().setIntervenerSolEmail(intervenerEmail);
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper
            .builder().build();
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerTwoWrapper(),
            caseDetails)).thenReturn(true);
        when(notificationService.getFinremCaseDataKeysForIntervenerSolicitor(caseDetails.getData().getIntervenerTwoWrapper()))
            .thenReturn(dataKeysWrapper);
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerTwoWrapper(),
            caseDetails);
        verify(notificationService).getFinremCaseDataKeysForIntervenerSolicitor(caseDetails.getData().getIntervenerTwoWrapper());
        verify(notificationService).sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(caseDetails, dataKeysWrapper);
    }

    @Test
    public void emailIntervenerSolicitorShouldSendToIntervenerThree() {
        String intervenerEmail = "3SolEmail";
        caseDetails.getData().getIntervenerThreeWrapper().setIntervenerSolEmail(intervenerEmail);
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper
            .builder().build();
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerThreeWrapper(),
            caseDetails)).thenReturn(true);
        when(notificationService.getFinremCaseDataKeysForIntervenerSolicitor(caseDetails.getData().getIntervenerThreeWrapper()))
            .thenReturn(dataKeysWrapper);
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerThreeWrapper(),
            caseDetails);
        verify(notificationService).getFinremCaseDataKeysForIntervenerSolicitor(caseDetails.getData().getIntervenerThreeWrapper());
        verify(notificationService).sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(caseDetails, dataKeysWrapper);
    }

    @Test
    public void emailIntervenerSolicitorShouldSendToIntervenerFour() {
        String intervenerEmail = "4SolEmail";
        caseDetails.getData().getIntervenerFourWrapper().setIntervenerSolEmail(intervenerEmail);
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper
            .builder().build();
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerFourWrapper(),
            caseDetails)).thenReturn(true);
        when(notificationService.getFinremCaseDataKeysForIntervenerSolicitor(caseDetails.getData().getIntervenerFourWrapper()))
            .thenReturn(dataKeysWrapper);
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerFourWrapper(),
            caseDetails);
        verify(notificationService).getFinremCaseDataKeysForIntervenerSolicitor(caseDetails.getData().getIntervenerFourWrapper());
        verify(notificationService).sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(caseDetails, dataKeysWrapper);
    }

    @Test
    public void shouldSendLetterToApplicantAndRespondentAndIntervenerSolicitor() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);

        caseDetails.getData().getIntervenerOneWrapper().setIntervenerName("intervenerName");
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerOneWrapper(),
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
