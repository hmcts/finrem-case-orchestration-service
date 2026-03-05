package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Component
public class FormerIntervenerTwoSolicitorListener extends FormerIntervenerSolicitorListener {

    public FormerIntervenerTwoSolicitorListener(BulkPrintService bulkPrintService,
                                                EmailService emailService,
                                                NotificationService notificationService,
                                                InternationalPostalService internationalPostalService) {
        super(IntervenerType.INTERVENER_TWO, bulkPrintService, emailService, notificationService,
            internationalPostalService);
    }

    @Override
    protected String getNotificationParty() {
        return "former intervener two solicitor";
    }

    @Override
    protected boolean isRelevantParty(SendCorrespondenceEvent event) {
        return event.getNotificationParties().contains(NotificationParty.FORMER_INTERVENER_TWO_SOLICITOR_ONLY);
    }
}
