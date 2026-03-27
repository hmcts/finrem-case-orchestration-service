package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static com.google.common.base.Strings.nullToEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

/**
 * Listener responsible for handling correspondence notifications for the respondent.
 *
 * <p>This implementation extends {@link AbstractPartyListener} and provides
 * respondent-specific logic to:
 *
 * <ul>
 *     <li>Identify when the respondent is the intended notification party</li>
 *     <li>Determine if email notification criteria are satisfied</li>
 *     <li>Populate respondent-specific contact details for notifications</li>
 *     <li>Retrieve the appropriate coversheet for bulk print</li>
 *     <li>Determine whether the respondent resides outside the UK for postal handling</li>
 * </ul>
 *
 * <p>Email notifications are sent only when the respondent's solicitor is marked as
 * digital and a valid email address is available in the case details.
 */
@Component
public class RespondentPartyListener extends AbstractPartyListener {

    public RespondentPartyListener(BulkPrintService bulkPrintService,
                                   EmailService emailService,
                                   NotificationService notificationService,
                                   InternationalPostalService internationalPostalService) {
        super(bulkPrintService, emailService, notificationService, internationalPostalService);
    }

    @Override
    protected String getNotificationParty() {
        return RESPONDENT;
    }

    @Override
    protected boolean isRelevantParty(SendCorrespondenceEvent event) {
        return event.getNotificationParties().contains(NotificationParty.RESPONDENT);
    }

    @Override
    protected boolean shouldSendEmailNotification(SendCorrespondenceEvent event) {
        return notificationService.isRespondentSolicitorDigitalAndEmailPopulated(event.getCaseDetails());
    }

    @Override
    protected PartySpecificDetails setPartySpecificDetails(SendCorrespondenceEvent event) {
        FinremCaseDetails caseDetails = event.getCaseDetails();
        String email = caseDetails.getRespSolicitorEmail();
        String name = caseDetails.getRespSolicitorName();
        String ref = nullToEmpty(caseDetails.getRespSolicitorRef());
        return new PartySpecificDetails(email, name, ref);
    }

    @Override
    protected CaseDocument getPartyCoversheet(SendCorrespondenceEvent event) {
        return bulkPrintService.getRespondentCoverSheet(event.getCaseDetails(), event.authToken);
    }

    @Override
    protected boolean isPartyOutsideUK(SendCorrespondenceEvent event) {
        return internationalPostalService.isRespondentResideOutsideOfUK(event.getCaseData());
    }
}
