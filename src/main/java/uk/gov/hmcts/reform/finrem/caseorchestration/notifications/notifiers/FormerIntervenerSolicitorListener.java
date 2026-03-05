package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.apache.tika.utils.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

public abstract class FormerIntervenerSolicitorListener extends EmailNotificationOnlyListener {

    protected IntervenerType intervenerType;

    protected FormerIntervenerSolicitorListener(IntervenerType intervenerType,
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
            FinremCaseData caseData = event.getCaseDetailsBefore().getData();

            IntervenerWrapper intervenerWrapper = caseData.getIntervenerById(intervenerType.getIntervenerId());
            boolean represented = YesOrNo.isYes(intervenerWrapper.getIntervenerRepresented());
            if (represented) {
                return !StringUtils.isBlank(intervenerWrapper.getIntervenerSolEmail());
            }
        }
        return false;
    }
}
