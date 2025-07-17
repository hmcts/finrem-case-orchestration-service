package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hwf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HelpWithFeesDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;

@ExtendWith(MockitoExtension.class)
class HwfConsentedApplicantCorresponderTest {

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private HelpWithFeesDocumentService helpWithFessDocumentService;

    @InjectMocks
    private HwfConsentedApplicantCorresponder underTest;

    private CaseDocument expectedCaseDocument;

    @BeforeEach
    void setUp() {
        expectedCaseDocument = CaseDocument.builder().build();
        lenient().when(helpWithFessDocumentService.generateHwfSuccessfulNotificationLetter(any(FinremCaseDetails.class), eq(AUTH_TOKEN),
            eq(DocumentHelper.PaperNotificationRecipient.APPLICANT))).thenReturn(
            expectedCaseDocument);
    }

    @Test
    void shouldGetDocumentToPrint() {
        // Arrange
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().build();

        // Act
        CaseDocument result = underTest.getDocumentToPrint(caseDetails, AUTH_TOKEN);

        // Assert
        assertThat(result).isEqualTo(expectedCaseDocument);
    }

    @Test
    void shouldSendLetterToApplicant() {
        // Arrange
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().data(FinremCaseData.builder().build()).build();
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);

        // Act
        underTest.sendCorrespondence(caseDetails, AUTH_TOKEN);

        // Assert
        verify(bulkPrintService).sendDocumentForPrint(expectedCaseDocument, caseDetails, APPLICANT, AUTH_TOKEN);
    }

    @Test
    void shouldSendEmailToApplicant() {
        // Arrange
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().data(FinremCaseData.builder().build()).build();
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        // Act
        underTest.sendCorrespondence(caseDetails, AUTH_TOKEN);

        // Assert
        verify(notificationService).sendConsentedHWFSuccessfulConfirmationEmail(caseDetails);
    }
}
