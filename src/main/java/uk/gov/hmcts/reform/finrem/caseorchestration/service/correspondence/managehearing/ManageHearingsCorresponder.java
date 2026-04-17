package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.CourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.managehearings.HearingCorrespondenceHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCaseCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.VacateOrAdjournAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.HearingLike;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.VacateOrAdjournedHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_ADJOURN_NOTIFICATION_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_VACATE_NOTIFICATION_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.getNotificationPartyFromRole;

@RequiredArgsConstructor
@Service
@Slf4j
public class ManageHearingsCorresponder {

    private final HearingCorrespondenceHelper hearingCorrespondenceHelper;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Collect associated new hearing information and send hearing notification to the solicitor through
     *  a correspondence event publisher.
     *
     * @param callbackRequest the callback request containing case details and data
     * @param userAuthorisation the authorization token of the user initiating this action
     */
    public void sendHearingCorrespondence(FinremCallbackRequest callbackRequest, String userAuthorisation) {
        SendCorrespondenceEvent event = buildHearingCorrespondenceEventIfNeeded(callbackRequest, userAuthorisation);
        if (event != null) {
            applicationEventPublisher.publishEvent(event);
        }
    }

    /**
     * Builds a {@link SendCorrespondenceEvent} for a hearing notification to be sent to the solicitor,
     * if notification is required.
     *
     * <p>
     * This method retrieves the active hearing in context and checks whether notifications
     * should be sent. If notifications are enabled, it gathers all relevant documents including
     * additional hearing documents, any required mini Form A, and associated working hearing
     * documents. It then constructs a correspondence event to notify the solicitor.
     * </p>
     *
     * @param callbackRequest the callback request containing case details and data
     * @param userAuthorisation the authorization token of the user initiating this action
     * @return a {@link SendCorrespondenceEvent} containing the hearing notification details,
     *         or {@code null} if no notification is required
     */
    public SendCorrespondenceEvent buildHearingCorrespondenceEventIfNeeded(FinremCallbackRequest callbackRequest,
                                                                           String userAuthorisation) {

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = finremCaseDetails.getData();
        ManageHearingsWrapper wrapper = finremCaseData.getManageHearingsWrapper();
        Hearing hearing = hearingCorrespondenceHelper.getActiveHearingInContext(wrapper, wrapper.getWorkingHearingId());

        if (!hearing.shouldSendNotifications()) {
            return null;
        }

        List<CaseDocument> documentsToPost = getAdditionalHearingDocs(hearing);
        hearingCorrespondenceHelper.getMiniFormAIfRequired(finremCaseData, hearing)
            .ifPresent(documentsToPost::add);
        documentsToPost.addAll(wrapper.getAssociatedWorkingHearingDocuments());

        return buildSendCorrespondenceEvent(
            finremCaseDetails,
            hearing,
            ManageHearingsAction.ADD_HEARING,
            userAuthorisation,
            documentsToPost,
            FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR
        );
    }

    /**
     * Builds a {@link SendCorrespondenceEvent} to notify the solicitor when a hearing
     * is adjourned or vacated, if notification is required.
     *
     * <p>
     * This method determines whether the hearing has been vacated and relisted. In such cases,
     * a hearing correspondence is always sent via {@code sendHearingCorrespondence}, as the user
     * cannot opt out of notifications. It then retrieves the vacated or adjourned hearing in context
     * and evaluates whether a notification should be sent.
     * </p>
     *
     * <p>
     * If notification is required, it prepares the relevant hearing notice document and selects
     * the appropriate email template based on whether the hearing was adjourned or vacated,
     * before constructing the correspondence event.
     * </p>
     *
     * @param callbackRequest the callback request containing case details and data
     * @param userAuthorisation the authorization token of the user initiating this action
     * @return a {@link SendCorrespondenceEvent} containing the hearing notification details,
     *         or {@code null} if notification should not be sent
     */
    public SendCorrespondenceEvent buildAdjournedOrVacatedHearingCorrespondenceEventIfNeeded(FinremCallbackRequest callbackRequest,
                                                                                             String userAuthorisation) {

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = finremCaseDetails.getData();
        ManageHearingsWrapper wrapper = finremCaseData.getManageHearingsWrapper();

        boolean isVacatedAndRelistedHearing = hearingCorrespondenceHelper.isVacatedAndRelistedHearing(finremCaseData);

        if (isVacatedAndRelistedHearing) {
            sendHearingCorrespondence(callbackRequest, userAuthorisation);
        }

        VacateOrAdjournedHearing vacateOrAdjournedHearing = hearingCorrespondenceHelper.getVacateOrAdjournedHearingInContext(
            wrapper, wrapper.getWorkingVacatedHearingId());

        // Always send vacate hearing notice when relisted, as user cannot select to send or not in this scenario
        if (shouldNotSendVacateOrAdjournNotification(isVacatedAndRelistedHearing, vacateOrAdjournedHearing)) {
            return null;
        }

        VacateOrAdjournAction action = vacateOrAdjournedHearing.getHearingStatus();

        List<CaseDocument> documentsToPost = List.of(hearingCorrespondenceHelper.getVacateHearingNotice(finremCaseData));

        EmailTemplateNames templateName = VacateOrAdjournAction.ADJOURN_HEARING.equals(action)
            ? FR_CONTESTED_ADJOURN_NOTIFICATION_SOLICITOR
            : FR_CONTESTED_VACATE_NOTIFICATION_SOLICITOR;

        return buildSendCorrespondenceEvent(
            finremCaseDetails,
            vacateOrAdjournedHearing,
            ManageHearingsAction.ADJOURN_OR_VACATE_HEARING,
            userAuthorisation,
            documentsToPost,
            templateName
        );
    }

    /**
     * Builds a NotificationRequest object using the provided case data and hearings information.
     *
     * @param caseData the case data containing details like contact information and case ID
     * @param action the action being performed on the hearing (e.g., add or vacate)
     * @param hearing the hearing information from which to extract details for the notification
     * @return a fully constructed NotificationRequest object with data extracted from the inputs
     */
    private NotificationRequest buildNotificationRequest(FinremCaseData caseData, ManageHearingsAction action, HearingLike hearing) {
        ContactDetailsWrapper contactDetailsWrapper = caseData.getContactDetailsWrapper();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");

        String vacatedHearingType = "";
        String vacatedHearingDateTime = "";

        if (ManageHearingsAction.ADJOURN_OR_VACATE_HEARING.equals(action)) {

            vacatedHearingType = Optional.ofNullable(hearing.getHearingType())
                .orElseThrow(() -> new IllegalStateException("Hearing type must not be null")).getId();

            String formattedDate = Optional.ofNullable(hearing.getHearingDate())
                .orElseThrow(() -> new IllegalStateException("Hearing date must not be null"))
                .format(dateFormatter);

            String hearingTime = Optional.ofNullable(hearing.getHearingTime())
                .orElseThrow(() -> new IllegalStateException("Hearing time must not be null"));

            vacatedHearingDateTime = "%s at %s".formatted(
                formattedDate,
                hearingTime
            );
        }

        String hearingType = Optional.ofNullable(hearing.getHearingType())
            .orElseThrow(() -> new IllegalStateException("Hearing type must not be null")).getId();

        String applicantSurname = contactDetailsWrapper.getApplicantLname();
        String respondentSurname = contactDetailsWrapper.getRespondentLname();
        String selectedFRC = CourtHelper.getFRCForHearing(hearing);

        String formattedHearingDate = Optional.ofNullable(hearing.getHearingDate())
            .orElseThrow(() -> new IllegalStateException("Hearing date must not be null"))
            .format(dateFormatter);

        return NotificationRequest.builder()
            .caseReferenceNumber(caseData.getCcdCaseId())
            .hearingType(hearingType)
            .hearingDate(formattedHearingDate)
            .applicantName(applicantSurname)
            .respondentName(respondentSurname)
            .caseType(EmailService.CONTESTED)
            .selectedCourt(selectedFRC)
            .vacatedHearingDateTime(vacatedHearingDateTime)
            .vacatedHearingType(vacatedHearingType)
            .build();
    }

    private List<CaseDocument> getAdditionalHearingDocs(HearingLike hearing) {
        return new ArrayList<>(Optional.ofNullable(hearing.getAdditionalHearingDocs())
            .orElseGet(List::of)
            .stream()
            .map(DocumentCollectionItem::getValue)
            .toList());
    }

    private SendCorrespondenceEvent buildSendCorrespondenceEvent(FinremCaseDetails caseDetails,
                                                                 HearingLike hearing,
                                                                 ManageHearingsAction action,
                                                                 String userAuthorisation,
                                                                 List<CaseDocument> documentsToPost,
                                                                 EmailTemplateNames templateName) {
        List<PartyOnCaseCollectionItem> partiesOnCase =
            Optional.ofNullable(hearing.getPartiesOnCase()).orElseGet(List::of);

        return SendCorrespondenceEvent.builder()
            .notificationParties(partiesOnCase.stream()
                .map(party -> getNotificationPartyFromRole(party.getValue().getRole()))
                .toList())
            .emailNotificationRequest(buildNotificationRequest(caseDetails.getData(), action, hearing))
            .emailTemplate(templateName)
            .documentsToPost(documentsToPost)
            .caseDetails(caseDetails)
            .authToken(userAuthorisation)
            .build();
    }

    private boolean shouldNotSendVacateOrAdjournNotification(boolean isVacatedAndRelistedHearing,
                                                          VacateOrAdjournedHearing vacateOrAdjournedHearing) {
        return !isVacatedAndRelistedHearing && !vacateOrAdjournedHearing.shouldSendNotifications();
    }
}
