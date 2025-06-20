package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consented;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremSingleLetterOrEmailAllPartiesCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.IssueApplicationConsentCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
class IssueApplicationConsentCorresponderTest {

    @TestLogs
    private final TestLogger logs = new TestLogger(FinremSingleLetterOrEmailAllPartiesCorresponder.class);

    private final CaseType[] testingCastTypes = new CaseType[] {CONSENTED, CONTESTED};

    private final CaseDocument expectedCaseDocument = expectedCaseDocument();

    @InjectMocks
    private IssueApplicationConsentCorresponder underTest;

    @Mock
    private AssignedToJudgeDocumentService assignedToJudgeDocumentService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private BulkPrintService bulkPrintService;

    @BeforeEach
    void setUp() {
        when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(any(FinremCaseDetails.class), eq(AUTH_TOKEN),
            any(DocumentHelper.PaperNotificationRecipient.class))).thenReturn(expectedCaseDocument);
    }

    @ParameterizedTest
    @EnumSource(value = DocumentHelper.PaperNotificationRecipient.class,
        names = {"APPLICANT", "RESPONDENT"})
    void givenAnyCaseTypeCase_whenGetDocumentToPrint_thenReturnExpectedDocument(DocumentHelper.PaperNotificationRecipient party) {
        for (CaseType caseType : testingCastTypes) {
            // Arrange
            FinremCaseDetails caseDetails = buildCaseDetails(caseType);
            switch (party) {
                case APPLICANT:
                    lenient().when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(caseDetails, AUTH_TOKEN,
                        DocumentHelper.PaperNotificationRecipient.RESPONDENT)).thenReturn(unexpectedCaseDocument());
                    break;
                case RESPONDENT:
                    lenient().when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(caseDetails, AUTH_TOKEN,
                        DocumentHelper.PaperNotificationRecipient.APPLICANT)).thenReturn(unexpectedCaseDocument());
                    break;
                default:
                    throw new IllegalStateException("Unreachable code");
            }

            // Act
            CaseDocument result = underTest.getDocumentToPrint(caseDetails, AUTH_TOKEN, party);

            // Assert
            assertThat(result).isEqualTo(expectedCaseDocument);
        }
    }

    @Test
    void givenAnyCaseTypeCase_whenApplicantSolicitorEmailPopulated_thenSendEmailToApplicantSolicitor() {
        for (CaseType caseType : testingCastTypes) {
            // Arrange
            FinremCaseDetails caseDetails = buildCaseDetails(caseType);
            when(notificationService.isApplicantSolicitorEmailPopulated(caseDetails)).thenReturn(true);

            // Act
            underTest.sendCorrespondence(caseDetails, AUTH_TOKEN);

            // Assert
            verify(notificationService).sendAssignToJudgeConfirmationEmailToApplicantSolicitor(caseDetails);
            verify(bulkPrintService, never()).sendDocumentForPrint(expectedCaseDocument, caseDetails, CCDConfigConstant.APPLICANT, AUTH_TOKEN);
        }
    }

    @Test
    void givenAnyCaseTypeCase_whenApplicantSolicitorEmailNotPopulated_thenSendLetter() {
        for (CaseType caseType : testingCastTypes) {
            // Arrange
            FinremCaseDetails caseDetails = buildCaseDetails(caseType);
            when(notificationService.isApplicantSolicitorEmailPopulated(caseDetails)).thenReturn(false);

            // Act
            underTest.sendCorrespondence(caseDetails, AUTH_TOKEN);

            // Assert
            verify(notificationService, never()).sendAssignToJudgeConfirmationEmailToApplicantSolicitor(caseDetails);
            verify(bulkPrintService).sendDocumentForPrint(expectedCaseDocument, caseDetails, CCDConfigConstant.APPLICANT, AUTH_TOKEN);
        }
    }

    @Test
    void givenAnyCaseTypeCase_whenRespondentSolicitorEmailPopulated_thenSendEmailToApplicantSolicitor() {
        for (CaseType caseType : testingCastTypes) {
            // Arrange
            FinremCaseDetails caseDetails = buildCaseDetails(caseType);
            when(notificationService.isRespondentSolicitorEmailPopulated(caseDetails)).thenReturn(true);

            // Act
            underTest.sendCorrespondence(caseDetails, AUTH_TOKEN);

            // Assert
            verify(notificationService).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(caseDetails);
            verify(bulkPrintService, never()).sendDocumentForPrint(expectedCaseDocument, caseDetails, CCDConfigConstant.RESPONDENT, AUTH_TOKEN);
        }
    }

    @Test
    void givenAnyCaseTypeCase_whenRespondentSolicitorEmailNotPopulated_thenSendLetter() {
        for (CaseType caseType : testingCastTypes) {
            // Arrange
            FinremCaseDetails caseDetails = buildCaseDetails(caseType);

            when(notificationService.isRespondentSolicitorEmailPopulated(caseDetails)).thenReturn(false);

            // Act
            underTest.sendCorrespondence(caseDetails, AUTH_TOKEN);

            // Assert
            verify(notificationService, never()).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(caseDetails);
            verify(bulkPrintService).sendDocumentForPrint(expectedCaseDocument, caseDetails, CCDConfigConstant.RESPONDENT, AUTH_TOKEN);
        }
    }

    @ParameterizedTest
    @NullSource
    @EnumSource(value = YesOrNo.class, names = {"NO"})
    void givenAnyCaseTypeCaseWithRespondentResideOutsideUK_whenRespondentSolicitorEmailNotPopulated_thenSendLetter(
        YesOrNo respondentResideOutsideUK) {
        for (CaseType caseType : testingCastTypes) {
            // Arrange
            FinremCaseData caseData = FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                    .respondentResideOutsideUK(respondentResideOutsideUK)
                    .build())
                .build();
            FinremCaseDetails caseDetails = buildCaseDetails(caseType, caseData);

            when(notificationService.isRespondentSolicitorEmailPopulated(caseDetails)).thenReturn(false);

            // Act
            underTest.sendCorrespondence(caseDetails, AUTH_TOKEN);

            // Assert
            verify(notificationService, never()).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(caseDetails);
            verify(bulkPrintService).sendDocumentForPrint(expectedCaseDocument, caseDetails, CCDConfigConstant.RESPONDENT, AUTH_TOKEN);
        }
    }

    @Test
    void givenAnyCaseTypeCase_whenRespondentSolicitorEmailNotPopulatedAndRespondentResidesOutsideUK_thenNotSendingLetter() {
        for (CaseType caseType : testingCastTypes) {
            // Arrange
            FinremCaseData caseData = FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                    .respondentResideOutsideUK(YesOrNo.YES)
                    .build())
                .build();
            FinremCaseDetails caseDetails = buildCaseDetails(caseType, caseData);

            when(notificationService.isRespondentSolicitorEmailPopulated(caseDetails)).thenReturn(false);

            // Act
            underTest.sendCorrespondence(caseDetails, AUTH_TOKEN);

            // Assert
            verify(notificationService, never()).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(caseDetails);
            verify(bulkPrintService, never()).sendDocumentForPrint(expectedCaseDocument, caseDetails, CCDConfigConstant.RESPONDENT, AUTH_TOKEN);
            assertThat(logs.getInfos()).contains(format("Nothing is sent to respondent for Case ID: %s", CASE_ID));
            logs.reset();
        }
    }

    @Test
    void givenContestedCase_whenIntervenerSolicitorEmailPopulated_thenSendEmailToIntervenerSolicitor() {
        // Arrange
        FinremCaseDetails caseDetails = buildCaseDetails(CONTESTED);
        SolicitorCaseDataKeysWrapper expectedSolicitorCaseDataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(any(IntervenerWrapper.class),
            any(FinremCaseDetails.class))).thenReturn(true);
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class)))
            .thenReturn(expectedSolicitorCaseDataKeysWrapper);

        // Act
        underTest.sendCorrespondence(caseDetails, AUTH_TOKEN);

        // Assert
        verify(notificationService, times(4))
            .sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(caseDetails, expectedSolicitorCaseDataKeysWrapper);
        verify(bulkPrintService, never()).sendDocumentForPrint(expectedCaseDocument, caseDetails, INTERVENER_ONE.getTypeValue(), AUTH_TOKEN);
        verify(bulkPrintService, never()).sendDocumentForPrint(expectedCaseDocument, caseDetails, INTERVENER_TWO.getTypeValue(), AUTH_TOKEN);
        verify(bulkPrintService, never()).sendDocumentForPrint(expectedCaseDocument, caseDetails, INTERVENER_THREE.getTypeValue(), AUTH_TOKEN);
        verify(bulkPrintService, never()).sendDocumentForPrint(expectedCaseDocument, caseDetails, INTERVENER_FOUR.getTypeValue(), AUTH_TOKEN);
    }

    @Test
    void givenContestedCase_whenIntervenerOneAndThreeSolicitorEmailPopulated_thenSendEmailToParticularIntervenerSolicitors() {
        // Arrange
        IntervenerOne i1 = IntervenerOne.builder().intervenerName("One").build();
        IntervenerThree i3 = IntervenerThree.builder().intervenerName("Three").build();
        FinremCaseData caseData = FinremCaseData.builder()
            .intervenerOne(i1).intervenerThree(i3)
            .build();
        FinremCaseDetails caseDetails = buildCaseDetails(CONTESTED, caseData);
        SolicitorCaseDataKeysWrapper expectedSolicitorCaseDataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(any(IntervenerWrapper.class),
            any(FinremCaseDetails.class))).thenReturn(false);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(eq(i1),
            any(FinremCaseDetails.class))).thenReturn(true);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(eq(i3),
            any(FinremCaseDetails.class))).thenReturn(true);
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class)))
            .thenReturn(expectedSolicitorCaseDataKeysWrapper);

        // Act
        underTest.sendCorrespondence(caseDetails, AUTH_TOKEN);

        // Assert
        verify(notificationService, times(2))
            .sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(caseDetails, expectedSolicitorCaseDataKeysWrapper);
        verify(bulkPrintService, never()).sendDocumentForPrint(expectedCaseDocument, caseDetails, INTERVENER_ONE.getTypeValue(), AUTH_TOKEN);
        verify(bulkPrintService, never()).sendDocumentForPrint(expectedCaseDocument, caseDetails, INTERVENER_TWO.getTypeValue(), AUTH_TOKEN);
        verify(bulkPrintService, never()).sendDocumentForPrint(expectedCaseDocument, caseDetails, INTERVENER_THREE.getTypeValue(), AUTH_TOKEN);
        verify(bulkPrintService, never()).sendDocumentForPrint(expectedCaseDocument, caseDetails, INTERVENER_FOUR.getTypeValue(), AUTH_TOKEN);
    }

    @Test
    void givenConsentCase_whenSendCorrespondence_thenNothingToBeSent() {
        // Arrange
        FinremCaseDetails caseDetails = buildCaseDetails(CONSENTED);
        SolicitorCaseDataKeysWrapper expectedSolicitorCaseDataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();

        // Act
        underTest.sendCorrespondence(caseDetails, AUTH_TOKEN);

        // Assert
        verify(notificationService, never())
            .sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(caseDetails, expectedSolicitorCaseDataKeysWrapper);
        verify(bulkPrintService, never()).sendDocumentForPrint(expectedCaseDocument, caseDetails, INTERVENER_ONE.getTypeValue(), AUTH_TOKEN);
        verify(bulkPrintService, never()).sendDocumentForPrint(expectedCaseDocument, caseDetails, INTERVENER_TWO.getTypeValue(), AUTH_TOKEN);
        verify(bulkPrintService, never()).sendDocumentForPrint(expectedCaseDocument, caseDetails, INTERVENER_THREE.getTypeValue(), AUTH_TOKEN);
        verify(bulkPrintService, never()).sendDocumentForPrint(expectedCaseDocument, caseDetails, INTERVENER_FOUR.getTypeValue(), AUTH_TOKEN);
    }

    @Test
    void givenContestedCaseWithIntvOne_whenIntervenerSolicitorEmailNotPopulated_thenSendLetter() {
        // Arrange
        FinremCaseData caseData = FinremCaseData.builder()
            .intervenerOne(IntervenerOne.builder().intervenerName("One").build())
            .build();
        FinremCaseDetails caseDetails = buildCaseDetails(CONTESTED, caseData);
        SolicitorCaseDataKeysWrapper expectedSolicitorCaseDataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(any(IntervenerWrapper.class),
            any(FinremCaseDetails.class))).thenReturn(false);

        // Act
        underTest.sendCorrespondence(caseDetails, AUTH_TOKEN);

        // Assert
        verify(notificationService, never())
            .sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(caseDetails, expectedSolicitorCaseDataKeysWrapper);
        verify(bulkPrintService).sendDocumentForPrint(expectedCaseDocument, caseDetails, INTERVENER_ONE.getTypeValue(), AUTH_TOKEN);
        verify(bulkPrintService, never()).sendDocumentForPrint(expectedCaseDocument, caseDetails, INTERVENER_TWO.getTypeValue(), AUTH_TOKEN);
        verify(bulkPrintService, never()).sendDocumentForPrint(expectedCaseDocument, caseDetails, INTERVENER_THREE.getTypeValue(), AUTH_TOKEN);
        verify(bulkPrintService, never()).sendDocumentForPrint(expectedCaseDocument, caseDetails, INTERVENER_FOUR.getTypeValue(), AUTH_TOKEN);
    }

    @Test
    void givenContestedCaseWithIntvOneAndThree_whenIntervenerSolicitorEmailNotPopulated_thenSendLetter() {
        // Arrange
        FinremCaseData caseData = FinremCaseData.builder()
            .intervenerOne(IntervenerOne.builder().intervenerName("One").build())
            .intervenerThree(IntervenerThree.builder().intervenerName("Three").build())
            .build();
        FinremCaseDetails caseDetails = buildCaseDetails(CONTESTED, caseData);
        SolicitorCaseDataKeysWrapper expectedSolicitorCaseDataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(any(IntervenerWrapper.class),
            any(FinremCaseDetails.class))).thenReturn(false);

        // Act
        underTest.sendCorrespondence(caseDetails, AUTH_TOKEN);

        // Assert
        verify(notificationService, never())
            .sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(caseDetails, expectedSolicitorCaseDataKeysWrapper);
        verify(bulkPrintService).sendDocumentForPrint(expectedCaseDocument, caseDetails, INTERVENER_ONE.getTypeValue(), AUTH_TOKEN);
        verify(bulkPrintService, never()).sendDocumentForPrint(expectedCaseDocument, caseDetails, INTERVENER_TWO.getTypeValue(), AUTH_TOKEN);
        verify(bulkPrintService).sendDocumentForPrint(expectedCaseDocument, caseDetails, INTERVENER_THREE.getTypeValue(), AUTH_TOKEN);
        verify(bulkPrintService, never()).sendDocumentForPrint(expectedCaseDocument, caseDetails, INTERVENER_FOUR.getTypeValue(), AUTH_TOKEN);
    }

    private FinremCaseDetails buildCaseDetails(CaseType caseType) {
        return buildCaseDetails(caseType, FinremCaseData.builder().ccdCaseType(caseType).build());
    }

    private FinremCaseDetails buildCaseDetails(CaseType caseType, FinremCaseData caseData) {
        caseData.setCcdCaseType(caseType);
        return FinremCaseDetailsBuilderFactory.from(CASE_ID, caseType, caseData)
            .build();
    }

    private CaseDocument expectedCaseDocument() {
        return CaseDocument.builder().documentFilename("expected").build();
    }

    private CaseDocument unexpectedCaseDocument() {
        return CaseDocument.builder().documentFilename("unexpected").build();
    }

}
