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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_FOUR;

@ExtendWith(MockitoExtension.class)
class IntervenerFourPartyListenerTest {
    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private EmailService emailService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private InternationalPostalService internationalPostalService;

    private IntervenerFourPartyListener intervenerFourPartyListener;

    private static final String INTERVENER_FOUR_EMAIL = "intervenerFour@solicitor.com";
    private static final String INTERVENER_FOUR_NAME = "IntervenerFour Solicitor";
    private static final String INTERVENER_FOUR_REF = "REF123";
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
            .data(FinremCaseData.builder().intervenerFour(IntervenerFour
                    .builder()
                    .intervenerSolEmail(INTERVENER_FOUR_EMAIL)
                    .intervenerSolName(INTERVENER_FOUR_NAME)
                    .intervenerSolicitorReference(INTERVENER_FOUR_REF)
                    .build())
                .build())
            .build();
        event = SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails)
            .emailNotificationRequest(NotificationRequest.builder().build())
            .notificationParties(List.of(NotificationParty.INTERVENER_FOUR))
            .emailTemplate(EmailTemplateNames.FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR)
            .documentsToPost(List.of(CaseDocument.builder().documentFilename(TEST_DOC_NAME).build()))
            .authToken(AUTH_TOKEN)
            .build();

        intervenerFourPartyListener = new IntervenerFourPartyListener(
            bulkPrintService, emailService, notificationService, internationalPostalService
        );
    }

    @Test
    void shouldNotNotifyWhenNotRelevantParty() {
        SendCorrespondenceEvent otherEvent = SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails)
            .notificationParties(List.of(NotificationParty.APPLICANT))
            .build();
        intervenerFourPartyListener.handleNotification(otherEvent);
        verifyNoInteractions(emailService, bulkPrintService, notificationService, internationalPostalService);
    }

    /**
     * Tests setPartySpecificDetails returns correct details.
     */
    @Test
    void shouldSetPartySpecificDetails() {
        AbstractPartyListener.PartySpecificDetails details = intervenerFourPartyListener.setPartySpecificDetails(event);
        assertThat(details.recipientSolEmailAddress()).isEqualTo(INTERVENER_FOUR_EMAIL);
        assertThat(details.recipientSolName()).isEqualTo(INTERVENER_FOUR_NAME);
        assertThat(details.recipientSolReference()).isEqualTo(INTERVENER_FOUR_REF);
    }

    /**
     * Tests getPartyCoversheet delegates to bulkPrintService.
     */
    @Test
    void shouldGetPartyCoversheet() {
        CaseDocument coverSheet = CaseDocument.builder().documentFilename(COVER_SHEET_FILE).build();
        when(bulkPrintService.getIntervenerFourCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(coverSheet);
        CaseDocument result = intervenerFourPartyListener.getPartyCoversheet(event);
        assertThat(result).isEqualTo(coverSheet);
        verify(bulkPrintService).getIntervenerFourCoverSheet(caseDetails, AUTH_TOKEN);
    }

    /**
     * Tests isPartyOutsideUK delegates to internationalPostalService.
     */
    @Test
    void shouldReturnTrueWhenPartyOutsideUK() {
        when(internationalPostalService
            .isIntervenerResideOutsideOfUK(caseDetails.getData().getIntervenerFour())).thenReturn(true);
        boolean result = intervenerFourPartyListener.isPartyOutsideUK(event);
        assertThat(result).isTrue();
        verify(internationalPostalService).isIntervenerResideOutsideOfUK(caseDetails.getData().getIntervenerFour());
    }

    /**
     * Tests isPartyOutsideUK returns false when intervenerFour is in UK.
     */
    @Test
    void shouldReturnFalseWhenPartyNotOutsideUK() {
        when(internationalPostalService
            .isIntervenerResideOutsideOfUK(caseDetails.getData().getIntervenerFour())).thenReturn(false);
        boolean result = intervenerFourPartyListener.isPartyOutsideUK(event);
        assertThat(result).isFalse();
    }

    @Test
    void shouldSendDigitalNotificationWhenPartyIsDigitalViaHandleNotification() {
        when(notificationService
            .isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerFour(), caseDetails)).thenReturn(true);

        // Set up event with intervenerFour as notification party
        intervenerFourPartyListener.handleNotification(event);

        // Verify email notification is sent
        assertThat(event.getEmailNotificationRequest().getName()).isEqualTo(INTERVENER_FOUR_NAME);
        assertThat(event.getEmailNotificationRequest().getNotificationEmail()).isEqualTo(INTERVENER_FOUR_EMAIL);
        assertThat(event.getEmailNotificationRequest().getSolicitorReferenceNumber()).isEqualTo(INTERVENER_FOUR_REF);

        verify(notificationService).isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerFour(), caseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(event.getEmailTemplate()));
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

        when(notificationService
            .isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerFour(), caseDetails)).thenReturn(false);
        CaseDocument coverSheet = CaseDocument.builder().documentFilename(COVER_SHEET_FILE).build();
        when(bulkPrintService.getIntervenerFourCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(coverSheet);
        when(bulkPrintService.convertCaseDocumentsToBulkPrintDocuments(List.of(testDocument, coverSheet), AUTH_TOKEN, caseDetails.getCaseType()))
            .thenReturn(List.of(bulkPrintDocument1, bulkPrintCoverSheet));

        intervenerFourPartyListener.handleNotification(event);

        verify(bulkPrintService).getIntervenerFourCoverSheet(caseDetails, AUTH_TOKEN);
        verify(bulkPrintService).convertCaseDocumentsToBulkPrintDocuments(List.of(testDocument, coverSheet), AUTH_TOKEN, caseDetails.getCaseType());
        verify(bulkPrintService).bulkPrintFinancialRemedyLetterPack(
            caseDetails, INTERVENER_FOUR, List.of(bulkPrintDocument1, bulkPrintCoverSheet), false, AUTH_TOKEN
        );
    }

    /**
     * Tests that when setPartySpecificDetails used,  null values are replaced with blank strings.
     */
    @ParameterizedTest
    @CsvSource(value = {
        "null,''",
        "'a value', 'a value'"
    }, nullValues = "null")
    void shouldUseBlankStringsWhenPartySpecificDetailsNull(String provided, String expected) {

        caseDetails.getData().getIntervenerFour().setIntervenerSolName(provided);
        caseDetails.getData().getIntervenerFour().setIntervenerSolEmail(provided);
        caseDetails.getData().getIntervenerFour().setIntervenerSolicitorReference(provided);

        AbstractPartyListener.PartySpecificDetails details = intervenerFourPartyListener.setPartySpecificDetails(event);

        assertThat(details.recipientSolName()).isEqualTo(expected);
        assertThat(details.recipientSolEmailAddress()).isEqualTo(expected);
        assertThat(details.recipientSolReference()).isEqualTo(expected);
    }
}
