package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static com.google.common.base.Strings.nullToEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;

@Component
public class IntervenerThreePartyListener extends AbstractPartyListener {

    public IntervenerThreePartyListener(BulkPrintService bulkPrintService,
                                        EmailService emailService,
                                        NotificationService notificationService,
                                        InternationalPostalService internationalPostalService) {
        super(bulkPrintService, emailService, notificationService, internationalPostalService);
    }

    @Override
    protected String getNotificationParty() {
        return IntervenerConstant.INTERVENER_THREE;
    }

    @Override
    protected boolean isRelevantParty(SendCorrespondenceEvent event) {
        return event.getNotificationParties().contains(NotificationParty.INTERVENER_THREE);
    }

    @Override
    protected boolean shouldSendEmailNotification(SendCorrespondenceEvent event) {
        return notificationService
            .isIntervenerSolicitorDigitalAndEmailPopulated(event.getCaseData().getIntervenerThree(), event.getCaseDetails());
    }

    @Override
    protected PartySpecificDetails setPartySpecificDetails(SendCorrespondenceEvent event) {
        IntervenerWrapper intervenerThree = event.getCaseData().getIntervenerThree();
        String email = intervenerThree.getIntervenerSolEmail();
        String name = intervenerThree.getIntervenerSolName();
        String ref = nullToEmpty(intervenerThree.getIntervenerSolicitorReference());
        return new PartySpecificDetails(email, name, ref);
    }

    @Override
    protected CaseDocument getPartyCoversheet(SendCorrespondenceEvent event) {
        return bulkPrintService.getIntervenerThreeCoverSheet(event.getCaseDetails(), event.authToken);
    }

    @Override
    protected boolean isPartyOutsideUK(SendCorrespondenceEvent event) {
        return internationalPostalService.isIntervenerResideOutsideOfUK(event.getCaseData().getIntervenerThree());
    }
}
