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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_THREE;

@ExtendWith(MockitoExtension.class)
class IntervenerThreePartyListenerTest {

    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private EmailService emailService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private InternationalPostalService internationalPostalService;

    private IntervenerThreePartyListener intervenerThreePartyListener;

    private static final String INTERVENER_THREE_EMAIL = "intervenerThree@solicitor.com";
    private static final String INTERVENER_THREE_NAME = "IntervenerThree Solicitor";
    private static final String INTERVENER_THREE_REF = "REF123";
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
            .data(FinremCaseData.builder().intervenerThree(IntervenerThree
                    .builder()
                    .intervenerSolEmail(INTERVENER_THREE_EMAIL)
                    .intervenerSolName(INTERVENER_THREE_NAME)
                    .intervenerSolicitorReference(INTERVENER_THREE_REF)
                    .build())
                .build())
            .build();
        event = SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails)
            .emailNotificationRequest(NotificationRequest.builder().build())
            .notificationParties(List.of(NotificationParty.INTERVENER_THREE))
            .emailTemplate(EmailTemplateNames.FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR)
            .documentsToPost(List.of(CaseDocument.builder().documentFilename(TEST_DOC_NAME).build()))
            .authToken(AUTH_TOKEN)
            .build();

        intervenerThreePartyListener = new IntervenerThreePartyListener(
            bulkPrintService, emailService, notificationService, internationalPostalService
        );
    }

    @Test
    void shouldNotNotifyWhenNotRelevantParty() {
        SendCorrespondenceEvent otherEvent = SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails)
            .notificationParties(List.of(NotificationParty.APPLICANT))
            .build();
        intervenerThreePartyListener.handleNotification(otherEvent);
        verifyNoInteractions(emailService, bulkPrintService, notificationService, internationalPostalService);
    }

    /**
     * Tests setPartySpecificDetails returns correct details.
     */
    @Test
    void shouldSetPartySpecificDetails() {
        AbstractPartyListener.PartySpecificDetails details = intervenerThreePartyListener.setPartySpecificDetails(event);
        assertThat(details.recipientSolEmailAddress()).isEqualTo(INTERVENER_THREE_EMAIL);
        assertThat(details.recipientSolName()).isEqualTo(INTERVENER_THREE_NAME);
        assertThat(details.recipientSolReference()).isEqualTo(INTERVENER_THREE_REF);
    }

    /**
     * Tests getPartyCoversheet delegates to bulkPrintService.
     */
    @Test
    void shouldGetPartyCoversheet() {
        CaseDocument coverSheet = CaseDocument.builder().documentFilename(COVER_SHEET_FILE).build();
        when(bulkPrintService.getIntervenerThreeCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(coverSheet);
        CaseDocument result = intervenerThreePartyListener.getPartyCoversheet(event);
        assertThat(result).isEqualTo(coverSheet);
        verify(bulkPrintService).getIntervenerThreeCoverSheet(caseDetails, AUTH_TOKEN);
    }

    /**
     * Tests isPartyOutsideUK delegates to internationalPostalService.
     */
    @Test
    void shouldReturnTrueWhenPartyOutsideUK() {
        when(internationalPostalService.isIntervenerResideOutsideOfUK(caseDetails.getData().getIntervenerThree())).thenReturn(true);
        boolean result = intervenerThreePartyListener.isPartyOutsideUK(event);
        assertThat(result).isTrue();
        verify(internationalPostalService).isIntervenerResideOutsideOfUK(caseDetails.getData().getIntervenerThree());
    }

    /**
     * Tests isPartyOutsideUK returns false when intervenerThree is in UK.
     */
    @Test
    void shouldReturnFalseWhenPartyNotOutsideUK() {
        when(internationalPostalService.isIntervenerResideOutsideOfUK(caseDetails.getData().getIntervenerThree())).thenReturn(false);
        boolean result = intervenerThreePartyListener.isPartyOutsideUK(event);
        assertThat(result).isFalse();
    }

    @Test
    void shouldSendDigitalNotificationWhenPartyIsDigitalViaHandleNotification() {
        when(notificationService
            .isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerThree(), caseDetails)).thenReturn(true);

        // Set up event with intervenerThree as notification party
        intervenerThreePartyListener.handleNotification(event);

        // Verify email notification is sent
        assertThat(event.getEmailNotificationRequest().getName()).isEqualTo(INTERVENER_THREE_NAME);
        assertThat(event.getEmailNotificationRequest().getNotificationEmail()).isEqualTo(INTERVENER_THREE_EMAIL);
        assertThat(event.getEmailNotificationRequest().getSolicitorReferenceNumber()).isEqualTo(INTERVENER_THREE_REF);

        verify(notificationService).isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerThree(), caseDetails);
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

        // Cover sheet should be at the beginning of the documents sent for bulk print
        when(notificationService
            .isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerThree(), caseDetails)).thenReturn(false);
        CaseDocument coverSheet = CaseDocument.builder().documentFilename(COVER_SHEET_FILE).build();
        when(bulkPrintService.getIntervenerThreeCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(coverSheet);
        when(bulkPrintService.convertCaseDocumentsToBulkPrintDocuments(List.of(coverSheet, testDocument), AUTH_TOKEN, caseDetails.getCaseType()))
            .thenReturn(List.of(bulkPrintCoverSheet, bulkPrintDocument1));

        intervenerThreePartyListener.handleNotification(event);

        verify(bulkPrintService).getIntervenerThreeCoverSheet(caseDetails, AUTH_TOKEN);
        verify(bulkPrintService).convertCaseDocumentsToBulkPrintDocuments(List.of(coverSheet, testDocument), AUTH_TOKEN, caseDetails.getCaseType());
        verify(bulkPrintService).bulkPrintFinancialRemedyLetterPack(
            caseDetails, INTERVENER_THREE, List.of(bulkPrintCoverSheet, bulkPrintDocument1), false, AUTH_TOKEN
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
    void shouldUseBlankStringsWhenSetPartySpecificDetailsNull(String provided, String expected) {

        caseDetails.getData().getIntervenerThree().setIntervenerSolName(provided);
        caseDetails.getData().getIntervenerThree().setIntervenerSolEmail(provided);
        caseDetails.getData().getIntervenerThree().setIntervenerSolicitorReference(provided);

        AbstractPartyListener.PartySpecificDetails details = intervenerThreePartyListener.setPartySpecificDetails(event);

        assertThat(details.recipientSolName()).isEqualTo(expected);
        assertThat(details.recipientSolEmailAddress()).isEqualTo(expected);
        assertThat(details.recipientSolReference()).isEqualTo(expected);
    }
}
