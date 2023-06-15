package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
    FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    AssignedToJudgeDocumentService assignedToJudgeDocumentService;

    private static final String AUTHORISATION_TOKEN = "authToken";

    private CaseDetails caseDetails;
    private CaseDocument caseDocument;

    @Before
    public void setUp() throws Exception {
        assignToJudgeCorresponder = new AssignToJudgeCorresponder(notificationService, bulkPrintService, finremCaseDetailsMapper,
            assignedToJudgeDocumentService);
        caseDetails = CaseDetails.builder().build();
        caseDocument = CaseDocument.builder().build();
        when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT)).thenReturn(
            caseDocument);
        when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT)).thenReturn(
            caseDocument);
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)).thenReturn(FinremCaseDetails.builder()
            .data(FinremCaseData.builder().build()).build());
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
        when(notificationService.isContestedApplication(caseDetails)).thenReturn(true);
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)).thenReturn(FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .intervenerOneWrapper(IntervenerOneWrapper.builder()
                    .intervenerSolEmail(intervenerEmail)
                    .build())
                .build())
            .build());
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(IntervenerOneWrapper.builder()
                .intervenerSolEmail(intervenerEmail).build(), caseDetails)).thenReturn(true);
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class))).thenReturn(dataKeysWrapper);
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).isIntervenerSolicitorDigitalAndEmailPopulated(IntervenerOneWrapper.builder()
                .intervenerSolEmail(intervenerEmail).build(),caseDetails);
    }

    @Test
    public void emailIntervenerSolicitorShouldSendToIntervenerTwo() {
        String intervenerEmail = "intervener2SolEmail";
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper
            .builder().build();
        when(notificationService.isContestedApplication(caseDetails)).thenReturn(true);
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)).thenReturn(FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .intervenerTwoWrapper(IntervenerTwoWrapper.builder()
                    .intervenerSolEmail(intervenerEmail)
                    .build())
                .build())
            .build());
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(IntervenerTwoWrapper.builder()
                .intervenerSolEmail(intervenerEmail).build(), caseDetails)).thenReturn(true);
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class))).thenReturn(dataKeysWrapper);
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).isIntervenerSolicitorDigitalAndEmailPopulated(IntervenerTwoWrapper.builder()
                .intervenerSolEmail(intervenerEmail).build(),caseDetails);
    }

    @Test
    public void emailIntervenerSolicitorShouldSendToIntervenerThree() {
        String intervenerEmail = "intervener3SolEmail";
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper
            .builder().build();
        when(notificationService.isContestedApplication(caseDetails)).thenReturn(true);
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)).thenReturn(FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .intervenerThreeWrapper(IntervenerThreeWrapper.builder()
                    .intervenerSolEmail(intervenerEmail)
                    .build())
                .build())
            .build());
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(IntervenerThreeWrapper.builder()
                .intervenerSolEmail(intervenerEmail).build(), caseDetails)).thenReturn(true);
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class))).thenReturn(dataKeysWrapper);
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).isIntervenerSolicitorDigitalAndEmailPopulated(IntervenerThreeWrapper.builder()
                .intervenerSolEmail(intervenerEmail).build(), caseDetails);
    }

    @Test
    public void emailIntervenerSolicitorShouldSendToIntervenerFour() {
        String intervenerEmail = "intervener4SolEmail";
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper
            .builder().build();
        when(notificationService.isContestedApplication(caseDetails)).thenReturn(true);
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)).thenReturn(FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .intervenerFourWrapper(IntervenerFourWrapper.builder()
                    .intervenerSolEmail(intervenerEmail)
                    .build())
                .build())
            .build());
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(IntervenerFourWrapper.builder()
                .intervenerSolEmail(intervenerEmail).build(), caseDetails)).thenReturn(true);
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class))).thenReturn(dataKeysWrapper);
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).isIntervenerSolicitorDigitalAndEmailPopulated(IntervenerFourWrapper.builder()
                .intervenerSolEmail(intervenerEmail).build(), caseDetails);
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

        verify(bulkPrintService).sendDocumentForPrint(caseDocument, caseDetails, CCDConfigConstant.APPLICANT, AUTHORISATION_TOKEN);
        verify(bulkPrintService).sendDocumentForPrint(caseDocument, caseDetails, CCDConfigConstant.RESPONDENT, AUTHORISATION_TOKEN);
    }
}