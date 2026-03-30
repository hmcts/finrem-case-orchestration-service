package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistoryCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NocLetterNotificationService;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest.RESPONDENT_PARTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENTED_NOC_CASEWORKER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_NOC_CASEWORKER;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateContactDetailsNotificationService {

    private final FinremNotificationRequestMapper finremNotificationRequestMapper;

    private final NocLetterNotificationService nocLetterNotificationService;

    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    /**
     * Determines whether Notice of Change notifications should be sent.
     *
     * <p>This method checks if the case data indicates that the update includes
     * a representative change. Notifications are required only when this flag
     * is set to {@link YesOrNo#YES}.
     *
     * @param finremCaseData the case data containing contact details and update flags
     * @return {@code true} if notifications are required; {@code false} otherwise
     */
    public boolean requiresNotifications(FinremCaseData finremCaseData) {
        return YesOrNo.isYes(finremCaseData.getContactDetailsWrapper().getUpdateIncludesRepresentativeChange());
    }

    /**
     * Sends a Notice of Change (NOC) email initiated by a caseworker to the relevant litigant solicitor.
     *
     * <p>This method determines the appropriate email template based on the provided
     * {@link FinremCaseDetails}, constructs a {@link NotificationRequest}, and prepares
     * a {@link SendCorrespondenceEvent} to trigger the email notification.
     *
     * <p>The email informs the recipient that a Notice of Change request has been completed
     * and that the case has been added to the organisation’s unassigned case list.
     *
     * @param caseDetails the case details used to determine the email template and populate
     *                    the notification request
     * @return a {@link SendCorrespondenceEvent} representing the prepared email notification event
     */
    public SendCorrespondenceEvent prepareNocEmailToLitigantSolicitor(FinremCaseDetails caseDetails) {
        EmailTemplateNames template = getNocEmailTemplateToLitigantSolicitor(caseDetails);
        return createNocEmailToLitigantSolicitorEvent(caseDetails, template);
    }

    /**
     * Invokes all configured Notice of Change (NOC) letter handlers for the given case.
     *
     * <p>This method iterates through the registered {@code letterHandlers} and delegates
     * the handling of NOC letter generation and sending to each handler. Each handler is
     * responsible for determining whether a letter should be produced based on the
     * provided case details and performing the appropriate action.
     *
     * <p><strong>Note:</strong> This implementation should be refactored in the future to
     * use {@link SendCorrespondenceEvent} for consistency with the notification
     * approach. (Related to DFR-4303)
     *
     * @param finremCaseDetails        the current case details
     * @param finremCaseDetailsBefore  the previous case details, used to detect changes
     * @param authToken                the authorisation token used for downstream service calls
     */
    public void sendNocLetterToLitigants(FinremCaseDetails finremCaseDetails, FinremCaseDetails finremCaseDetailsBefore,
                                         String authToken) {
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        CaseDetails caseDetailsBefore = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetailsBefore);
        nocLetterNotificationService.sendNoticeOfChangeLetters(caseDetails, caseDetailsBefore, authToken);
    }

    private EmailTemplateNames getNocEmailTemplateToLitigantSolicitor(FinremCaseDetails caseDetails) {
        return caseDetails.isConsentedApplication() ? FR_CONSENTED_NOC_CASEWORKER : FR_CONTESTED_NOC_CASEWORKER;
    }

    private SendCorrespondenceEvent createNocEmailToLitigantSolicitorEvent(FinremCaseDetails caseDetails,
                                                                           EmailTemplateNames template) {

        FinremCaseData finremCaseData = caseDetails.getData();
        boolean isRespondentSolicitorChanged = isRespondentSolicitorChangedOnLatestRepresentationUpdate(finremCaseData);

        return SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails)
            .emailNotificationRequest(finremNotificationRequestMapper.getNotificationRequestForNoticeOfChange(caseDetails,
                isRespondentSolicitorChanged))
            .notificationParties(List.of(getNocNotificationParty(isRespondentSolicitorChanged)))
            .emailTemplate(template)
            .build();
    }

    private NotificationParty getNocNotificationParty(boolean isRespondentSolicitorChangedOnLatestRepresentationUpdate) {
        return isRespondentSolicitorChangedOnLatestRepresentationUpdate
            ? NotificationParty.RESPONDENT_SOLICITOR_ONLY
            : NotificationParty.APPLICANT_SOLICITOR_ONLY;
    }

    private boolean isRespondentSolicitorChangedOnLatestRepresentationUpdate(FinremCaseData finremCaseData) {
        Optional<RepresentationUpdate> latest = getLastRepresentationUpdate(finremCaseData);
        return RESPONDENT_PARTY.equalsIgnoreCase(latest.map(RepresentationUpdate::getParty).orElseThrow(()
            -> new IllegalStateException("No latest representation update found for case: %s"
            .formatted(finremCaseData.getCcdCaseId()))));
    }

    private Optional<RepresentationUpdate> getLastRepresentationUpdate(FinremCaseData finremCaseData) {
        return emptyIfNull(finremCaseData.getRepresentationUpdateHistory())
            .stream()
            .map(RepresentationUpdateHistoryCollection::getValue)
            .filter(Objects::nonNull)
            .max(Comparator.comparing(RepresentationUpdate::getDate, Comparator.nullsLast(Comparator.naturalOrder())));
    }
}
