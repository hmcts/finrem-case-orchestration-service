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

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = finremCaseDetails.getData();
        ManageHearingsWrapper wrapper = finremCaseData.getManageHearingsWrapper();
        Hearing hearing = hearingCorrespondenceHelper.getActiveHearingInContext(wrapper, wrapper.getWorkingHearingId());

        if (!hearing.shouldSendNotifications()) {
            return;
        }

        List<CaseDocument> documentsToPost = getAdditionalHearingDocs(hearing);
        hearingCorrespondenceHelper.getMiniFormAIfRequired(finremCaseData, hearing)
            .ifPresent(documentsToPost::add);
        documentsToPost.addAll(wrapper.getAssociatedWorkingHearingDocuments());

        publishEvent(
            finremCaseDetails,
            hearing,
            ManageHearingsAction.ADD_HEARING,
            userAuthorisation,
            documentsToPost,
            FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR
        );
    }

    /**
     * Collect associated vacated hearing information and send hearing notification to the solicitor through
     *  a correspondence event publisher.
     *
     * @param callbackRequest the callback request containing case details and data
     * @param userAuthorisation the authorization token of the user initiating this action
     */
    public void sendVacatedHearingCorrespondence(FinremCallbackRequest callbackRequest, String userAuthorisation) {

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
        if (!isVacatedAndRelistedHearing && !vacateOrAdjournedHearing.shouldSendNotifications()) {
            return;
        }

        List<CaseDocument> documentsToPost = List.of(hearingCorrespondenceHelper.getVacateHearingNotice(finremCaseData));

        publishEvent(
            finremCaseDetails,
            vacateOrAdjournedHearing,
            ManageHearingsAction.VACATE_HEARING,
            userAuthorisation, documentsToPost,
            FR_CONTESTED_VACATE_NOTIFICATION_SOLICITOR
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

        if (action.equals(ManageHearingsAction.VACATE_HEARING)) {
            vacatedHearingType = hearing.getHearingType().getId();
            vacatedHearingDateTime = "%s at %s".formatted(
                hearing.getHearingDate().format(dateFormatter),
                hearing.getHearingTime()
            );
        }
        String applicantSurname = contactDetailsWrapper.getApplicantLname();
        String respondentSurname = contactDetailsWrapper.getRespondentLname();
        String selectedFRC = CourtHelper.getFRCForHearing(hearing);

        String formattedHearingDate = hearing.getHearingDate().format(dateFormatter);

        return NotificationRequest.builder()
            .caseReferenceNumber(String.valueOf(caseData.getCcdCaseId()))
            .hearingType(hearing.getHearingType().getId())
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
        return new ArrayList<>(hearing.getAdditionalHearingDocs().stream()
            .map(DocumentCollectionItem::getValue)
            .toList());
    }

    /**
     * Publishes a correspondence event for managing hearing notifications.
     *
     * @param caseDetails the details of the financial remedy case
     * @param hearing the hearing-related information to be included in the event
     * @param userAuthorisation the authorization token of the user triggering the event
     * @param documentsToPost the list of documents to be sent as part of the correspondence
     * @param templateName the email template name to use for notifications
     */
    private void publishEvent(FinremCaseDetails caseDetails,
                              HearingLike hearing,
                              ManageHearingsAction action,
                              String userAuthorisation,
                              List<CaseDocument> documentsToPost,
                              EmailTemplateNames templateName) {

        List<PartyOnCaseCollectionItem> partiesOnCase =
            Optional.ofNullable(hearing.getPartiesOnCase()).orElseGet(List::of);

        applicationEventPublisher.publishEvent(SendCorrespondenceEvent.builder()
            .notificationParties(partiesOnCase.stream()
                .map(party -> getNotificationPartyFromRole(party.getValue().getRole()))
                .toList())
            .emailNotificationRequest(buildNotificationRequest(caseDetails.getData(), action, hearing))
            .emailTemplateId(templateName)
            .documentsToPost(documentsToPost)
            .caseDetails(caseDetails)
            .authToken(userAuthorisation)
            .build()
        );
    }
}
