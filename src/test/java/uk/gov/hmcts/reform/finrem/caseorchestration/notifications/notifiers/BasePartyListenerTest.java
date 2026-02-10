package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;

@ExtendWith(MockitoExtension.class)
abstract class BasePartyListenerTest {

    @Mock
    protected BulkPrintService bulkPrintService;
    @Mock
    protected EmailService emailService;
    @Mock
    protected NotificationService notificationService;
    @Mock
    protected InternationalPostalService internationalPostalService;

    protected NotificationParty notificationParty;

    BasePartyListenerTest(NotificationParty notificationParty) {
        this.notificationParty = notificationParty;
    }

    protected static NotificationRequest emailNotificationRequest(String solicitorReferenceNumber) {
        return NotificationRequest.builder()
            .notificationEmail(TEST_SOLICITOR_EMAIL)
            .name(TEST_SOLICITOR_NAME)
            .solicitorReferenceNumber(solicitorReferenceNumber)
            .build();
    }

    protected SendCorrespondenceEvent sendCorrespondenceEventWithTargetNotificationParty(
        FinremCaseDetails caseDetailsBefore, EmailTemplateNames emailTemplate) {

        return sendCorrespondenceEventWithTargetNotificationParty(caseDetailsBefore, emailTemplate, null);
    }

    protected SendCorrespondenceEvent sendCorrespondenceEventWithTargetNotificationParty(
        FinremCaseDetails caseDetailsBefore, EmailTemplateNames emailTemplate, String solicitorReferenceNumber) {

        return SendCorrespondenceEvent.builder()
            .caseDetails(FinremCaseDetails.builder().data(FinremCaseData.builder().build()).build())
            .caseDetailsBefore(caseDetailsBefore)
            .notificationParties(List.of(notificationParty))
            .emailNotificationRequest(emailNotificationRequest(solicitorReferenceNumber))
            .emailTemplate(emailTemplate)
            .build();
    }

    protected void verifyNoLetterSent() {
        verifyNoInteractions(bulkPrintService, internationalPostalService);
    }
}
