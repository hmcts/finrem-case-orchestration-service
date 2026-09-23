package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * լս Listener responsible for handling correspondence notifications
 * specifically for the applicant's solicitor.
 *
 * <p>This listener extends {@link EmailNotificationOnlyListener} and determines:
 * <ul>
 *     <li>Whether the applicant solicitor is the intended notification party</li>
 *     <li>Whether an email notification should be sent based on available case details</li>
 * </ul>
 *
 * <p>Email notifications are only sent when the applicant solicitor's email
 * is present and marked as valid within the case data.
 */
@Component
public class ApplicantSolicitorListener extends EmailNotificationOnlyListener {

    public ApplicantSolicitorListener(BulkPrintService bulkPrintService,
                                      EmailService emailService,
                                      NotificationService notificationService,
                                      InternationalPostalService internationalPostalService) {
        super(bulkPrintService, emailService, notificationService, internationalPostalService);
    }

    @Override
    protected NotificationParty getNotificationPartyEnum() {
        return NotificationParty.APPLICANT_SOLICITOR_ONLY;
    }

    @Override
    protected String getNotificationParty() {
        return "applicant solicitor";
    }

    /**
     * Determines whether an email / paper notification should be sent to the applicant solicitor
     * for the given correspondence event, based on the case data alone: the applicant solicitor
     * email must be populated.
     *
     * <p><strong>Important:</strong> this method must <em>not</em> depend on an API call
     * (e.g. the Case Assignment API) to determine the applicant's representation state.
     * The {@code about-to-submit} callback closes the transaction and persists the case data,
     * and the {@code submitted} callback then acts on that persisted data and calls other
     * third-party services. The event itself may grant the applicant solicitor access to the
     * case, which changes the applicant's digital state, so an API lookup would not reliably
     * reflect the persisted case data. The solicitor email held in the case data is the source
     * of truth for this decision.
     *
     * @param event the send correspondence event containing the case details to evaluate
     * @return {@code true} if the applicant solicitor email is populated; {@code false} otherwise
     */
    @Override
    protected boolean shouldSendEmailNotification(SendCorrespondenceEvent event) {
        return isNotBlank(ofNullable(event.getCaseDetails()).map(FinremCaseDetails::getAppSolicitorEmail)
            .orElse(null));
    }
}
