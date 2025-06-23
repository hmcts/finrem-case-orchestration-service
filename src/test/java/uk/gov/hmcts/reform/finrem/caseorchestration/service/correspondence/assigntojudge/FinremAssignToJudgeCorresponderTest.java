package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType.INTERVENER_TWO;

@ExtendWith(MockitoExtension.class)
class FinremAssignToJudgeCorresponderTest {

    FinremAssignToJudgeCorresponder assignToJudgeCorresponder;

    @Mock
    NotificationService notificationService;

    @Mock
    BulkPrintService bulkPrintService;

    @Mock
    AssignedToJudgeDocumentService assignedToJudgeDocumentService;

    private CaseDocument expectedApplicantCaseDocument;

    private CaseDocument expectedRespondentCaseDocument;

    private CaseDocument expectedIntevenerOneCaseDocument;

    private CaseDocument expectedIntevenerTwoCaseDocument;

    private CaseDocument expectedIntevenerThreeCaseDocument;

    private CaseDocument expectedIntevenerFourCaseDocument;

    @BeforeEach
    void setUp() {
        assignToJudgeCorresponder = new FinremAssignToJudgeCorresponder(notificationService, bulkPrintService, assignedToJudgeDocumentService);
        expectedApplicantCaseDocument = expectedCaseDocument("applicant");
        expectedRespondentCaseDocument = expectedCaseDocument("respondent");
        expectedIntevenerOneCaseDocument = expectedCaseDocument("intevenerOne");
        expectedIntevenerTwoCaseDocument = expectedCaseDocument("intevenerTwo");
        expectedIntevenerThreeCaseDocument = expectedCaseDocument("intevenerThree");
        expectedIntevenerFourCaseDocument = expectedCaseDocument("intevenerFour");

        lenient().when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(any(FinremCaseDetails.class), eq(AUTH_TOKEN),
            eq(DocumentHelper.PaperNotificationRecipient.APPLICANT))).thenReturn(
            expectedApplicantCaseDocument);
        lenient().when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(any(FinremCaseDetails.class), eq(AUTH_TOKEN),
            eq(DocumentHelper.PaperNotificationRecipient.RESPONDENT))).thenReturn(
            expectedRespondentCaseDocument);
        lenient().when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(any(FinremCaseDetails.class), eq(AUTH_TOKEN),
            eq(DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE))).thenReturn(
            expectedIntevenerOneCaseDocument);
        lenient().when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(any(FinremCaseDetails.class), eq(AUTH_TOKEN),
            eq(DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO))).thenReturn(
            expectedIntevenerTwoCaseDocument);
        lenient().when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(any(FinremCaseDetails.class), eq(AUTH_TOKEN),
            eq(DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE))).thenReturn(
            expectedIntevenerThreeCaseDocument);
        lenient().when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(any(FinremCaseDetails.class), eq(AUTH_TOKEN),
            eq(DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR))).thenReturn(
            expectedIntevenerFourCaseDocument);
    }

    @Test
    void givenConsentedCase_whenGetDocumentToPrintInvoked_thenGenerateAssignedToJudgeNotificationLetterForApplicant() {
        // Arrange
        FinremCaseDetails caseDetails = buildCaseDetails(CONSENTED);

        // Act
        CaseDocument result = assignToJudgeCorresponder.getDocumentToPrint(caseDetails, AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);

        // Assert
        assertEquals(expectedApplicantCaseDocument, result);
        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(caseDetails, AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
    }

    @Test
    void givenConsentedCase_whenGetDocumentToPrintInvoked_thenGenerateAssignedToJudgeNotificationLetterForRespondent() {
        // Arrange
        FinremCaseDetails caseDetails = buildCaseDetails(CONSENTED);

        // Act
        CaseDocument result = assignToJudgeCorresponder.getDocumentToPrint(caseDetails, AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);

        // Assert
        assertEquals(expectedRespondentCaseDocument, result);
        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(caseDetails, AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    void givenConsentedCase_whenGetDocumentToPrintInvoked_thenGenerateAssignedToJudgeNotificationLetterForIntervener() {
        // Arrange
        FinremCaseDetails caseDetails = buildCaseDetails(CONSENTED);

        // Act
        CaseDocument result = assignToJudgeCorresponder.getDocumentToPrint(caseDetails, AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);

        // Assert
        assertEquals(expectedIntevenerOneCaseDocument, result);
        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(caseDetails, AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);
    }

    @Test
    void givenConsentedCase_whenApplicantSolicitorEmailPopulated_thenEmailApplicantSolicitor() {
        // Arrange
        FinremCaseDetails caseDetails = buildCaseDetails(CONSENTED);
        when(notificationService.isApplicantSolicitorEmailPopulated(caseDetails)).thenReturn(true);

        // Act
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTH_TOKEN);

        // Assert
        verify(notificationService).isApplicantSolicitorEmailPopulated(caseDetails);
        verify(notificationService).sendAssignToJudgeConfirmationEmailToApplicantSolicitor(caseDetails);
    }

    @Test
    void givenConsentedCase_whenRespondentSolicitorEmailPopulated_thenEmailRespondentSolicitor() {
        // Arrange
        FinremCaseDetails caseDetails = buildCaseDetails(CONSENTED);
        when(notificationService.isRespondentSolicitorEmailPopulated(caseDetails)).thenReturn(true);

        // Act
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTH_TOKEN);

        // Assert
        verify(notificationService).isRespondentSolicitorEmailPopulated(caseDetails);
        verify(notificationService).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(caseDetails);
    }

    @Test
    void givenContestedCase_whenIntervenerSolicitorPopulated_thenSendToIntervenerOne() {
        // Arrange
        IntervenerOne intervenerOne = IntervenerOne.builder().intervenerSolEmail("1SolEmail").build();
        FinremCaseDetails caseDetails = buildCaseDetails(CONTESTED, FinremCaseData.builder().intervenerOne(intervenerOne).build());
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
    void givenContestedCase_whenIntervenerSolicitorPopulated_thenSendToIntervenerTwo() {
        // Arrange
        IntervenerTwo intervenerTwo = IntervenerTwo.builder().intervenerSolEmail("2SolEmail").build();
        FinremCaseDetails caseDetails = buildCaseDetails(CONTESTED, FinremCaseData.builder().intervenerTwo(intervenerTwo).build());
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
    void givenContestedCase_whenIntervenerSolicitorPopulated_thenSendToIntervenerThree() {
        // Arrange
        IntervenerThree intervenerThree = IntervenerThree.builder().intervenerSolEmail("3SolEmail").build();
        FinremCaseDetails caseDetails = buildCaseDetails(CONTESTED, FinremCaseData.builder().intervenerThree(intervenerThree).build());
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
    void givenContestedCase_whenIntervenerSolicitorPopulated_thenSendToIntervenerFour() {
        // Arrange
        IntervenerFour intervenerFour = IntervenerFour.builder().intervenerSolEmail("4SolEmail").build();
        FinremCaseDetails caseDetails = buildCaseDetails(CONTESTED, FinremCaseData.builder().intervenerFour(intervenerFour).build());
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
    void givenContestedCase_whenEmailNotPopulated_thenSendLetterToApplicantAndRespondentAndIntervenerSolicitor() {
        // Arrange
        FinremCaseDetails caseDetails = buildCaseDetails(CONTESTED, FinremCaseData.builder()
            .intervenerOne(IntervenerOne.builder().intervenerName("intervenerName").build()).build());

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
        verify(bulkPrintService).sendDocumentForPrint(expectedApplicantCaseDocument, caseDetails, CCDConfigConstant.APPLICANT, AUTH_TOKEN);
        verify(bulkPrintService).sendDocumentForPrint(expectedRespondentCaseDocument, caseDetails, CCDConfigConstant.RESPONDENT, AUTH_TOKEN);
        verify(bulkPrintService).sendDocumentForPrint(expectedIntevenerOneCaseDocument, caseDetails, IntervenerConstant.INTERVENER_ONE, AUTH_TOKEN);
    }

    @ParameterizedTest
    @NullSource
    @EnumSource(value = YesOrNo.class, names = {"NO"})
    void givenConsentedCaseWithRespondentResideOutsideUK_whenRespondentSolicitorEmailNotPopulated_thenSendLetter(
        YesOrNo respondentResideOutsideUK) {
        // Arrange
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .respondentResideOutsideUK(respondentResideOutsideUK)
                .build())
            .build();
        FinremCaseDetails caseDetails = buildCaseDetails(CONSENTED, caseData);
        when(notificationService.isRespondentSolicitorEmailPopulated(caseDetails)).thenReturn(false);

        // Act
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTH_TOKEN);

        // Assert
        verify(notificationService, never()).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(caseDetails);
        verify(bulkPrintService).sendDocumentForPrint(expectedRespondentCaseDocument, caseDetails, CCDConfigConstant.RESPONDENT, AUTH_TOKEN);
    }

    @Test
    void givenConsentedCase_whenRespondentSolicitorEmailNotPopulatedAndRespondentResidesOutsideUK_thenNotSendingLetter() {
        // Arrange
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .respondentResideOutsideUK(YesOrNo.YES)
                .build())
            .build();
        FinremCaseDetails caseDetails = buildCaseDetails(CONSENTED, caseData);
        when(notificationService.isRespondentSolicitorEmailPopulated(caseDetails)).thenReturn(false);

        // Act
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTH_TOKEN);

        // Assert
        verify(notificationService, never()).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(caseDetails);
        verify(bulkPrintService).sendDocumentForPrint(expectedRespondentCaseDocument, caseDetails, CCDConfigConstant.RESPONDENT, AUTH_TOKEN);
    }

    @Test
    void givenConsentedCase_whenSendCorrespondence_thenNothingToBeSentToInterveners() {
        // Arrange
        FinremCaseDetails caseDetails = buildCaseDetails(CONSENTED);
        SolicitorCaseDataKeysWrapper expectedSolicitorCaseDataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();

        // Act
        assignToJudgeCorresponder.sendCorrespondence(caseDetails, AUTH_TOKEN);

        // Assert
        verify(notificationService, never())
            .sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(caseDetails, expectedSolicitorCaseDataKeysWrapper);
        verify(bulkPrintService, never()).sendDocumentForPrint(expectedIntevenerOneCaseDocument, caseDetails, INTERVENER_ONE.getTypeValue(),
            AUTH_TOKEN);
        verify(bulkPrintService, never()).sendDocumentForPrint(expectedIntevenerTwoCaseDocument, caseDetails, INTERVENER_TWO.getTypeValue(),
            AUTH_TOKEN);
        verify(bulkPrintService, never()).sendDocumentForPrint(expectedIntevenerThreeCaseDocument, caseDetails, INTERVENER_THREE.getTypeValue(),
            AUTH_TOKEN);
        verify(bulkPrintService, never()).sendDocumentForPrint(expectedIntevenerFourCaseDocument, caseDetails, INTERVENER_FOUR.getTypeValue(),
            AUTH_TOKEN);
    }

    private FinremCaseDetails buildCaseDetails(CaseType caseType) {
        return buildCaseDetails(caseType, FinremCaseData.builder().ccdCaseType(caseType).build());
    }

    private FinremCaseDetails buildCaseDetails(CaseType caseType, FinremCaseData caseData) {
        caseData.setCcdCaseType(caseType);
        return FinremCaseDetailsBuilderFactory.from(CASE_ID, caseType, caseData)
            .build();
    }

    private CaseDocument expectedCaseDocument(String type) {
        return CaseDocument.builder().documentFilename("expected_" + type).build();
    }
}
