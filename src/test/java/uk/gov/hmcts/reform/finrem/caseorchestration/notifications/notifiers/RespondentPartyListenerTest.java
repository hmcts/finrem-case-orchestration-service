package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@ExtendWith(MockitoExtension.class)
class RespondentPartyListenerTest {

    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private EmailService emailService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private InternationalPostalService internationalPostalService;

    private RespondentPartyListener respondentPartyListener;

    private static final String RESPONDENT_EMAIL = "respondent@solicitor.com";
    private static final String RESPONDENT_NAME = "Respondent Solicitor";
    private static final String RESPONDENT_REF = "REF123";
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
                .contactDetailsWrapper(
                    ContactDetailsWrapper
                        .builder()
                        .respondentSolicitorName(RESPONDENT_NAME)
                        .respondentSolicitorEmail(RESPONDENT_EMAIL)
                        .respondentSolicitorReference(RESPONDENT_REF)
                        .build()
                ).build()).build();

        event = SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails)
            .emailNotificationRequest(NotificationRequest.builder().build())
            .notificationParties(List.of(NotificationParty.RESPONDENT))
            .emailTemplate(EmailTemplateNames.FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR)
            .documentsToPost(List.of(CaseDocument.builder().documentFilename(TEST_DOC_NAME).build()))
            .authToken(AUTH_TOKEN)
            .build();

        respondentPartyListener = new RespondentPartyListener(
            bulkPrintService, emailService, notificationService, internationalPostalService
        );
    }

    @Test
    void shouldNotNotifyWhenNotRelevantParty() {
        SendCorrespondenceEvent otherEvent = SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails)
            .notificationParties(List.of(NotificationParty.APPLICANT))
            .build();
        respondentPartyListener.handleNotification(otherEvent);
        verifyNoInteractions(emailService, bulkPrintService, notificationService, internationalPostalService);
    }

    /**
     * Tests setPartySpecificDetails returns correct details.
     */
    @Test
    void shouldSetPartySpecificDetails() {
        AbstractPartyListener.PartySpecificDetails details = respondentPartyListener.setPartySpecificDetails(event);
        assertThat(details.recipientSolEmailAddress()).isEqualTo(RESPONDENT_EMAIL);
        assertThat(details.recipientSolName()).isEqualTo(RESPONDENT_NAME);
        assertThat(details.recipientSolReference()).isEqualTo(RESPONDENT_REF);
    }

    /**
     * Tests getPartyCoversheet delegates to bulkPrintService.
     */
    @Test
    void shouldGetPartyCoversheet() {
        CaseDocument coverSheet = CaseDocument.builder().documentFilename(COVER_SHEET_FILE).build();
        when(bulkPrintService.getRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(coverSheet);
        CaseDocument result = respondentPartyListener.getPartyCoversheet(event);
        assertThat(result).isEqualTo(coverSheet);
        verify(bulkPrintService).getRespondentCoverSheet(caseDetails, AUTH_TOKEN);
    }

    /**
     * Tests isPartyOutsideUK delegates to internationalPostalService.
     */
    @Test
    void shouldReturnTrueWhenPartyOutsideUK() {
        when(internationalPostalService.isRespondentResideOutsideOfUK(caseDetails.getData())).thenReturn(true);
        boolean result = respondentPartyListener.isPartyOutsideUK(event);
        assertThat(result).isTrue();
        verify(internationalPostalService).isRespondentResideOutsideOfUK(caseDetails.getData());
    }

    /**
     * Tests isPartyOutsideUK returns false when respondent is in UK.
     */
    @Test
    void shouldReturnFalseWhenPartyNotOutsideUK() {
        when(internationalPostalService.isRespondentResideOutsideOfUK(caseDetails.getData())).thenReturn(false);
        boolean result = respondentPartyListener.isPartyOutsideUK(event);
        assertThat(result).isFalse();
    }

    @Test
    void shouldSendDigitalNotificationWhenPartyIsDigitalViaHandleNotification() {
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        // Set up event with respondent as notification party
        respondentPartyListener.handleNotification(event);

        // Verify email notification is sent
        assertThat(event.getEmailNotificationRequest().getName()).isEqualTo(RESPONDENT_NAME);
        assertThat(event.getEmailNotificationRequest().getNotificationEmail()).isEqualTo(RESPONDENT_EMAIL);
        assertThat(event.getEmailNotificationRequest().getSolicitorReferenceNumber()).isEqualTo(RESPONDENT_REF);

        verify(notificationService).isRespondentSolicitorDigitalAndEmailPopulated(caseDetails);
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

        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        CaseDocument coverSheet = CaseDocument.builder().documentFilename(COVER_SHEET_FILE).build();
        when(bulkPrintService.getRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(coverSheet);
        when(bulkPrintService.convertCaseDocumentsToBulkPrintDocuments(List.of(testDocument, coverSheet), AUTH_TOKEN, caseDetails.getCaseType()))
            .thenReturn(List.of(bulkPrintDocument1, bulkPrintCoverSheet));

        respondentPartyListener.handleNotification(event);

        verify(bulkPrintService).getRespondentCoverSheet(caseDetails, AUTH_TOKEN);
        verify(bulkPrintService).convertCaseDocumentsToBulkPrintDocuments(List.of(testDocument, coverSheet), AUTH_TOKEN, caseDetails.getCaseType());
        verify(bulkPrintService).bulkPrintFinancialRemedyLetterPack(
            caseDetails, RESPONDENT, List.of(bulkPrintDocument1, bulkPrintCoverSheet), false, AUTH_TOKEN
        );
    }
}
