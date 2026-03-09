package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Component
public class FormerIntervenerOneBarristerListener extends FormerIntervenerBarristerListener {

    protected FormerIntervenerOneBarristerListener(BulkPrintService bulkPrintService,
                                                   EmailService emailService,
                                                   NotificationService notificationService,
                                                   InternationalPostalService internationalPostalService) {
        super(IntervenerType.INTERVENER_ONE, bulkPrintService, emailService, notificationService, internationalPostalService);
    }

    @Override
    protected String getNotificationParty() {
        return "former intervener one barrister";
    }

    @Override
    protected boolean isRelevantParty(SendCorrespondenceEvent event) {
        return event.getNotificationParties().contains(NotificationParty.FORMER_INTERVENER_ONE_BARRISTER_ONLY);
    }
}
