package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
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

    @InjectMocks
    private ApplicantPartyListener applicantPartyListener;

    private static final String APPLICANT_EMAIL = "applicant@solicitor.com";
    private static final String APPLICANT_NAME = "Applicant Solicitor";
    private static final String APPLICANT_REF = "REF123";
    private static final String COVER_SHEET_FILE = "cover.pdf";
    private static final String TEST_DOC_NAME = "test-document.pdf";
    private FinremCaseDetails caseDetails;
    private SendCorrespondenceEvent event;

    @BeforeEach
    void setUp() {

        caseDetails = FinremCaseDetails.builder()
            .data(FinremCaseData.builder().contactDetailsWrapper(
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
            .documentsToPost(List.of(CaseDocument.builder().documentFilename(TEST_DOC_NAME).build()))
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
     * Tests setPartySpecificDetails null email throws exception.
     */
    @Test
    void shouldThrowExceptionWhenPartySpecificEmailDetailsNull() {

        caseDetails.getData().getContactDetailsWrapper().setApplicantSolicitorEmail(null);

        IllegalArgumentException exception =  assertThrows(IllegalArgumentException.class,
            () -> applicantPartyListener.setPartySpecificDetails(event));

        assertTrue(exception.getMessage().contains("PartySpecificDetails fields must not be null"));
    }

    /**
     * Tests setPartySpecificDetails null sol name throws exception.
     */
    @Test
    void shouldThrowExceptionWhenPartySpecificSolNameDetailsNull() {

        caseDetails.getData().getContactDetailsWrapper().setApplicantSolicitorName(null);

        IllegalArgumentException exception =  assertThrows(IllegalArgumentException.class,
            () -> applicantPartyListener.setPartySpecificDetails(event));

        assertTrue(exception.getMessage().contains("PartySpecificDetails fields must not be null"));
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

        assertThat(exception.getMessage()).isEqualTo("Notification Request is required for digital notifications");
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

        assertThat(exception.getMessage()).isEqualTo("Email template is required for digital notifications");
    }
    
    /**
     * Tests paper notification flow via handleNotification (AbstractPartyListener).
     */
    @Test
    void shouldSendPaperNotificationWhenPartyIsNotDigitalViaHandleNotification() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        CaseDocument coverSheet = CaseDocument.builder().documentFilename(COVER_SHEET_FILE).build();
        when(bulkPrintService.getApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(coverSheet);
        when(bulkPrintService.convertCaseDocumentsToBulkPrintDocuments(anyList()))
            .thenReturn(List.of(BulkPrintDocument.builder().build()));

        applicantPartyListener.handleNotification(event);

        verify(bulkPrintService).getApplicantCoverSheet(caseDetails, AUTH_TOKEN);
        verify(bulkPrintService).convertCaseDocumentsToBulkPrintDocuments(anyList());
        verify(bulkPrintService).bulkPrintFinancialRemedyLetterPack(
            eq(caseDetails), eq(APPLICANT), anyList(), eq(false), eq(AUTH_TOKEN)
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

        assertThat(exception.getMessage()).isEqualTo("No documents to post provided for paper notification");

        verifyNoInteractions(bulkPrintService);
    }
}
