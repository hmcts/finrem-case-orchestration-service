package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static java.util.Optional.ofNullable;

@Component
public class FormerRespondentBarristerListener extends EmailNotificationOnlyListener {

    public FormerRespondentBarristerListener(BulkPrintService bulkPrintService,
                                             EmailService emailService,
                                             NotificationService notificationService,
                                             InternationalPostalService internationalPostalService) {
        super(bulkPrintService, emailService, notificationService, internationalPostalService);
    }

    @Override
    protected String getNotificationParty() {
        return "former respondent barrister";
    }

    @Override
    protected boolean isRelevantParty(SendCorrespondenceEvent event) {
        return event.getNotificationParties().contains(NotificationParty.FORMER_RESPONDENT_BARRISTER_ONLY);
    }

    @Override
    protected boolean shouldSendEmailNotification(SendCorrespondenceEvent event) {
        if (event.getCaseDetailsBefore() != null) {
            Barrister barrister = event.getBarrister(); // target barrister is set in the event
            FinremCaseData caseDataBefore = event.getCaseDetailsBefore().getData();
            return ofNullable(caseDataBefore.getBarristerCollectionWrapper()
                .getRespondentBarristers()
            ).orElseThrow(IllegalStateException::new)
                .stream()
                .map(BarristerCollectionItem::getValue)
                .anyMatch(b -> b.equals(barrister));
        }
        return false;
    }
}
