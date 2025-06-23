package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class FinremAssignToJudgeCorresponderTest {

    FinremAssignToJudgeCorresponder assignToJudgeCorresponder;

    @Mock
    NotificationService notificationService;

    @Mock
    BulkPrintService bulkPrintService;

    @Mock
    AssignedToJudgeDocumentService assignedToJudgeDocumentService;

    private CaseDocument caseDocument;

    @BeforeEach
    void setUp() {
        assignToJudgeCorresponder = new FinremAssignToJudgeCorresponder(notificationService, bulkPrintService, assignedToJudgeDocumentService);
        caseDocument = CaseDocument.builder().build();

        lenient().when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(any(FinremCaseDetails.class), eq(AUTH_TOKEN),
            eq(DocumentHelper.PaperNotificationRecipient.APPLICANT))).thenReturn(
            caseDocument);
        lenient().when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(any(FinremCaseDetails.class), eq(AUTH_TOKEN),
            eq(DocumentHelper.PaperNotificationRecipient.RESPONDENT))).thenReturn(
            caseDocument);
        lenient().when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(any(FinremCaseDetails.class), eq(AUTH_TOKEN),
            eq(DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE))).thenReturn(
            caseDocument);
    }

    @Test
    void shouldGetDocumentToPrintForApplicant() {
        FinremCaseDetails caseDetails = buildContestedCaseDetails();
        CaseDocument result = assignToJudgeCorresponder.getDocumentToPrint(caseDetails, AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        assertEquals(caseDocument, result);
        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(caseDetails, AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
    }

    @Test
    void shouldGetDocumentToPrintForRespondent() {
        FinremCaseDetails caseDetails = buildContestedCaseDetails();
        CaseDocument result = assignToJudgeCorresponder.getDocumentToPrint(caseDetails, AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        assertEquals(caseDocument, result);
        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(caseDetails, AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    void shouldGetDocumentToPrintForIntervener() {
        FinremCaseDetails caseDetails = buildContestedCaseDetails();
        CaseDocument result = assignToJudgeCorresponder.getDocumentToPrint(caseDetails, AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);
        assertEquals(caseDocument, result);
        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(caseDetails, AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);
    }

    @Test
    void shouldEmailApplicantSolicitor() {
        FinremCaseDetails caseDetails = buildContestedCaseDetails();
        when(notificationService.isApplicantSolicitorEmailPopulated(caseDetails)).thenReturn(true);
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTH_TOKEN);
        verify(notificationService).isApplicantSolicitorEmailPopulated(caseDetails);
        verify(notificationService).sendAssignToJudgeConfirmationEmailToApplicantSolicitor(caseDetails);
    }

    @Test
    void emailRespondentSolicitor() {
        FinremCaseDetails caseDetails = buildContestedCaseDetails();
        when(notificationService.isRespondentSolicitorEmailPopulated(caseDetails)).thenReturn(true);
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTH_TOKEN);
        verify(notificationService).isRespondentSolicitorEmailPopulated(caseDetails);
        verify(notificationService).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(caseDetails);
    }

    @Test
    void emailIntervenerSolicitorShouldSendToIntervenerOne() {
        // Arrange
        IntervenerOne intervenerOne = IntervenerOne.builder().intervenerSolEmail("1SolEmail").build();
        FinremCaseDetails caseDetails = buildContestedCaseDetails(FinremCaseData.builder().intervenerOne(intervenerOne));
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper
            .builder().build();

        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(any(IntervenerWrapper.class),
            eq(caseDetails))).thenReturn(false);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(intervenerOne,
            caseDetails)).thenReturn(true);
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerOne)).thenReturn(dataKeysWrapper);

        // Act
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTH_TOKEN);

        verify(notificationService).isIntervenerSolicitorDigitalAndEmailPopulated(intervenerOne, caseDetails);
        verify(notificationService).getCaseDataKeysForIntervenerSolicitor(intervenerOne);
        verify(notificationService).sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(caseDetails, dataKeysWrapper);
    }

    @Test
    void emailIntervenerSolicitorShouldSendToIntervenerTwo() {
        // Arrange
        IntervenerTwo intervenerTwo = IntervenerTwo.builder().intervenerSolEmail("2SolEmail").build();
        FinremCaseDetails caseDetails = buildContestedCaseDetails(FinremCaseData.builder().intervenerTwo(intervenerTwo));
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper
            .builder().build();

        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(any(IntervenerWrapper.class),
            eq(caseDetails))).thenReturn(false);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(intervenerTwo,
            caseDetails)).thenReturn(true);
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(caseDetails.getData().getIntervenerTwo()))
            .thenReturn(dataKeysWrapper);

        // Act
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTH_TOKEN);

        verify(notificationService).isIntervenerSolicitorDigitalAndEmailPopulated(intervenerTwo, caseDetails);
        verify(notificationService).getCaseDataKeysForIntervenerSolicitor(intervenerTwo);
        verify(notificationService).sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(caseDetails, dataKeysWrapper);
    }

    @Test
    void emailIntervenerSolicitorShouldSendToIntervenerThree() {
        // Arrange
        IntervenerThree intervenerThree = IntervenerThree.builder().intervenerSolEmail("3SolEmail").build();
        FinremCaseDetails caseDetails = buildContestedCaseDetails(FinremCaseData.builder().intervenerThree(intervenerThree));
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper
            .builder().build();

        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(any(IntervenerWrapper.class),
            eq(caseDetails))).thenReturn(false);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(intervenerThree,
            caseDetails)).thenReturn(true);
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerThree)).thenReturn(dataKeysWrapper);

        // Act
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTH_TOKEN);

        verify(notificationService).isIntervenerSolicitorDigitalAndEmailPopulated(intervenerThree, caseDetails);
        verify(notificationService).getCaseDataKeysForIntervenerSolicitor(intervenerThree);
        verify(notificationService).sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(caseDetails, dataKeysWrapper);
    }

    @Test
    void emailIntervenerSolicitorShouldSendToIntervenerFour() {
        // Arrange
        IntervenerFour intervenerFour = IntervenerFour.builder().intervenerSolEmail("4SolEmail").build();
        FinremCaseDetails caseDetails = buildContestedCaseDetails(FinremCaseData.builder().intervenerFour(intervenerFour));
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper
            .builder().build();

        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(any(IntervenerWrapper.class),
            eq(caseDetails))).thenReturn(false);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(intervenerFour,
            caseDetails)).thenReturn(true);
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerFour)).thenReturn(dataKeysWrapper);

        // Act
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTH_TOKEN);

        // Assert
        verify(notificationService).isIntervenerSolicitorDigitalAndEmailPopulated(intervenerFour, caseDetails);
        verify(notificationService).getCaseDataKeysForIntervenerSolicitor(intervenerFour);
        verify(notificationService).sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(caseDetails, dataKeysWrapper);
    }

    @Test
    void shouldSendLetterToApplicantAndRespondentAndIntervenerSolicitor() {
        // Arrange
        FinremCaseDetails caseDetails = buildContestedCaseDetails(FinremCaseData.builder()
            .intervenerOne(IntervenerOne.builder().intervenerName("intervenerName").build()));

        when(notificationService.isApplicantSolicitorEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorEmailPopulated(caseDetails)).thenReturn(false);

        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(any(IntervenerWrapper.class),
            eq(caseDetails))).thenReturn(false);

        // Act
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTH_TOKEN);

        // Assert
        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(caseDetails, AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(caseDetails, AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(caseDetails, AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);
        verify(bulkPrintService).sendDocumentForPrint(caseDocument, caseDetails, CCDConfigConstant.APPLICANT, AUTH_TOKEN);
        verify(bulkPrintService).sendDocumentForPrint(caseDocument, caseDetails, CCDConfigConstant.RESPONDENT, AUTH_TOKEN);
        verify(bulkPrintService).sendDocumentForPrint(caseDocument, caseDetails, IntervenerConstant.INTERVENER_ONE, AUTH_TOKEN);
    }

    private FinremCaseDetails buildContestedCaseDetails() {
        return buildContestedCaseDetails(FinremCaseData.builder());
    }

    private FinremCaseDetails buildContestedCaseDetails(FinremCaseData.FinremCaseDataBuilder caseDataBuilder) {
        return FinremCaseDetails.builder()
            .data(caseDataBuilder.ccdCaseType(CaseType.CONTESTED).build())
            .caseType(CaseType.CONTESTED)
            .build();
    }
}
