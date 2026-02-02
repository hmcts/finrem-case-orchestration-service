package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;

@ExtendWith(MockitoExtension.class)
class ApplicantPartyListenerTest {

    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private EmailService emailService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private InternationalPostalService internationalPostalService;

    private ApplicantPartyListener applicantPartyListener;

    private static final String APPLICANT_EMAIL = "applicant@solicitor.com";
    private static final String APPLICANT_NAME = "Applicant Solicitor";
    private static final String APPLICANT_REF = "REF123";
    private static final String COVER_SHEET_FILE = "cover.pdf";
    private static final String TEST_DOC_NAME = "test-document.pdf";
    private CaseDocument testDocument;
    private FinremCaseDetails caseDetails;
    private SendCorrespondenceEvent event;

    @BeforeEach
    void setUp() {

        testDocument = CaseDocument.builder()
            .documentFilename(TEST_DOC_NAME)
            .build();

        caseDetails = FinremCaseDetails.builder()
            .caseType(CaseType.CONTESTED)
            .data(FinremCaseData.builder()
                .ccdCaseId(CASE_ID)
                .contactDetailsWrapper(
                    ContactDetailsWrapper
                        .builder()
                        .applicantSolicitorName(APPLICANT_NAME)
                        .applicantSolicitorEmail(APPLICANT_EMAIL)
                        .solicitorReference(APPLICANT_REF)
                        .build()
                ).build()).build();

        event = SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails)
            .emailNotificationRequest(NotificationRequest.builder().build())
            .notificationParties(List.of(NotificationParty.APPLICANT))
            .emailTemplate(EmailTemplateNames.FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR)
            .documentsToPost(List.of(testDocument))
            .authToken(AUTH_TOKEN)
            .build();

        applicantPartyListener = new ApplicantPartyListener(
            bulkPrintService, emailService, notificationService, internationalPostalService
        );
    }

    @Test
    void shouldNotNotifyWhenNotRelevantParty() {
        SendCorrespondenceEvent otherEvent = SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails)
            .notificationParties(List.of(NotificationParty.RESPONDENT))
            .build();
        applicantPartyListener.handleNotification(otherEvent);
        verifyNoInteractions(emailService, bulkPrintService, notificationService, internationalPostalService);
    }

    /**
     * Tests setPartySpecificDetails returns correct details.
     */
    @Test
    void shouldSetPartySpecificDetails() {
        AbstractPartyListener.PartySpecificDetails details = applicantPartyListener.setPartySpecificDetails(event);
        assertThat(details.recipientSolEmailAddress()).isEqualTo(APPLICANT_EMAIL);
        assertThat(details.recipientSolName()).isEqualTo(APPLICANT_NAME);
        assertThat(details.recipientSolReference()).isEqualTo(APPLICANT_REF);
    }

    /**
     * Tests that when setPartySpecificDetails used,  null values are replaced with blank strings.
     */
    @ParameterizedTest
    @CsvSource(value = {
        "null,''",
        "'a value', 'a value'"
    }, nullValues = "null")
    void shouldUseBlankStringsWhenSetPartySpecificDetailsNull(String provided, String expected) {

        caseDetails.getData().getContactDetailsWrapper().setApplicantSolicitorName(provided);
        caseDetails.getData().getContactDetailsWrapper().setApplicantSolicitorEmail(provided);
        caseDetails.getData().getContactDetailsWrapper().setSolicitorReference(provided);

        AbstractPartyListener.PartySpecificDetails details = applicantPartyListener.setPartySpecificDetails(event);

        assertThat(details.recipientSolName()).isEqualTo(expected);
        assertThat(details.recipientSolEmailAddress()).isEqualTo(expected);
        assertThat(details.recipientSolReference()).isEqualTo(expected);
    }

    /**
     * Tests getPartyCoversheet delegates to bulkPrintService.
     */
    @Test
    void shouldGetPartyCoversheet() {
        CaseDocument coverSheet = CaseDocument.builder().documentFilename(COVER_SHEET_FILE).build();
        when(bulkPrintService.getApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(coverSheet);
        CaseDocument result = applicantPartyListener.getPartyCoversheet(event);
        assertThat(result).isEqualTo(coverSheet);
        verify(bulkPrintService).getApplicantCoverSheet(caseDetails, AUTH_TOKEN);
    }

    /**
     * Tests isPartyOutsideUK delegates to internationalPostalService.
     */
    @Test
    void shouldReturnTrueWhenPartyOutsideUK() {
        when(internationalPostalService.isApplicantResideOutsideOfUK(caseDetails.getData())).thenReturn(true);
        boolean result = applicantPartyListener.isPartyOutsideUK(event);
        assertThat(result).isTrue();
        verify(internationalPostalService).isApplicantResideOutsideOfUK(caseDetails.getData());
    }

    /**
     * Tests isPartyOutsideUK returns false when applicant is in UK.
     */
    @Test
    void shouldReturnFalseWhenPartyNotOutsideUK() {
        when(internationalPostalService.isApplicantResideOutsideOfUK(caseDetails.getData())).thenReturn(false);
        boolean result = applicantPartyListener.isPartyOutsideUK(event);
        assertThat(result).isFalse();
    }

    @Test
    void shouldSendDigitalNotificationWhenPartyIsDigitalViaHandleNotification() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        // Set up event with applicant as notification party
        applicantPartyListener.handleNotification(event);

        // Verify email notification is sent
        assertThat(event.getEmailNotificationRequest().getName()).isEqualTo(APPLICANT_NAME);
        assertThat(event.getEmailNotificationRequest().getNotificationEmail()).isEqualTo(APPLICANT_EMAIL);
        assertThat(event.getEmailNotificationRequest().getSolicitorReferenceNumber()).isEqualTo(APPLICANT_REF);

        verify(notificationService).isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(event.getEmailTemplate()));
    }

    @Test
    void shouldThrowIllegalArgumentWhenNotificationRequestIsNullOnSendDigitalNotification() {

        SendCorrespondenceEvent newEvent = SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails)
            .emailNotificationRequest(null)
            .notificationParties(List.of(NotificationParty.APPLICANT))
            .emailTemplate(EmailTemplateNames.FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR)
            .documentsToPost(List.of())
            .authToken(AUTH_TOKEN)
            .build();

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> applicantPartyListener.handleNotification(newEvent));

        verify(notificationService).isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
        verifyNoInteractions(emailService);

        assertThat(exception.getMessage()).isEqualTo("Notification Request is required for digital notifications, case ID: " + CASE_ID);
    }

    @Test
    void shouldThrowIllegalArgumentWhenEmailTemplateIsNullOnSendDigitalNotification() {

        SendCorrespondenceEvent newEvent = SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails)
            .emailNotificationRequest(NotificationRequest.builder().build())
            .notificationParties(List.of(NotificationParty.APPLICANT))
            .emailTemplate(null)
            .documentsToPost(List.of())
            .authToken(AUTH_TOKEN)
            .build();

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> applicantPartyListener.handleNotification(newEvent));

        verify(notificationService).isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
        verifyNoInteractions(emailService);

        assertThat(exception.getMessage()).isEqualTo("Email template is required for digital notifications, case ID: " + CASE_ID);
    }

    /**
     * Tests paper notification flow via handleNotification (AbstractPartyListener).
     */
    @Test
    void shouldSendPaperNotificationWhenPartyIsNotDigitalViaHandleNotification() {
        BulkPrintDocument bulkPrintDocument1 = BulkPrintDocument
            .builder()
            .fileName(TEST_DOC_NAME)
            .build();

        BulkPrintDocument bulkPrintCoverSheet = BulkPrintDocument
            .builder()
            .fileName(COVER_SHEET_FILE)
            .build();

        // Cover sheet should be at the begging of the documents sent for bulk print
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        CaseDocument coverSheet = CaseDocument.builder().documentFilename(COVER_SHEET_FILE).build();
        when(bulkPrintService.getApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(coverSheet);
        when(bulkPrintService.convertCaseDocumentsToBulkPrintDocuments(List.of(coverSheet, testDocument), AUTH_TOKEN, caseDetails.getCaseType()))
            .thenReturn(List.of(bulkPrintCoverSheet, bulkPrintDocument1));

        applicantPartyListener.handleNotification(event);

        verify(bulkPrintService).getApplicantCoverSheet(caseDetails, AUTH_TOKEN);
        verify(bulkPrintService).convertCaseDocumentsToBulkPrintDocuments(List.of(coverSheet, testDocument), AUTH_TOKEN, caseDetails.getCaseType());
        verify(bulkPrintService).bulkPrintFinancialRemedyLetterPack(
            caseDetails, APPLICANT, List.of(bulkPrintCoverSheet, bulkPrintDocument1), false, AUTH_TOKEN
        );
    }

    /**
     * Tests paper notification flow via handleNotification (AbstractPartyListener).
     */
    @Test
    void shouldThrowIllegalArgWhenSendPaperNotificationWithNoDocs() {

        SendCorrespondenceEvent newEvent = SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails)
            .emailNotificationRequest(NotificationRequest.builder().build())
            .notificationParties(List.of(NotificationParty.APPLICANT))
            .emailTemplate(EmailTemplateNames.FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR)
            .documentsToPost(List.of())
            .authToken(AUTH_TOKEN)
            .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> applicantPartyListener.handleNotification(newEvent));

        assertThat(exception.getMessage()).isEqualTo("No documents to post provided for paper notification, case ID: " + CASE_ID);

        verifyNoInteractions(bulkPrintService);
    }
}
