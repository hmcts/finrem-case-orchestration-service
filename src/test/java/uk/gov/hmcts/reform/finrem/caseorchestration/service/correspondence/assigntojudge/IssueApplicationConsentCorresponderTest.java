package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremSingleLetterOrEmailAllPartiesCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType.INTERVENER_TWO;

@ExtendWith(MockitoExtension.class)
class IssueApplicationConsentCorresponderTest {

    @TestLogs
    private final TestLogger logs = new TestLogger(FinremSingleLetterOrEmailAllPartiesCorresponder.class);

    private final CaseDocument expectedApplicantCaseDocument = expectedCaseDocument("applicant");

    private final CaseDocument expectedRespondentCaseDocument = expectedCaseDocument("respondent");

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
        lenient().when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(any(FinremCaseDetails.class), eq(AUTH_TOKEN),
            eq(DocumentHelper.PaperNotificationRecipient.APPLICANT))).thenReturn(expectedApplicantCaseDocument);
        lenient().when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(any(FinremCaseDetails.class), eq(AUTH_TOKEN),
            eq(DocumentHelper.PaperNotificationRecipient.RESPONDENT))).thenReturn(expectedRespondentCaseDocument);
    }

    @ParameterizedTest
    @EnumSource(value = DocumentHelper.PaperNotificationRecipient.class,
        names = {"APPLICANT", "RESPONDENT"})
    void givenConsentedCase_whenGetDocumentToPrint_thenReturnExpectedDocument(DocumentHelper.PaperNotificationRecipient party) {
        // Arrange
        FinremCaseDetails caseDetails = buildCaseDetails(CONSENTED);
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
        switch (party) {
            case APPLICANT:
                assertThat(result).isEqualTo(expectedApplicantCaseDocument);
                break;
            case RESPONDENT:
                assertThat(result).isEqualTo(expectedRespondentCaseDocument);
                break;
            default:
                throw new IllegalStateException("Unreachable code");
        }
    }

    @Test
    void givenConsentedCase_whenApplicantSolicitorEmailPopulated_thenSendEmailToApplicantSolicitor() {
        // Arrange
        FinremCaseDetails caseDetails = buildCaseDetails(CONSENTED);
        when(notificationService.isApplicantSolicitorEmailPopulated(caseDetails)).thenReturn(true);

        // Act
        underTest.sendCorrespondence(caseDetails, AUTH_TOKEN);

        // Assert
        verify(notificationService).sendAssignToJudgeConfirmationEmailToApplicantSolicitor(caseDetails);
        verify(bulkPrintService, never()).sendDocumentForPrint(expectedApplicantCaseDocument, caseDetails, CCDConfigConstant.APPLICANT, AUTH_TOKEN);
    }

    @Test
    void givenConsentedCase_whenApplicantSolicitorEmailNotPopulated_thenSendLetter() {
        // Arrange
        FinremCaseDetails caseDetails = buildCaseDetails(CONSENTED);
        when(notificationService.isApplicantSolicitorEmailPopulated(caseDetails)).thenReturn(false);

        // Act
        underTest.sendCorrespondence(caseDetails, AUTH_TOKEN);

        // Assert
        verify(notificationService, never()).sendAssignToJudgeConfirmationEmailToApplicantSolicitor(caseDetails);
        verify(bulkPrintService).sendDocumentForPrint(expectedApplicantCaseDocument, caseDetails, CCDConfigConstant.APPLICANT, AUTH_TOKEN);
    }

    @Test
    void givenConsentedCase_whenRespondentSolicitorEmailPopulated_thenSendEmailToApplicantSolicitor() {
        // Arrange
        FinremCaseDetails caseDetails = buildCaseDetails(CONSENTED);
        when(notificationService.isRespondentSolicitorEmailPopulated(caseDetails)).thenReturn(true);

        // Act
        underTest.sendCorrespondence(caseDetails, AUTH_TOKEN);

        // Assert
        verify(notificationService).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(caseDetails);
        verify(bulkPrintService, never()).sendDocumentForPrint(expectedRespondentCaseDocument, caseDetails, CCDConfigConstant.RESPONDENT, AUTH_TOKEN);
    }

    @Test
    void givenConsentedCase_whenRespondentSolicitorEmailNotPopulated_thenSendLetter() {
        // Arrange
        FinremCaseDetails caseDetails = buildCaseDetails(CONSENTED);

        when(notificationService.isRespondentSolicitorEmailPopulated(caseDetails)).thenReturn(false);

        // Act
        underTest.sendCorrespondence(caseDetails, AUTH_TOKEN);

        // Assert
        verify(notificationService, never()).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(caseDetails);
        verify(bulkPrintService).sendDocumentForPrint(expectedRespondentCaseDocument, caseDetails, CCDConfigConstant.RESPONDENT, AUTH_TOKEN);
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
        underTest.sendCorrespondence(caseDetails, AUTH_TOKEN);

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
        underTest.sendCorrespondence(caseDetails, AUTH_TOKEN);

        // Assert
        verify(notificationService, never()).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(caseDetails);
        verify(bulkPrintService, never()).sendDocumentForPrint(expectedRespondentCaseDocument, caseDetails, CCDConfigConstant.RESPONDENT, AUTH_TOKEN);
        assertThat(logs.getInfos()).contains(format("Nothing is sent to respondent for Case ID: %s", CASE_ID));
        logs.reset();
    }

    @Test
    void givenConsentedCase_whenSendCorrespondence_thenNothingToBeSentToInterveners() {
        // Arrange
        FinremCaseDetails caseDetails = buildCaseDetails(CONSENTED);
        SolicitorCaseDataKeysWrapper expectedSolicitorCaseDataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();

        // Act
        underTest.sendCorrespondence(caseDetails, AUTH_TOKEN);

        // Assert
        verify(notificationService, never())
            .sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(caseDetails, expectedSolicitorCaseDataKeysWrapper);
        verify(bulkPrintService, never()).sendDocumentForPrint(any(CaseDocument.class), eq(caseDetails),
            eq(INTERVENER_ONE.getTypeValue()), eq(AUTH_TOKEN));
        verify(bulkPrintService, never()).sendDocumentForPrint(any(CaseDocument.class), eq(caseDetails),
            eq(INTERVENER_TWO.getTypeValue()), eq(AUTH_TOKEN));
        verify(bulkPrintService, never()).sendDocumentForPrint(any(CaseDocument.class), eq(caseDetails),
            eq(INTERVENER_THREE.getTypeValue()), eq(AUTH_TOKEN));
        verify(bulkPrintService, never()).sendDocumentForPrint(any(CaseDocument.class), eq(caseDetails),
            eq(INTERVENER_FOUR.getTypeValue()), eq(AUTH_TOKEN));
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

    private CaseDocument unexpectedCaseDocument() {
        return CaseDocument.builder().documentFilename("unexpected").build();
    }

}
