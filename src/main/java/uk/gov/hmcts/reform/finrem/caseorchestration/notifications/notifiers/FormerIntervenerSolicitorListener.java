package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.apache.tika.utils.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.Set;

@Component
public class FormerIntervenerSolicitorListener extends EmailNotificationOnlyListener {

    private static final Set<NotificationParty> FORMER_INTERVENER_SOLICITORS = Set.of(
        NotificationParty.FORMER_INTERVENER_ONE_SOLICITOR_ONLY,
        NotificationParty.FORMER_INTERVENER_TWO_SOLICITOR_ONLY,
        NotificationParty.FORMER_INTERVENER_THREE_SOLICITOR_ONLY,
        NotificationParty.FORMER_INTERVENER_FOUR_SOLICITOR_ONLY
    );

    public FormerIntervenerSolicitorListener(BulkPrintService bulkPrintService,
                                             EmailService emailService,
                                             NotificationService notificationService,
                                             InternationalPostalService internationalPostalService) {
        super(bulkPrintService, emailService, notificationService, internationalPostalService);
    }

    @Override
    protected String getNotificationParty() {
        return "former intervener solicitor";
    }

    @Override
    protected boolean isRelevantParty(SendCorrespondenceEvent event) {
        return event.getNotificationParties().stream()
            .anyMatch(FORMER_INTERVENER_SOLICITORS::contains);
    }

    @Override
    protected boolean shouldSendEmailNotification(SendCorrespondenceEvent event) {
        if (event.getCaseDetailsBefore() != null) {
            FinremCaseData caseData = event.getCaseDetailsBefore().getData();
            IntervenerWrapper intervenerWrapper = caseData.getIntervenerById(event.getIntervenerType().getIntervenerId());
            boolean represented = YesOrNo.isYes(intervenerWrapper.getIntervenerRepresented());
            if (represented) {
                return !StringUtils.isBlank(intervenerWrapper.getIntervenerSolEmail());
            }
        }
        return false;
    }
}
