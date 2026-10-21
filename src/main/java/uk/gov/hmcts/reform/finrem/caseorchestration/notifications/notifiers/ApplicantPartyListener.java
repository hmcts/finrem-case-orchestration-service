package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static com.google.common.base.Strings.nullToEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;

@Component
public class ApplicantPartyListener extends AbstractPartyListener {

    public ApplicantPartyListener(BulkPrintService bulkPrintService,
                                  EmailService emailService,
                                  NotificationService notificationService,
                                  InternationalPostalService internationalPostalService) {
        super(bulkPrintService, emailService, notificationService, internationalPostalService);
    }

    @Override
    protected NotificationParty getNotificationPartyEnum() {
        return NotificationParty.APPLICANT;
    }

    @Override
    protected String getNotificationParty() {
        return APPLICANT;
    }

    /**
     * Determines whether an email / paper notification should be sent to the applicant solicitor
     * for the given correspondence event.
     *
     * <p><strong>Important:</strong> this method must <em>not</em> depend on an API call
     * (e.g. the Case Assignment API) to determine the applicant's representation state.
     * The {@code about-to-submit} callback closes the transaction and persists the case data,
     * and the {@code submitted} callback then acts on that persisted data and calls other
     * third-party services. The event itself may grant the applicant solicitor access to the
     * case, which changes the applicant's digital state, so an API lookup would not reliably
     * reflect the persisted case data. The represented flag, organisation ID, and solicitor
     * email held in the case data are the source of truth for this decision.
     *
     * @param event the send correspondence event containing the case details to evaluate
     * @return {@code true} if the applicant solicitor is digital; {@code false} otherwise
     */
    @Override
    protected boolean shouldSendEmailNotification(SendCorrespondenceEvent event) {
        FinremCaseDetails caseDetails = event.getCaseDetails();
        return caseDetails.isApplicantSolicitorDigital();
    }

    @Override
    protected PartySpecificDetails setPartySpecificDetails(SendCorrespondenceEvent event) {
        FinremCaseDetails caseDetails = event.getCaseDetails();
        String email = caseDetails.getAppSolicitorEmail();
        String name = caseDetails.getAppSolicitorName();
        String ref = nullToEmpty(caseDetails.getApplicantSolicitorRef());
        return new PartySpecificDetails(email, name, ref);
    }

    @Override
    protected CaseDocument getPartyCoversheet(SendCorrespondenceEvent event) {
        return bulkPrintService.getApplicantCoverSheet(event.getCaseDetails(), event.authToken);
    }

    @Override
    protected boolean isPartyOutsideUK(SendCorrespondenceEvent event) {
        return internationalPostalService.isApplicantResideOutsideOfUK(event.getCaseData());
    }
}
