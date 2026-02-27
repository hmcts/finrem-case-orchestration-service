package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.Set;

import static java.util.Optional.ofNullable;

@Component
public class FormerIntervenerBarristerListener extends EmailNotificationOnlyListener {

    private static final Set<NotificationParty> FORMER_INTERVENER_BARRISTERS = Set.of(
        NotificationParty.FORMER_INTERVENER_ONE_BARRISTER_ONLY,
        NotificationParty.FORMER_INTERVENER_TWO_BARRISTER_ONLY,
        NotificationParty.FORMER_INTERVENER_THREE_BARRISTER_ONLY,
        NotificationParty.FORMER_INTERVENER_FOUR_BARRISTER_ONLY
    );

    public FormerIntervenerBarristerListener(BulkPrintService bulkPrintService,
                                             EmailService emailService,
                                             NotificationService notificationService,
                                             InternationalPostalService internationalPostalService) {
        super(bulkPrintService, emailService, notificationService, internationalPostalService);
    }

    @Override
    protected String getNotificationParty() {
        return "former intervener barrister";
    }

    @Override
    protected boolean isRelevantParty(SendCorrespondenceEvent event) {
        return event.getNotificationParties().stream()
            .anyMatch(FORMER_INTERVENER_BARRISTERS::contains);
    }

    @Override
    protected boolean shouldSendEmailNotification(SendCorrespondenceEvent event) {
        IntervenerType intervenerType = event.getIntervenerType();
        if (event.getCaseDetailsBefore() != null && intervenerType != null) {
            Barrister barrister = event.getBarrister();
            FinremCaseData caseDataBefore = event.getCaseDetailsBefore().getData();
            return ofNullable(
                    caseDataBefore.getBarristerCollectionWrapper()
                        .getIntervenerBarristersByIndex(intervenerType.getIntervenerId())
                ).orElseThrow(IllegalStateException::new)
                .stream()
                .map(BarristerCollectionItem::getValue)
                .anyMatch(b -> b.equals(barrister));
        }
        return false;
    }
}
