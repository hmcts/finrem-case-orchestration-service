package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@ExtendWith(MockitoExtension.class)
class AbstractPartyListenerTest {

    private static final String RECIPIENT_NAME = "recipientName";
    private static final String RECIPIENT_EMAIL = "recipientEmail";
    private static final String RECIPIENT_REFERENCE = "recipientReference";
    private static final CaseDocument PARTY_COVERSHEET_DOCUMENT = mock(CaseDocument.class);

    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private EmailService emailService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private InternationalPostalService internationalPostalService;

    class RelevantPartyListener extends AbstractPartyListener {

        RelevantPartyListener() {
            super(AbstractPartyListenerTest.this.bulkPrintService,
                AbstractPartyListenerTest.this.emailService,
                AbstractPartyListenerTest.this.notificationService,
                AbstractPartyListenerTest.this.internationalPostalService);
        }

        @Override
        protected boolean isRelevantParty(SendCorrespondenceEvent event) {
            return true;
        }

        @Override
        protected boolean shouldSendEmailNotification(SendCorrespondenceEvent event) {
            return false;
        }

        @Override
        protected PartySpecificDetails setPartySpecificDetails(SendCorrespondenceEvent event) {
            return null;
        }

        @Override
        protected CaseDocument getPartyCoversheet(SendCorrespondenceEvent event) {
            return null;
        }

        @Override
        protected boolean isPartyOutsideUK(SendCorrespondenceEvent event) {
            return false;
        }

        @Override
        protected boolean shouldSendPaperNotification(SendCorrespondenceEvent event) {
            return false;
        }
    }

    class IrrelevantPartyListener extends RelevantPartyListener {

        @Override
        protected boolean isRelevantParty(SendCorrespondenceEvent event) {
            return false;
        }
    }

    class SendEmailNotificationListener extends RelevantPartyListener {

        @Override
        protected boolean shouldSendEmailNotification(SendCorrespondenceEvent event) {
            return true;
        }

        @Override
        protected PartySpecificDetails setPartySpecificDetails(SendCorrespondenceEvent event) {
            return new PartySpecificDetails(RECIPIENT_EMAIL, RECIPIENT_NAME, RECIPIENT_REFERENCE);
        }
    }

    class InvalidSendEmailNotificationListener extends RelevantPartyListener {

        private final int nullField;

        InvalidSendEmailNotificationListener(int nullField) {
            this.nullField = nullField;
        }

        @Override
        protected boolean shouldSendEmailNotification(SendCorrespondenceEvent event) {
            return true;
        }

        @Override
        protected PartySpecificDetails setPartySpecificDetails(SendCorrespondenceEvent event) {
            return new PartySpecificDetails(nullField  == 0 ? null : "a", nullField == 1 ? null : "b'", nullField == 2 ? null : "c");
        }
    }

    class SendPaperNotificationListener extends RelevantPartyListener {

        boolean outsideUK;

        SendPaperNotificationListener(boolean outsideUK) {
            this.outsideUK = outsideUK;
            this.notificationParty = SendPaperNotificationListener.class.getName();
        }

        @Override
        protected boolean shouldSendPaperNotification(SendCorrespondenceEvent event) {
            return true;
        }

        @Override
        protected CaseDocument getPartyCoversheet(SendCorrespondenceEvent event) {
            return PARTY_COVERSHEET_DOCUMENT;
        }

        @Override
        protected boolean isPartyOutsideUK(SendCorrespondenceEvent event) {
            return outsideUK;
        }
    }

    private IrrelevantPartyListener irrelevantPartyListener;

    private SendEmailNotificationListener sendEmailNotificationListener;

    private SendPaperNotificationListener sendPaperNotificationListener;

    private SendPaperNotificationListener sendPaperNotificationOutsideUkListener;

    private InvalidSendEmailNotificationListener[] invalidSendEmailNotificationListeners;

    @BeforeEach
    void setUp() {
        irrelevantPartyListener = new IrrelevantPartyListener();
        sendEmailNotificationListener = new SendEmailNotificationListener();
        invalidSendEmailNotificationListeners = new InvalidSendEmailNotificationListener[] {
            new InvalidSendEmailNotificationListener(0), new InvalidSendEmailNotificationListener(1),
            new InvalidSendEmailNotificationListener(2)
        };
        sendPaperNotificationOutsideUkListener = new SendPaperNotificationListener(true);
        sendPaperNotificationListener = new SendPaperNotificationListener(false);
    }

    @Test
    void givenAnyEvent_whenIrrelevantPartyListenerCalled_thenNoEmailAndLetterSent() {
        irrelevantPartyListener.handleNotification(mock(SendCorrespondenceEvent.class));

        verifyNoEmailSent();
        verifyNoLetterSent();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void givenAnyEvent_whenInvalidSendEmailNotificationListenerCalled_thenExceptionIsThrown(int invalidListenerId) {
        SendCorrespondenceEvent event = spy(SendCorrespondenceEvent.builder()
            .emailTemplate(mock(EmailTemplateNames.class))
            .emailNotificationRequest(mock(NotificationRequest.class))
            .caseDetails(FinremCaseDetails.builder().build())
            .build());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            invalidSendEmailNotificationListeners[invalidListenerId].handleNotification(event));
        assertThat(exception.getMessage()).isEqualTo("PartySpecificDetails fields must not be null");
    }

    @Test
    void givenNotificationRequestMissing_whenSendEmailNotificationListenerCalled_thenExceptionIsThrown() {
        EmailTemplateNames template = mock(EmailTemplateNames.class);
        SendCorrespondenceEvent event = spy(SendCorrespondenceEvent.builder()
            .emailTemplate(template)
            .caseDetails(FinremCaseDetails.builder().build())
            .build());
        when(event.getCaseId()).thenReturn(TEST_CASE_ID);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            sendEmailNotificationListener.handleNotification(event));
        assertThat(exception.getMessage()).isEqualTo("Notification Request is required for digital notifications, case ID: "
            + TEST_CASE_ID);
    }

    @Test
    void givenEmailTemplateNamesMissing_whenSendEmailNotificationListenerCalled_thenSendEmailAndNoLetterSent() {
        SendCorrespondenceEvent event = spy(SendCorrespondenceEvent.builder()
            .emailTemplate(null)
            .emailNotificationRequest(mock(NotificationRequest.class))
            .caseDetails(FinremCaseDetails.builder().build())
            .build());
        when(event.getCaseId()).thenReturn(TEST_CASE_ID);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            sendEmailNotificationListener.handleNotification(event));
        assertThat(exception.getMessage()).isEqualTo("Email template is required for digital notifications, case ID: "
            + TEST_CASE_ID);
    }

    @Test
    void givenNotificationRequestProvided_whenSendEmailNotificationListenerCalled_thenSendEmailAndNoLetterSent() {
        EmailTemplateNames template = mock(EmailTemplateNames.class);
        NotificationRequest nr = spy(NotificationRequest.builder().build());

        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .emailTemplate(template)
            .emailNotificationRequest(nr)
            .build();

        sendEmailNotificationListener.handleNotification(event);

        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(emailService).sendConfirmationEmail(captor.capture(), eq(template));
        verifyNoLetterSent();
        assertThat(captor.getValue())
            .extracting(
                NotificationRequest::getName,
                NotificationRequest::getNotificationEmail,
                NotificationRequest::getSolicitorReferenceNumber)
            .contains(RECIPIENT_NAME, RECIPIENT_EMAIL, RECIPIENT_REFERENCE);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void givenDocumentsToPostMissing_whenSendLetterNotificationListenerCalled_thenExceptionIsThrown(List<CaseDocument> documentsToPost) {
        SendCorrespondenceEvent event = spy(SendCorrespondenceEvent.builder()
            .documentsToPost(documentsToPost)
            .build());
        when(event.getCaseId()).thenReturn(TEST_CASE_ID);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            sendPaperNotificationListener.handleNotification(event));
        assertThat(exception.getMessage()).isEqualTo("No documents to post provided for paper notification, case ID: "
            + TEST_CASE_ID);
    }

    @ParameterizedTest
    @MethodSource
    void givenDocumentsToPostProvided_whenSendLetterNotificationListenerCalled_thenShouldSendToBulkPrintService(
        List<CaseDocument> documentsToPost, boolean isOutsideUK) {

        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        CaseType caseType = mock(CaseType.class);
        when(caseDetails.getCaseType()).thenReturn(caseType);
        SendCorrespondenceEvent event = spy(SendCorrespondenceEvent.builder()
            .authToken(AUTH_TOKEN)
            .caseDetails(caseDetails)
            .documentsToPost(documentsToPost)
            .build());
        when(event.getCaseDetails()).thenReturn(caseDetails);
        when(event.getCaseId()).thenReturn(TEST_CASE_ID);

        List<CaseDocument> expectedDocuments = new ArrayList<>(documentsToPost);
        expectedDocuments.add(PARTY_COVERSHEET_DOCUMENT);

        List<BulkPrintDocument> bpDocs = mock(List.class);
        when(bulkPrintService.convertCaseDocumentsToBulkPrintDocuments(expectedDocuments, AUTH_TOKEN, caseType))
            .thenReturn(bpDocs);

        // act
        if (isOutsideUK) {
            sendPaperNotificationOutsideUkListener.handleNotification(event);
        } else {
            sendPaperNotificationListener.handleNotification(event);
        }
        // assert
        verify(bulkPrintService).convertCaseDocumentsToBulkPrintDocuments(expectedDocuments, AUTH_TOKEN, caseType);
        verify(bulkPrintService).bulkPrintFinancialRemedyLetterPack(caseDetails, SendPaperNotificationListener.class.getName(),
            bpDocs, isOutsideUK, AUTH_TOKEN);
    }

    private static Stream<Arguments> givenDocumentsToPostProvided_whenSendLetterNotificationListenerCalled_thenShouldSendToBulkPrintService() {
        return Stream.of(
            Arguments.of(List.of(caseDocument("file1")), true),
            Arguments.of(List.of(caseDocument("file1")), false),
            Arguments.of(List.of(caseDocument("file1"), caseDocument("file2")), false)
        );
    }

    private void verifyNoEmailSent() {
        verifyNoInteractions(emailService);
    }

    private void verifyNoLetterSent() {
        verifyNoInteractions(bulkPrintService, internationalPostalService);
    }
}
