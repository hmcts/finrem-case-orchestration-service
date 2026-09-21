package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static com.google.common.base.Strings.nullToEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
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
    protected NotificationParty getNotificationPartyEnum() {
        return NotificationParty.RESPONDENT;
    }

    @Override
    protected String getNotificationParty() {
        return RESPONDENT;
    }

    /**
     * Determines whether an email / paper notification should be sent to the respondent solicitor
     * for the given correspondence event, based on the case data alone: the respondent must
     * be marked as represented and a respondent solicitor email must be populated.
     *
     * <p>The contested or consented "respondent represented" flag is used depending on the
     * application type. Returns {@code false} if the respondent is not represented or the
     * solicitor email is blank.
     *
     * <p><strong>Important:</strong> this method must <em>not</em> depend on an API call
     * (e.g. the Case Assignment API) to determine the respondent's representation state.
     * The {@code about-to-submit} callback closes the transaction and persists the case data,
     * and the {@code submitted} callback then acts on that persisted data and calls other
     * third-party services. The event itself may grant the respondent solicitor access to the
     * case, which changes the respondent's digital state, so an API lookup would not reliably
     * reflect the persisted case data. The represented flag and solicitor email held in the
     * case data are the source of truth for this decision.
     *
     * @param event the send correspondence event containing the case details to evaluate
     * @return {@code true} if the respondent is represented and the respondent solicitor
     *         email is populated; {@code false} otherwise
     */
    @Override
    protected boolean shouldSendEmailNotification(SendCorrespondenceEvent event) {
        FinremCaseDetails caseDetails = event.getCaseDetails();
        FinremCaseData finremCaseData = caseDetails.getData();
        ContactDetailsWrapper contactDetailsWrapper = finremCaseData.getContactDetailsWrapper();
        if (caseDetails.isContestedApplication()) {
            return !isBlank(YesOrNo.isYes(contactDetailsWrapper.getContestedRespondentRepresented())
                ? finremCaseData.getRespondentSolicitorEmail() : null);
        } else {
            return !isBlank(YesOrNo.isYes(contactDetailsWrapper.getConsentedRespondentRepresented())
                ? finremCaseData.getRespondentSolicitorEmail() : null);
        }
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
