package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.consented;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationAuditService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremSingleLetterOrEmailAllPartiesCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckSolicitorIsDigitalService;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_ASSIGNED_TO_JUDGE;

/**
 * Builds the correspondence events and audit records for the "assign to judge" notification
 * in the <strong>consented</strong> journey.
 *
 * <p>
 * This class applies to consented applications only. It is not used for contested cases,
 * so it deals only with the two parties to a consented case, the applicant and the
 * respondent. There are no interveners in the consented journey.
 * </p>
 *
 * <p>
 * When a consented case is assigned to a judge, the parties are told by email
 * ({@code FR_ASSIGNED_TO_JUDGE} template) and, where needed, by a posted letter. Rather than
 * sending that correspondence directly, this class builds {@link SendCorrespondenceEvent}s
 * that describe it, so it can be published and sent elsewhere. It offers two operations:
 * </p>
 * <ul>
 *     <li>{@link #buildSendCorrespondenceEvents(EventType, FinremCaseDetails, String)}
 *     builds the applicant and respondent events used to send the correspondence</li>
 *     <li>{@link #createAuditsForCorrespondence(EventType, FinremCaseDetails, String)}
 *     records notification audits for the same events, linking them with a shared
 *     notification tracker ID</li>
 * </ul>
 *
 * <p>
 * Solicitor email requests are built with the digital or non-digital variant depending on
 * whether each party's solicitor is registered as digital.
 * </p>
 *
 * @see FinremSingleLetterOrEmailAllPartiesCorresponder
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AssignToJudgeCorresponder {

    private static final EmailTemplateNames EMAIL_TEMPLATE = FR_ASSIGNED_TO_JUDGE;

    private final AssignedToJudgeDocumentService assignedToJudgeDocumentService;

    private final FinremNotificationRequestMapper finremNotificationRequestMapper;

    private final CheckSolicitorIsDigitalService checkSolicitorIsDigitalService;

    private final NotificationAuditService notificationAuditService;

    /**
     * Creates audit records for all correspondence associated with the given event, linking them
     * under a common notification tracker ID.
     *
     * <p>
     * The correspondence events to audit are built via
     * {@link #buildSendCorrespondenceEventsForAuditCreation(EventType, FinremCaseDetails, String)}.
     * The first audit creates a notification tracker ID, which is then set on every subsequent
     * {@link SendCorrespondenceEvent} before its audit is created. This ensures that all
     * correspondence audits created in this call are linked to the same notification tracker.
     *
     * @param eventType the type of event that triggered the correspondence
     * @param finremCaseDetails the case details for which correspondence audits are being created
     * @param userAuthorisation the authorisation token of the user triggering the action
     */
    public void createAuditsForCorrespondence(EventType eventType, FinremCaseDetails finremCaseDetails, String userAuthorisation) {
        List<SendCorrespondenceEvent> events = buildSendCorrespondenceEventsForAuditCreation(eventType, finremCaseDetails,
            userAuthorisation);
        String trackerId = null;
        for (SendCorrespondenceEvent event : events) {
            if (nonNull(trackerId)) {
                event.setNotificationTrackerId(trackerId);
            }
            trackerId = notificationAuditService.createAuditsForCorrespondence(event, eventType);
        }
    }

    /**
     * Builds the {@link SendCorrespondenceEvent}s needed to notify all parties of the given
     * event, including a document to post for each party, for use when creating correspondence
     * audits.
     *
     * <p>
     * This is a convenience overload that delegates to
     * {@link #buildSendCorrespondenceEvents(EventType, FinremCaseDetails, boolean, String)}
     * with {@code includeDocumentsToPost} set to {@code true}, so each event includes a
     * document generated for that party's paper notification recipient.
     *
     * @param eventType the event that triggered the correspondence; its name is used as the
     *                  event ID on each {@link SendCorrespondenceEvent}
     * @param finremCaseDetails the case details used to build the email notification requests,
     *                  the documents to print and, for contested applications, the list of
     *                  interveners
     * @param authToken the authorisation token used to generate the documents and passed on to
     *                  each event
     * @return a list of {@link SendCorrespondenceEvent}s, one per party to be notified; never
     *         {@code null}, and always contains at least the applicant and respondent events
     * @see #buildSendCorrespondenceEvents(EventType, FinremCaseDetails, boolean, String)
     */
    public List<SendCorrespondenceEvent> buildSendCorrespondenceEvents(EventType eventType, FinremCaseDetails finremCaseDetails,
                                                                       String authToken) {
        return buildSendCorrespondenceEvents(eventType, finremCaseDetails, true, authToken);
    }

    /**
     * Builds the {@link SendCorrespondenceEvent}s needed to notify all parties of the
     * given event.
     *
     * <p>
     * This method replaces logic in
     * {@code FinremSingleLetterOrEmailAllPartiesCorresponder.sendCorrespondence}. That
     * method sent the correspondence directly, whereas this one only builds the events
     * that describe it. The logic for each party is carried over from the original:
     * </p>
     * <ul>
     *     <li>the applicant event replaces {@code sendApplicantCorrespondence}</li>
     *     <li>the respondent event replaces {@code sendRespondentCorrespondence}</li>
     * </ul>
     *
     * <p>
     * One event is created per party, in the order listed above, with interveners in the
     * order returned by the case data.
     * </p>
     *
     * <p>
     * Every event carries the same event ID (the name of {@code eventType}), case details,
     * email template and auth token. Each event targets a single {@link NotificationParty}
     * and includes the email notification request for that party. When
     * {@code includeDocumentsToPost} is true, each event also includes a document to post,
     * generated for that party's paper notification recipient.
     * </p>
     *
     * <p>
     * This method only builds the events. It does not publish them or send any
     * correspondence.
     * </p>
     *
     * @param eventType              the event that triggered the correspondence; its name is used
     *                               as the event ID on each {@link SendCorrespondenceEvent}
     * @param finremCaseDetails      the case details used to build the email notification requests,
     *                               the documents to print and, for contested applications, the
     *                               list of interveners
     * @param includeDocumentsToPost if {@code true}, a document is generated for each party and
     *                               added to that party's event for posting; if {@code false}, no
     *                               documents are generated and every event is built with an empty
     *                               list of documents to post
     * @param authToken              the authorisation token used to generate the documents and
     *                               passed on to each event
     * @return a list of {@link SendCorrespondenceEvent}s, one per party to be notified; never
     *         {@code null}, and always contains at least the applicant and respondent events
     * @see FinremSingleLetterOrEmailAllPartiesCorresponder
     */
    private List<SendCorrespondenceEvent> buildSendCorrespondenceEvents(EventType eventType,
                                                                        FinremCaseDetails finremCaseDetails,
                                                                        boolean includeDocumentsToPost, String authToken) {
        List<SendCorrespondenceEvent> events = new ArrayList<>();
        events.add(
            SendCorrespondenceEvent.builder()
                .eventId(eventType.name())
                .caseDetails(finremCaseDetails)
                .notificationParties(List.of(NotificationParty.APPLICANT))
                .emailTemplate(EMAIL_TEMPLATE)
                .emailNotificationRequest(getApplicantEmailNotificationRequest(finremCaseDetails))
                .documentsToPost(includeDocumentsToPost
                    ? List.of(getDocumentToPrint(finremCaseDetails, authToken, DocumentHelper.PaperNotificationRecipient.APPLICANT))
                    : List.of())
                .authToken(authToken)
            .build()
        );
        events.add(
            SendCorrespondenceEvent.builder()
                .eventId(eventType.name())
                .caseDetails(finremCaseDetails)
                .notificationParties(List.of(NotificationParty.RESPONDENT))
                .emailTemplate(EMAIL_TEMPLATE)
                .emailNotificationRequest(getRespondentEmailNotificationRequest(finremCaseDetails))
                .documentsToPost(includeDocumentsToPost
                    ? List.of(getDocumentToPrint(finremCaseDetails, authToken, DocumentHelper.PaperNotificationRecipient.RESPONDENT))
                    : List.of())
                .authToken(authToken)
            .build()
        );

        return events;
    }

    private List<SendCorrespondenceEvent> buildSendCorrespondenceEventsForAuditCreation(EventType eventType,
                                                                                       FinremCaseDetails finremCaseDetails,
                                                                                       String authToken) {
        return buildSendCorrespondenceEvents(eventType, finremCaseDetails, false, authToken);
    }

    private NotificationRequest getApplicantEmailNotificationRequest(FinremCaseDetails caseDetails) {
        return finremNotificationRequestMapper
            .getNotificationRequestForApplicantSolicitor(caseDetails, !isApplicantSolicitorDigital(caseDetails));
    }

    private NotificationRequest getRespondentEmailNotificationRequest(FinremCaseDetails caseDetails) {
        return finremNotificationRequestMapper
            .getNotificationRequestForRespondentSolicitor(caseDetails, !isRespondentSolicitorDigital(caseDetails));
    }

    private boolean isApplicantSolicitorDigital(FinremCaseDetails caseDetails) {
        return checkSolicitorIsDigitalService.isApplicantSolicitorDigital(caseDetails.getId().toString());
    }

    private boolean isRespondentSolicitorDigital(FinremCaseDetails caseDetails) {
        return checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString());
    }

    private CaseDocument getDocumentToPrint(FinremCaseDetails caseDetails, String authorisationToken,
                                            DocumentHelper.PaperNotificationRecipient recipient) {
        return assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(
            caseDetails, authorisationToken, recipient);
    }
}
