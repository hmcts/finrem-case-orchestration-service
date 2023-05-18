package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssignToJudgeCorresponderTest {

    AssignToJudgeCorresponder assignToJudgeCorresponder;

    @Mock
    NotificationService notificationService;
    @Mock
    BulkPrintService bulkPrintService;
    @Mock
    AssignedToJudgeDocumentService assignedToJudgeDocumentService;

    private static final String AUTHORISATION_TOKEN = "authToken";

    private CaseDetails caseDetails;
    private CaseDocument caseDocument;

    @Before
    public void setUp() throws Exception {
        assignToJudgeCorresponder = new AssignToJudgeCorresponder(notificationService, bulkPrintService, assignedToJudgeDocumentService);
        caseDetails = CaseDetails.builder().build();
        caseDocument = CaseDocument.builder().build();
        when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT)).thenReturn(
            caseDocument);
        when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT)).thenReturn(
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
        String intervenerEmail = "intervener1SolEmail";
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper
            .builder().build();
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails,
            intervenerEmail, CaseRole.INTVR_SOLICITOR_1)).thenReturn(true);
        when(notificationService.getCaseDataKeysForIntervenerOneSolicitor()).thenReturn(dataKeysWrapper);
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails,
            intervenerEmail, CaseRole.INTVR_SOLICITOR_1);
        verify(notificationService).getCaseDataKeysForIntervenerOneSolicitor();
        verify(notificationService).sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(caseDetails, dataKeysWrapper);
    }

    @Test
    public void emailIntervenerSolicitorShouldSendToIntervenerTwo() {
        String intervenerEmail = "intervener2SolEmail";
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper
            .builder().build();
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails,
            intervenerEmail, CaseRole.INTVR_SOLICITOR_2)).thenReturn(true);
        when(notificationService.getCaseDataKeysForIntervenerTwoSolicitor()).thenReturn(dataKeysWrapper);
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails,
            intervenerEmail, CaseRole.INTVR_SOLICITOR_2);
        verify(notificationService).getCaseDataKeysForIntervenerTwoSolicitor();
        verify(notificationService).sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(caseDetails, dataKeysWrapper);
    }

    @Test
    public void emailIntervenerSolicitorShouldSendToIntervenerThree() {
        String intervenerEmail = "intervener3SolEmail";
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper
            .builder().build();
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails,
            intervenerEmail, CaseRole.INTVR_SOLICITOR_3)).thenReturn(true);
        when(notificationService.getCaseDataKeysForIntervenerThreeSolicitor()).thenReturn(dataKeysWrapper);
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails,
            intervenerEmail, CaseRole.INTVR_SOLICITOR_3);
        verify(notificationService).getCaseDataKeysForIntervenerThreeSolicitor();
        verify(notificationService).sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(caseDetails, dataKeysWrapper);
    }

    @Test
    public void emailIntervenerSolicitorShouldSendToIntervenerFour() {
        String intervenerEmail = "intervener4SolEmail";
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper
            .builder().build();
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails,
            intervenerEmail, CaseRole.INTVR_SOLICITOR_4)).thenReturn(true);
        when(notificationService.getCaseDataKeysForIntervenerFourSolicitor()).thenReturn(dataKeysWrapper);
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails,
            intervenerEmail, CaseRole.INTVR_SOLICITOR_4);
        verify(notificationService).getCaseDataKeysForIntervenerFourSolicitor();
        verify(notificationService).sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(caseDetails, dataKeysWrapper);
    }

    @Test
    public void shouldSendLetterToApplicantAndRespondentSolicitor() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);

        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);

        verify(bulkPrintService).sendDocumentForPrint(caseDocument, caseDetails, CCDConfigConstant.APPLICANT);
        verify(bulkPrintService).sendDocumentForPrint(caseDocument, caseDetails, CCDConfigConstant.RESPONDENT);
    }
}