package uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.LetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEventWithDescription;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.APP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.RESP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_APPLICANT_BARRISTER_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_APPLICANT_SOLICITOR_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_RESPONDENT_BARRISTER_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_RESPONDENT_SOLICITOR_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.getFormerIntervenerBarrister;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.getFormerIntervenerSolicitor;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.EmailTemplateResolver.getNotifyApplicantRepresentativeTemplateName;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.EmailTemplateResolver.getNotifyIntervenerRepresentativeTemplateName;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.EmailTemplateResolver.getNotifyRespondentRepresentativeTemplateName;

@Service
@Slf4j
@RequiredArgsConstructor
public class StopRepresentingClientCorresponder {

    private final GenericDocumentService genericDocumentService;

    private final DocumentConfiguration documentConfiguration;

    private final LetterDetailsMapper letterDetailsMapper;

    private final FinremNotificationRequestMapper finremNotificationRequestMapper;

    /**
     * Generates a "Stop Representing" letter for the applicant.
     *
     * <p>Uses the configured applicant template and delegates to
     * {@link #generateStopRepresentingLetter(FinremCaseDetails, String, DocumentHelper.PaperNotificationRecipient, String, String)}.
     * The returned {@link CaseDocument} contains the generated PDF letter notifying that
     * the applicant's representation has been removed.</p>
     *
     * @param finremCaseDetails the case details for generating the letter
     * @param authorisationToken the user's authorisation token for document generation
     * @return a {@link CaseDocument} containing the generated letter to the applicant
     */
    public CaseDocument generateStopRepresentingApplicantLetter(FinremCaseDetails finremCaseDetails,
                                                                String authorisationToken) {
        return generateStopRepresentingLetter(
            finremCaseDetails,
            authorisationToken,
            APPLICANT,
            "ApplicantRepresentationRemovalNotice",
            documentConfiguration.getStopRepresentingLetterToApplicantTemplate()
        );
    }

    /**
     * Generates a "Stop Representing" letter for the respondent.
     *
     * <p>Uses the configured respondent template and delegates to
     * {@link #generateStopRepresentingLetter(FinremCaseDetails, String, DocumentHelper.PaperNotificationRecipient, String, String)}.
     * The returned {@link CaseDocument} contains the generated PDF letter notifying that
     * the respondent's representation has been removed.</p>
     *
     * @param finremCaseDetails the case details for generating the letter
     * @param authorisationToken the user's authorisation token for document generation
     * @return a {@link CaseDocument} containing the generated letter to the respondent
     */
    public CaseDocument generateStopRepresentingRespondentLetter(FinremCaseDetails finremCaseDetails,
                                                                 String authorisationToken) {
        return generateStopRepresentingLetter(
            finremCaseDetails,
            authorisationToken,
            RESPONDENT,
            "RespondentRepresentationRemovalNotice",
            documentConfiguration.getStopRepresentingLetterToRespondentTemplate()
        );
    }

    /**
     * Prepares email notification events for representatives whose access has been revoked.
     *
     * <p>If the {@link LitigantRevocation} indicates that a revocation has occurred, this method
     * will generate notification events for the affected representatives based on which parties
     * have had their solicitors revoked.</p>
     *
     * <ul>
     *     <li>If the applicant's solicitor is revoked, an applicant notification event is created.</li>
     *     <li>If the respondent's solicitor is revoked, a respondent notification event is created.</li>
     * </ul>
     *
     * @param litigantRevocation contains information about which representatives have been revoked
     * @param info additional context required to build the notification events
     * @return a list of {@link SendCorrespondenceEventWithDescription} representing the notifications
     *         to be sent; returns an empty list if no revocations occurred
     */
    public List<SendCorrespondenceEventWithDescription> prepareRepresentativeRevocationNotificationEvent(
        LitigantRevocation litigantRevocation, StopRepresentingClientInfo info) {
        List<SendCorrespondenceEventWithDescription> eventsWithDesc = new ArrayList<>();
        if (litigantRevocation.wasRevoked()) {
            if (litigantRevocation.applicantSolicitorRevoked()) {
                eventsWithDesc.add(prepareApplicantSolicitorEmailNotificationEvent(info));
            }
            if (litigantRevocation.respondentSolicitorRevoked()) {
                eventsWithDesc.add(prepareRespondentSolicitorEmailNotificationEvent(info));
            }
        }
        return eventsWithDesc;
    }

    /**
     * Prepares a list of {@link SendCorrespondenceEventWithDescription} for sending letter
     * notifications to litigants (applicant or respondent) whose representation has been revoked.
     *
     * <p>This method only constructs the correspondence events with description for letter
     * notifications. The letters are not sent directly; the events with description will be processed
     * later in the workflow to trigger the actual notifications.</p>
     *
     * @param litigantRevocation flags indicating which litigants' representation was revoked
     * @param info the stop representing client event information
     * @return a list of {@link SendCorrespondenceEventWithDescription} for later letter notification processing
     */
    public List<SendCorrespondenceEventWithDescription> prepareLitigantRevocationLetterNotificationEvents(LitigantRevocation litigantRevocation,
                                                                                                          StopRepresentingClientInfo info) {
        List<SendCorrespondenceEventWithDescription> events = new ArrayList<>();
        if (litigantRevocation.wasRevoked()) {
            if (litigantRevocation.applicantSolicitorRevoked()) {
                events.add(prepareApplicantLetterNotificationEvent(info));
            }
            if (litigantRevocation.respondentSolicitorRevoked()) {
                events.add(prepareRespondentLetterNotificationEvent(info));
            }
        }
        return events;
    }

    /**
     * Prepares a {@link SendCorrespondenceEventWithDescription} for sending an email notification
     * to the applicant's barrister when representation has stopped.
     *
     * <p>This method does not send the email directly. Instead, it constructs a
     * correspondence event containing the notification details and template
     * required to notify the former applicant barrister. The event will be processed
     * later in the correspondence workflow to trigger the actual email notification.</p>
     *
     * @param info the stop representing client event information
     * @param barrister the applicant barrister who should receive the notification
     * @return a populated {@link SendCorrespondenceEventWithDescription} for later email notification processing
     */
    public SendCorrespondenceEventWithDescription prepareApplicantBarristerEmailNotificationEvent(
        StopRepresentingClientInfo info, Barrister barrister) {

        return prepareRepresentativeEmailNotificationEvent(
            "notifying applicant barrister",
            info,
            List.of(FORMER_APPLICANT_BARRISTER_ONLY),
            getNotifyApplicantRepresentativeTemplateName(info.getFinremCaseData()),
            finremNotificationRequestMapper
                .getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(), barrister),
            barrister
        );
    }

    /**
     * Prepares a {@link SendCorrespondenceEventWithDescription} for sending an email notification
     * to the respondent's barrister when representation has stopped.
     *
     * <p>This method does not send the email directly. Instead, it constructs a
     * correspondence event containing the notification details and template
     * required to notify the former respondent barrister. The event will be processed
     * later in the correspondence workflow to trigger the actual email notification.</p>
     *
     * @param info the stop representing client event information
     * @param barrister the respondent barrister who should receive the notification
     * @return a populated {@link SendCorrespondenceEventWithDescription} for later email notification processing
     */
    public SendCorrespondenceEventWithDescription prepareRespondentBarristerEmailNotificationEvent(StopRepresentingClientInfo info,
                                                                                                   Barrister barrister) {
        return prepareRepresentativeEmailNotificationEvent(
            "notifying respondent barrister",
            info,
            List.of(FORMER_RESPONDENT_BARRISTER_ONLY),
            getNotifyRespondentRepresentativeTemplateName(info.getFinremCaseData()),
            finremNotificationRequestMapper
                .getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(), barrister),
            barrister
        );
    }

    /**
     * Prepares a {@link SendCorrespondenceEventWithDescription} for sending an email notification
     * to an intervener's solicitor when representation has stopped.
     *
     * <p>This method does not send the email directly. Instead, it constructs a
     * correspondence event with description containing the required notification details.
     * The event with description will be processed later in the correspondence workflow to trigger
     * the actual email notification.</p>
     *
     * @param info the stop representing client event information
     * @param intervenerType the intervener whose solicitor should receive the notification
     * @return a populated {@link SendCorrespondenceEventWithDescription} for later email notification processing
     */
    public SendCorrespondenceEventWithDescription prepareIntervenerSolicitorEmailNotificationEvent(StopRepresentingClientInfo info,
                                                                                                   IntervenerType intervenerType) {
        return prepareRepresentativeEmailNotificationEvent(
            "notifying %s solicitor".formatted(intervenerType.getTypeValue()),
            info,
            List.of(getFormerIntervenerSolicitor(intervenerType)),
            getNotifyIntervenerRepresentativeTemplateName(info.getFinremCaseData()),
            finremNotificationRequestMapper
                .getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(),
                    CaseRole.getIntervenerSolicitorByIndex(intervenerType.getIntervenerId()), intervenerType)
        );
    }

    /**
     * Prepares a {@link SendCorrespondenceEventWithDescription} for sending an email notification
     * to an intervener's barrister when representation has stopped.
     *
     * <p>This method does not send the email directly. Instead, it constructs a
     * correspondence event containing the notification details and template
     * required to notify the former intervener barrister. The event will be processed
     * later in the correspondence workflow to trigger the actual email notification.</p>
     *
     * @param info the stop representing client event information
     * @param intervenerType the intervener whose barrister should be notified
     * @param barrister the intervener barrister who should receive the notification
     * @return a populated {@link SendCorrespondenceEventWithDescription} for later email notification processing
     */
    public SendCorrespondenceEventWithDescription prepareIntervenerBarristerEmailNotificationEvent(StopRepresentingClientInfo info,
                                                                                                   IntervenerType intervenerType,
                                                                                                   Barrister barrister) {
        return prepareRepresentativeEmailNotificationEvent(
            "notifying %s barrister".formatted(intervenerType.getTypeValue()),
            info,
            List.of(getFormerIntervenerBarrister(intervenerType)),
            getNotifyIntervenerRepresentativeTemplateName(info.getFinremCaseData()),
            finremNotificationRequestMapper
                .getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(), barrister, intervenerType),
            barrister
        );
    }

    /**
     * Internal helper method to generate a "Stop Representing" letter for a given recipient.
     *
     * <p>Builds a {@link CaseDocument} using the provided template and recipient details.
     * The filename includes a timestamp to ensure uniqueness. The actual PDF generation
     * is handled by {@link GenericDocumentService#generateDocumentFromPlaceholdersMap(String, Map, String, String, CaseType)}.</p>
     *
     * @param finremCaseDetails the case details used to populate the letter
     * @param authorisationToken the user's authorisation token for document generation
     * @param recipient the recipient of the letter (e.g., applicant or respondent)
     * @param filenamePrefix the prefix for the generated PDF filename
     * @param template the document template identifier to use
     * @return a {@link CaseDocument} containing the generated "Stop Representing" letter
     */

    private CaseDocument generateStopRepresentingLetter(FinremCaseDetails finremCaseDetails,
                                                        String authorisationToken,
                                                        DocumentHelper.PaperNotificationRecipient recipient,
                                                        String filenamePrefix,
                                                        String template) {
        Map<String, Object> documentDataMap =
            letterDetailsMapper.getLetterDetailsAsMap(finremCaseDetails, recipient);

        String documentFilename = format("%s_%s.pdf",
            filenamePrefix,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        );

        return genericDocumentService.generateDocumentFromPlaceholdersMap(
            authorisationToken,
            documentDataMap,
            template,
            documentFilename,
            finremCaseDetails.getCaseType()
        );
    }

    private SendCorrespondenceEventWithDescription prepareRepresentativeEmailNotificationEvent(String description,
                                                                                               StopRepresentingClientInfo info,
                                                                                               List<NotificationParty> parties,
                                                                                               EmailTemplateNames emailTemplate,
                                                                                               NotificationRequest notificationRequest) {
        return prepareRepresentativeEmailNotificationEvent(description, info, parties, emailTemplate, notificationRequest, null);
    }

    private SendCorrespondenceEventWithDescription prepareRepresentativeEmailNotificationEvent(String description,
                                                                                               StopRepresentingClientInfo info,
                                                                                               List<NotificationParty> parties,
                                                                                               EmailTemplateNames emailTemplate,
                                                                                               NotificationRequest notificationRequest,
                                                                                               Barrister barrister) {
        String userAuthorisation = info.getUserAuthorisation();

        return SendCorrespondenceEventWithDescription.builder()
            .description(description)
            .event(SendCorrespondenceEvent.builder()
                .notificationParties(parties)
                .emailNotificationRequest(notificationRequest)
                .emailTemplate(emailTemplate)
                .caseDetails(info.getCaseDetails())
                .caseDetailsBefore(info.getCaseDetailsBefore())
                .authToken(userAuthorisation)
                .barrister(barrister)
                .build()
            )
            .build();
    }

    /**
     * Prepares a {@link SendCorrespondenceEventWithDescription} for sending an email notification
     * to the applicant's solicitor when representation has stopped.
     *
     * <p>This method does not send the email directly. Instead, it constructs a
     * correspondence event containing the notification details and template
     * required to notify the former applicant solicitor. The event will be processed
     * later in the correspondence workflow to trigger the actual email notification.</p>
     *
     * @param info the stop representing client event information
     * @return a populated {@link SendCorrespondenceEventWithDescription} for later email notification processing
     */
    private SendCorrespondenceEventWithDescription prepareApplicantSolicitorEmailNotificationEvent(StopRepresentingClientInfo info) {
        return prepareRepresentativeEmailNotificationEvent(
            "notifying applicant solicitor",
            info,
            List.of(FORMER_APPLICANT_SOLICITOR_ONLY),
            getNotifyApplicantRepresentativeTemplateName(info.getFinremCaseData()),
            finremNotificationRequestMapper
                .getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(), APP_SOLICITOR)
        );
    }

    /**
     * Prepares a {@link SendCorrespondenceEventWithDescription} for generating a letter notification
     * to a specific party when representation has stopped.
     *
     * <p>This method does not send the notification directly. Instead, it constructs the
     * correspondence event with description containing the party, case details, and generated
     * document to be posted. The event with description will be processed later in the correspondence
     * workflow to trigger the actual letter notification.</p>
     *
     * @param description a description of the correspondence event
     * @param info the stop representing client event information containing case details and authorisation
     * @param notificationParty the party who should receive the notification
     * @param documentGenerator function used to generate the document to be posted
     * @return a populated {@link SendCorrespondenceEventWithDescription} for later processing
     */
    private SendCorrespondenceEventWithDescription preparePartyLetterNotificationEvent(
        String description, StopRepresentingClientInfo info,
        NotificationParty notificationParty,
        Function<StopRepresentingClientInfo, CaseDocument> documentGenerator) {

        return SendCorrespondenceEventWithDescription.builder()
            .description(description)
            .event(SendCorrespondenceEvent.builder()
                .letterNotificationOnly(true)
                .notificationParties(List.of(notificationParty))
                .caseDetails(info.getCaseDetails())
                .caseDetailsBefore(info.getCaseDetailsBefore())
                .authToken(info.getUserAuthorisation())
                .documentsToPost(List.of(documentGenerator.apply(info)))
                .build()
            ).build();
    }

    /**
     * Prepares a {@link SendCorrespondenceEventWithDescription} for sending a stop representing
     * letter notification to the applicant.
     *
     * <p>The generated event with description includes the applicant as the notification party and
     * attaches the stop representing applicant letter. The correspondence event will
     * be processed later in the workflow to generate and send the letter.</p>
     *
     * @param info the stop representing client event information
     * @return a populated {@link SendCorrespondenceEventWithDescription} for applicant notification
     */
    private SendCorrespondenceEventWithDescription prepareApplicantLetterNotificationEvent(StopRepresentingClientInfo info) {
        return preparePartyLetterNotificationEvent(
            "notifying applicant",
            info,
            NotificationParty.APPLICANT,
            i -> generateStopRepresentingApplicantLetter(i.getCaseDetails(), i.getUserAuthorisation())
        );
    }

    /**
     * Prepares a {@link SendCorrespondenceEventWithDescription} for sending a stop representing
     * letter notification to the respondent.
     *
     * <p>The generated event with description includes the respondent as the notification party and
     * attaches the stop representing respondent letter. The correspondence event will
     * be processed later in the workflow to generate and send the letter.</p>
     *
     * @param info the stop representing client event information
     * @return a populated {@link SendCorrespondenceEventWithDescription} for respondent notification
     */
    private SendCorrespondenceEventWithDescription prepareRespondentLetterNotificationEvent(StopRepresentingClientInfo info) {
        return preparePartyLetterNotificationEvent(
            "notifying respondent",
            info,
            NotificationParty.RESPONDENT,
            i -> generateStopRepresentingRespondentLetter(i.getCaseDetails(), i.getUserAuthorisation())
        );
    }

    /**
     * Prepares a {@link SendCorrespondenceEventWithDescription} for sending an email notification
     * to the respondent's solicitor when representation has stopped.
     *
     * <p>This method does not send the email directly. Instead, it constructs a
     * correspondence event containing the notification details and template
     * required to notify the former respondent solicitor. The event will be processed
     * later in the correspondence workflow to trigger the actual email notification.</p>
     *
     * @param info the stop representing client event information
     * @return a populated {@link SendCorrespondenceEventWithDescription} for later email notification processing
     */
    private SendCorrespondenceEventWithDescription prepareRespondentSolicitorEmailNotificationEvent(StopRepresentingClientInfo info) {
        return prepareRepresentativeEmailNotificationEvent(
            "notifying respondent solicitor",
            info,
            List.of(FORMER_RESPONDENT_SOLICITOR_ONLY),
            getNotifyRespondentRepresentativeTemplateName(info.getFinremCaseData()),
            finremNotificationRequestMapper
                .getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(), RESP_SOLICITOR)
        );
    }
}
