package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static java.util.Optional.ofNullable;

public abstract class FormerIntervenerBarristerListener extends EmailNotificationOnlyListener {

    protected IntervenerType intervenerType;

    protected FormerIntervenerBarristerListener(IntervenerType intervenerType,
                                                BulkPrintService bulkPrintService,
                                                EmailService emailService,
                                                NotificationService notificationService,
                                                InternationalPostalService internationalPostalService) {
        super(bulkPrintService, emailService, notificationService, internationalPostalService);
        this.intervenerType = intervenerType;
    }

    @Override
    protected boolean shouldSendEmailNotification(SendCorrespondenceEvent event) {
        if (event.getCaseDetailsBefore() != null) {
            Barrister barrister = event.getBarrister(); // target barrister is set in the event
            FinremCaseData caseDataBefore = event.getCaseDetailsBefore().getData();
            return ofNullable(caseDataBefore.getBarristerCollectionWrapper()
                .getIntervenerBarristersByIndex(intervenerType.getIntervenerId())
            ).orElseThrow(IllegalStateException::new)
                .stream()
                .map(BarristerCollectionItem::getValue)
                .anyMatch(b -> b.equals(barrister));
        }
        return false;
    }
}
