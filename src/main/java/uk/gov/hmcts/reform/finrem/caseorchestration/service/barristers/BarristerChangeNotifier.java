package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChangeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerLetterTuple;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.adapters.BarristerLetterServiceAdapter;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChangeType.ADDED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChangeType.REMOVED;

@Service
@RequiredArgsConstructor
public class BarristerChangeNotifier {

    private final NotificationService notificationService;
    private final BarristerLetterServiceAdapter barristerLetterService;

    public record NotifierRequest(FinremCaseDetails caseDetails, String authToken, BarristerChange barristerChange) {}

    /**
     * Notify relevant parties of barrister changes and send letters where necessary.
     *
     * @param notifierRequest the notifier request containing case details, auth token, and barrister changes
     */
    public void notify(NotifierRequest notifierRequest) {
        BarristerChange barristerChange = notifierRequest.barristerChange();
        barristerChange.getAdded().forEach(barrister -> sendNotifications(notifierRequest, barrister, ADDED));
        barristerChange.getRemoved().forEach(barrister ->  sendNotifications(notifierRequest, barrister, REMOVED));
    }

    private void sendNotifications(NotifierRequest notifierRequest,
                                   Barrister barrister,
                                   BarristerChangeType barristerChangeType) {
        if (ADDED.equals(barristerChangeType)) {
            notificationService.sendBarristerAddedEmail(notifierRequest.caseDetails, barrister);
        } else {
            notificationService.sendBarristerRemovedEmail(notifierRequest.caseDetails, barrister);
        }

        DocumentHelper.PaperNotificationRecipient recipient =
            getPaperNotificationRecipient(notifierRequest.barristerChange.getBarristerParty());
        if (recipient == DocumentHelper.PaperNotificationRecipient.APPLICANT
            || recipient == DocumentHelper.PaperNotificationRecipient.RESPONDENT) {
            barristerLetterService.sendBarristerLetter(notifierRequest.caseDetails, barrister,
                BarristerLetterTuple.of(recipient, notifierRequest.authToken, barristerChangeType),
                notifierRequest.authToken);
        }
    }

    private DocumentHelper.PaperNotificationRecipient getPaperNotificationRecipient(BarristerParty barristerParty) {
        return switch (barristerParty) {
            case APPLICANT -> DocumentHelper.PaperNotificationRecipient.APPLICANT;
            case RESPONDENT -> DocumentHelper.PaperNotificationRecipient.RESPONDENT;
            case INTERVENER1 -> DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE;
            case INTERVENER2 -> DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO;
            case INTERVENER3 -> DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE;
            case INTERVENER4 -> DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR;
        };
    }
}
