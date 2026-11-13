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
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;
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
        List<SendCorrespondenceEvent> events = buildHearingCorrespondenceEventsIfNeeded(callbackRequest, userAuthorisation);
        events.forEach(applicationEventPublisher::publishEvent);
    }

    /**
     * Builds correspondence events for the selected manage hearings action, if correspondence is required.
     *
     * <p>This routes to the action-specific builder:
     * add hearing uses {@code buildHearingCorrespondenceEventsIfNeeded},
     * and adjourn/vacate uses {@code buildAdjournedOrVacatedHearingCorrespondenceEventsIfNeeded}.</p>
     *
     * @param actionSelection the selected manage hearings action
     * @param callbackRequest the callback request containing case details and hearing data
     * @param userAuthorisation the authorization token used to build correspondence payloads
     * @return a list of correspondence events for the selected action, or an empty list if none are required
     */
    public List<SendCorrespondenceEvent> buildCorrespondenceEventsIfNeeded(ManageHearingsAction actionSelection,
                                                                            FinremCallbackRequest callbackRequest,
                                                                            String userAuthorisation) {
        return switch (actionSelection) {
            case ADD_HEARING -> buildHearingCorrespondenceEventsIfNeeded(callbackRequest, userAuthorisation);
            case ADJOURN_OR_VACATE_HEARING -> buildAdjournedOrVacatedHearingCorrespondenceEventsIfNeeded(
                callbackRequest,
                userAuthorisation
            );
        };
    }

    /**
     * Builds one {@link SendCorrespondenceEvent} per party for a hearing notification to be
     * sent to the solicitor, if notification is required.
     *
     * <p>
     * This method retrieves the active hearing in context and checks whether notifications
     * should be sent. If notifications are enabled, it gathers all relevant documents including
     * additional hearing documents, any required mini Form A, and associated working hearing
     * documents. It then constructs one correspondence event per party, so each party's
     * correspondence can be sent/tracked independently.
     * </p>
     *
     * @param callbackRequest   the callback request containing case details and data
     * @param userAuthorisation the authorization token of the user initiating this action
     * @return a list of {@link SendCorrespondenceEvent}s, one per party on the hearing,
     *         or an empty list if no notification is required
     */
    public List<SendCorrespondenceEvent> buildHearingCorrespondenceEventsIfNeeded(FinremCallbackRequest callbackRequest,
                                                                                  String userAuthorisation) {
        HearingCorrespondenceContext context = prepareHearingCorrespondenceContext(callbackRequest);
        if (isNull(context)) {
            return List.of();
        }

        return buildSendCorrespondenceEvents(
            context.caseDetails(),
            context.hearing(),
            ManageHearingsAction.ADD_HEARING,
            userAuthorisation,
            context.documentsToPost(),
            FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR
        );
    }

    /**
     * Gathers the case details, active hearing, and documents to post required to build hearing
     * correspondence events, if notification is required for the active hearing.
     *
     * <p>
     * Resolves the active hearing in context and, provided notifications are required, collects
     * additional hearing documents, any required mini Form A, and associated working hearing
     * documents into a single list of documents to post.
     * </p>
     *
     * @param callbackRequest the callback request containing case details and data
     * @return a {@link HearingCorrespondenceContext} with the case details, hearing, and documents
     *         to post, or {@code null} if the hearing does not require notifications
     */
    private HearingCorrespondenceContext prepareHearingCorrespondenceContext(FinremCallbackRequest callbackRequest) {
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

        return new HearingCorrespondenceContext(finremCaseDetails, hearing, documentsToPost);
    }

    /**
     * Simple holder for the data needed to build hearing correspondence events.
     *
     * @param caseDetails     the case details for the correspondence
     * @param hearing         the active hearing the correspondence relates to
     * @param documentsToPost the documents to be sent by post
     */
    private record HearingCorrespondenceContext(FinremCaseDetails caseDetails,
                                                Hearing hearing,
                                                List<CaseDocument> documentsToPost) {
    }

    /**
     * Builds correspondence events for an adjourned or vacated hearing when notifications are required.
     *
     * <p>If the hearing was vacated and relisted, this method also includes add-hearing
     * correspondence events by calling {@code buildHearingCorrespondenceEventsIfNeeded(...)}.
     * Publishing is handled by the caller.</p>
     *
     * <p>When a vacate/adjourn notice should be sent, this method selects the appropriate
     * template (adjourn or vacate) and builds one event per notification party.</p>
     *
     * @param callbackRequest the callback request containing case details and hearing data
     * @param userAuthorisation the authorization token used to build correspondence payloads
     * @return a list of correspondence events for relist and/or vacate/adjourn notice;
     *         returns an empty list when notification is not required
     */
    public List<SendCorrespondenceEvent> buildAdjournedOrVacatedHearingCorrespondenceEventsIfNeeded(FinremCallbackRequest callbackRequest,
                                                                                                    String userAuthorisation) {

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = finremCaseDetails.getData();
        ManageHearingsWrapper wrapper = finremCaseData.getManageHearingsWrapper();

        boolean isVacatedAndRelistedHearing = hearingCorrespondenceHelper.isVacatedAndRelistedHearing(finremCaseData);
        List<SendCorrespondenceEvent> correspondenceEvents = new ArrayList<>();

        if (isVacatedAndRelistedHearing) {
            correspondenceEvents.addAll(buildHearingCorrespondenceEventsIfNeeded(callbackRequest, userAuthorisation));
        }

        VacateOrAdjournedHearing vacateOrAdjournedHearing = hearingCorrespondenceHelper.getVacateOrAdjournedHearingInContext(
            wrapper, wrapper.getWorkingVacatedHearingId());

        // Always send vacate hearing notice when relisted, as user cannot select to send or not in this scenario
        if (shouldNotSendVacateOrAdjournNotification(isVacatedAndRelistedHearing, vacateOrAdjournedHearing)) {
            return correspondenceEvents;
        }

        VacateOrAdjournAction action = vacateOrAdjournedHearing.getHearingStatus();

        List<CaseDocument> documentsToPost = List.of(hearingCorrespondenceHelper.getVacateHearingNotice(finremCaseData));

        EmailTemplateNames templateName = VacateOrAdjournAction.ADJOURN_HEARING.equals(action)
            ? FR_CONTESTED_ADJOURN_NOTIFICATION_SOLICITOR
            : FR_CONTESTED_VACATE_NOTIFICATION_SOLICITOR;

        correspondenceEvents.addAll(buildSendCorrespondenceEvents(
            finremCaseDetails,
            vacateOrAdjournedHearing,
            ManageHearingsAction.ADJOURN_OR_VACATE_HEARING,
            userAuthorisation,
            documentsToPost,
            templateName
        ));

        return correspondenceEvents;
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

    /**
     * Builds one {@link SendCorrespondenceEvent} per notification party on the hearing.
     *
     * <p>For applicant and respondent parties, party-specific Form C documents are also resolved
     * before building each event.</p>
     *
     * @param caseDetails the case the correspondence relates to
     * @param hearing the hearing whose parties should be notified
     * @param action the action that triggered correspondence
     * @param userAuthorisation the calling user's auth token
     * @param baseDocumentsToPost base documents to post (not party specific)
     * @param templateName email template for notifications
     * @return one correspondence event per resolved notification party
     */
    private List<SendCorrespondenceEvent> buildSendCorrespondenceEvents(FinremCaseDetails caseDetails,
                                                                        HearingLike hearing,
                                                                        ManageHearingsAction action,
                                                                        String userAuthorisation,
                                                                        List<CaseDocument> baseDocumentsToPost,
                                                                        EmailTemplateNames templateName) {
        return partiesOnCase(hearing).stream()
            .map(party -> getNotificationPartyFromRole(party.getValue().getRole()))
            .filter(Objects::nonNull)
            .map(notificationParty -> baseEventBuilder(
                caseDetails,
                hearing,
                action,
                userAuthorisation,
                buildDocumentsToPostForParty(caseDetails, baseDocumentsToPost, notificationParty, action),
                templateName)
                .notificationParties(List.of(notificationParty))
                .build())
            .toList();
    }

    /**
     * Returns the documents to post for a specific party, substituting Form C with the
     * recipient-specific version where applicable.
     *
     * @param caseDetails case details containing Form C variants
     * @param baseDocumentsToPost base documents to post (not party specific)
     * @param party target notification party
     * @param action correspondence action to determine whether party-specific hearing docs are needed
     * @return posting documents for the specified party
     */
    private List<CaseDocument> buildDocumentsToPostForParty(FinremCaseDetails caseDetails,
                                                             List<CaseDocument> baseDocumentsToPost,
                                                             NotificationParty party,
                                                             ManageHearingsAction action) {
        if (!ManageHearingsAction.ADD_HEARING.equals(action)) {
            return baseDocumentsToPost;
        }

        FinremCaseData caseData = caseDetails.getData();
        UUID hearingId = caseData.getManageHearingsWrapper().getWorkingHearingId();
        List<CaseDocument> partyHearingDocuments = hearingCorrespondenceHelper
            .getHearingCorrespondenceDocumentsForParty(caseData, hearingId, party);

        List<CaseDocument> documentsToPostForParty = new ArrayList<>(baseDocumentsToPost);
        documentsToPostForParty.addAll(partyHearingDocuments);
        return documentsToPostForParty;
    }

    /**
     * Creates a {@link SendCorrespondenceEvent} builder pre-populated with the fields common
     * to both single-event and per-party correspondence, excluding {@code notificationParties},
     * which the caller is responsible for setting.
     *
     * @param caseDetails       the case the correspondence relates to
     * @param hearing           the hearing the correspondence relates to
     * @param action            the hearing management action that triggered this correspondence
     * @param userAuthorisation the calling user's auth token
     * @param documentsToPost   documents to be sent by post
     * @param templateName      the email template to use
     * @return a partially-built {@code SendCorrespondenceEvent.SendCorrespondenceEventBuilder}
     */
    private SendCorrespondenceEvent.SendCorrespondenceEventBuilder baseEventBuilder(
        FinremCaseDetails caseDetails,
        HearingLike hearing,
        ManageHearingsAction action,
        String userAuthorisation,
        List<CaseDocument> documentsToPost,
        EmailTemplateNames templateName) {
        return SendCorrespondenceEvent.builder()
            .emailNotificationRequest(buildNotificationRequest(caseDetails.getData(), action, hearing))
            .emailTemplate(templateName)
            .documentsToPost(documentsToPost)
            .caseDetails(caseDetails)
            .authToken(userAuthorisation);
    }

    /**
     * Returns the parties on the case for the given hearing.
     *
     * @param hearing the hearing to read parties from
     * @return the parties on the case, or an empty list if none are set
     */
    private List<PartyOnCaseCollectionItem> partiesOnCase(HearingLike hearing) {
        return Optional.ofNullable(hearing.getPartiesOnCase()).orElseGet(List::of);
    }

    private boolean shouldNotSendVacateOrAdjournNotification(boolean isVacatedAndRelistedHearing,
                                                             VacateOrAdjournedHearing vacateOrAdjournedHearing) {
        return !isVacatedAndRelistedHearing && !vacateOrAdjournedHearing.shouldSendNotifications();
    }
}
