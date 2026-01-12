package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;

import static com.google.common.base.Strings.nullToEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_TWO;

@Component
public class IntervenerTwoPartyListener extends AbstractPartyListener {

    public IntervenerTwoPartyListener(BulkPrintService bulkPrintService,
                                      EmailService emailService,
                                      NotificationService notificationService,
                                      InternationalPostalService internationalPostalService) {
        super(bulkPrintService, emailService, notificationService, internationalPostalService);
    }

    @Override
    protected boolean isRelevantParty(SendCorrespondenceEvent event) {
        return event.getNotificationParties().contains(NotificationParty.INTERVENER_TWO);
    }

    @Override
    protected boolean isDigitalParty(SendCorrespondenceEvent event) {
        return notificationService
            .isIntervenerSolicitorDigitalAndEmailPopulated(event.getCaseDetails().getData().getIntervenerTwo(), event.getCaseDetails());
    }

    @Override
    protected PartySpecificDetails setPartySpecificDetails(SendCorrespondenceEvent event) {
        IntervenerWrapper intervenerTwo = event.getCaseDetails().getData().getIntervenerTwo();
        String email = intervenerTwo.getIntervenerSolEmail();
        String name = intervenerTwo.getIntervenerSolName();
        String ref = nullToEmpty(intervenerTwo.getIntervenerSolicitorReference());
        return new PartySpecificDetails(email, name, ref);
    }

    @Override
    protected CaseDocument getPartyCoversheet(SendCorrespondenceEvent event) {
        return bulkPrintService.getIntervenerTwoCoverSheet(event.getCaseDetails(), event.authToken);
    }

    @Override
    protected void sendLetter(SendCorrespondenceEvent event,
                              List<BulkPrintDocument> bulkPrintDocs,
                              boolean isOutsideUK) {
        bulkPrintService.bulkPrintFinancialRemedyLetterPack(
            event.caseDetails, INTERVENER_TWO, bulkPrintDocs, isOutsideUK, event.authToken
        );
    }

    @Override
    protected boolean isPartyOutsideUK(SendCorrespondenceEvent event) {
        return internationalPostalService.isIntervenerResideOutsideOfUK(event.getCaseDetails().getData().getIntervenerTwo());
    }
}
