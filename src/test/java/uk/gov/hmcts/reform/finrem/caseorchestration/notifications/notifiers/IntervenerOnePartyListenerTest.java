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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE;

@ExtendWith(MockitoExtension.class)
class IntervenerOnePartyListenerTest {
    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private EmailService emailService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private InternationalPostalService internationalPostalService;

    @InjectMocks
    private IntervenerOnePartyListener intervenerOnePartyListener;

    private static final String INTERVENER_ONE_EMAIL = "intervenerOne@solicitor.com";
    private static final String INTERVENER_ONE_NAME = "IntervenerOne Solicitor";
    private static final String INTERVENER_ONE_REF = "REF123";
    private static final String COVER_SHEET_FILE = "cover.pdf";
    private static final String TEST_DOC_NAME = "test-document.pdf";
    private FinremCaseDetails caseDetails;
    private SendCorrespondenceEvent event;

    @BeforeEach
    void setUp() {

        caseDetails = FinremCaseDetails.builder()
            .data(FinremCaseData.builder().intervenerOne(IntervenerOne
                    .builder()
                    .intervenerSolEmail(INTERVENER_ONE_EMAIL)
                    .intervenerSolName(INTERVENER_ONE_NAME)
                    .intervenerSolicitorReference(INTERVENER_ONE_REF)
                    .build())
                .build())
            .build();
        event = SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails)
            .emailNotificationRequest(NotificationRequest.builder().build())
            .notificationParties(List.of(NotificationParty.INTERVENER_ONE))
            .emailTemplateId(EmailTemplateNames.FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR)
            .documentsToPost(List.of(CaseDocument.builder().documentFilename(TEST_DOC_NAME).build()))
            .authToken(AUTH_TOKEN)
            .build();

        intervenerOnePartyListener = new IntervenerOnePartyListener(
            bulkPrintService, emailService, notificationService, internationalPostalService
        );
    }

    @Test
    void shouldNotNotifyWhenNotRelevantParty() {
        SendCorrespondenceEvent otherEvent = SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails)
            .notificationParties(List.of(NotificationParty.APPLICANT))
            .build();
        intervenerOnePartyListener.handleNotification(otherEvent);
        verifyNoInteractions(emailService, bulkPrintService, notificationService, internationalPostalService);
    }

    /**
     * Tests setPartySpecificDetails returns correct details.
     */
    @Test
    void shouldSetPartySpecificDetails() {
        AbstractPartyListener.PartySpecificDetails details = intervenerOnePartyListener.setPartySpecificDetails(event);
        assertThat(details.recipientSolEmailAddress()).isEqualTo(INTERVENER_ONE_EMAIL);
        assertThat(details.recipientSolName()).isEqualTo(INTERVENER_ONE_NAME);
        assertThat(details.recipientSolReference()).isEqualTo(INTERVENER_ONE_REF);
    }

    /**
     * Tests getPartyCoversheet delegates to bulkPrintService.
     */
    @Test
    void shouldGetPartyCoversheet() {
        CaseDocument coverSheet = CaseDocument.builder().documentFilename(COVER_SHEET_FILE).build();
        when(bulkPrintService.getIntervenerOneCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(coverSheet);
        CaseDocument result = intervenerOnePartyListener.getPartyCoversheet(event);
        assertThat(result).isEqualTo(coverSheet);
        verify(bulkPrintService).getIntervenerOneCoverSheet(caseDetails, AUTH_TOKEN);
    }

    /**
     * Tests sendLetter delegates to bulkPrintService.
     */
    @Test
    void shouldSendLetter() {
        List<BulkPrintDocument> docs = List.of(BulkPrintDocument.builder().build());
        intervenerOnePartyListener.sendLetter(event, docs, true);
        verify(bulkPrintService).bulkPrintFinancialRemedyLetterPack(
            caseDetails, INTERVENER_ONE, docs, true, AUTH_TOKEN
        );
    }

    /**
     * Tests isPartyOutsideUK delegates to internationalPostalService.
     */
    @Test
    void shouldReturnTrueWhenPartyOutsideUK() {
        when(internationalPostalService.isIntervenerResideOutsideOfUK(caseDetails.getData().getIntervenerOne())).thenReturn(true);
        boolean result = intervenerOnePartyListener.isPartyOutsideUK(event);
        assertThat(result).isTrue();
        verify(internationalPostalService).isIntervenerResideOutsideOfUK(caseDetails.getData().getIntervenerOne());
    }

    /**
     * Tests isPartyOutsideUK returns false when intervenerOne is in UK.
     */
    @Test
    void shouldReturnFalseWhenPartyNotOutsideUK() {
        when(internationalPostalService.isIntervenerResideOutsideOfUK(caseDetails.getData().getIntervenerOne())).thenReturn(false);
        boolean result = intervenerOnePartyListener.isPartyOutsideUK(event);
        assertThat(result).isFalse();
    }

    @Test
    void shouldSendDigitalNotificationWhenPartyIsDigitalViaHandleNotification() {
        when(notificationService
            .isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerOne(), caseDetails)).thenReturn(true);

        // Set up event with intervenerOne as notification party
        intervenerOnePartyListener.handleNotification(event);

        // Verify email notification is sent
        assertThat(event.getEmailNotificationRequest().getName()).isEqualTo(INTERVENER_ONE_NAME);
        assertThat(event.getEmailNotificationRequest().getNotificationEmail()).isEqualTo(INTERVENER_ONE_EMAIL);
        assertThat(event.getEmailNotificationRequest().getSolicitorReferenceNumber()).isEqualTo(INTERVENER_ONE_REF);

        verify(notificationService).isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerOne(), caseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(event.getEmailTemplateId()));
    }

    /**
     * Tests paper notification flow via handleNotification (AbstractPartyListener).
     */
    @Test
    void shouldSendPaperNotificationWhenPartyIsNotDigitalViaHandleNotification() {
        when(notificationService
            .isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails.getData().getIntervenerOne(), caseDetails)).thenReturn(false);
        CaseDocument coverSheet = CaseDocument.builder().documentFilename(COVER_SHEET_FILE).build();
        when(bulkPrintService.getIntervenerOneCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(coverSheet);
        when(bulkPrintService.convertCaseDocumentsToBulkPrintDocuments(anyList()))
            .thenReturn(List.of(BulkPrintDocument.builder().build()));

        intervenerOnePartyListener.handleNotification(event);

        verify(bulkPrintService).getIntervenerOneCoverSheet(caseDetails, AUTH_TOKEN);
        verify(bulkPrintService).convertCaseDocumentsToBulkPrintDocuments(anyList());
        verify(bulkPrintService).bulkPrintFinancialRemedyLetterPack(
            eq(caseDetails), eq(INTERVENER_ONE), anyList(), eq(false), eq(AUTH_TOKEN)
        );
    }
}
