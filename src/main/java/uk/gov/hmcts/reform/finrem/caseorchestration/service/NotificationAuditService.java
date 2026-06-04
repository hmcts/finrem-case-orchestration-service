package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCaseCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.HearingLike;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationAudit;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationAuditCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationToBeSentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.NotificationAuditWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService.FINANCIAL_REMEDY_PACK_LETTER_TYPE;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationAuditService {

    private final NotificationService notificationService;

    /**
     * Creates audit rows for a new-hearing correspondence: one row per party listed on the hearing.
     * Appends both the rows and their IDs to the case wrapper.
     *
     * @param caseDetails     the case
     * @param hearing         the hearing whose parties drive the recipient list
     * @param eventType       the event triggering the notification (used for the {@code event_id} field)
     * @param emailTemplate   the email template that the listeners will use
     * @param documentsToPost the documents that will be posted to any unrepresented parties
     */
    public void createAuditsForHearingCorrespondence(FinremCaseDetails caseDetails,
                                                     HearingLike hearing,
                                                     EventType eventType,
                                                     EmailTemplateNames emailTemplate,
                                                     List<CaseDocument> documentsToPost) {
        createAuditsForCorrespondence(caseDetails, hearing, eventType, emailTemplate, documentsToPost);
    }

    /**
     * Creates audit rows for a vacate/adjourn correspondence.
     */
    public void createAuditsForVacateCorrespondence(FinremCaseDetails caseDetails,
                                                    HearingLike vacateOrAdjournedHearing,
                                                    EventType eventType,
                                                    EmailTemplateNames emailTemplate,
                                                    List<CaseDocument> documentsToPost) {
        createAuditsForCorrespondence(caseDetails, vacateOrAdjournedHearing, eventType, emailTemplate, documentsToPost);
    }

    private void createAuditsForCorrespondence(FinremCaseDetails caseDetails,
                                               HearingLike hearing,
                                               EventType eventType,
                                               EmailTemplateNames emailTemplate,
                                               List<CaseDocument> documentsToPost) {
        FinremCaseData caseData = caseDetails.getData();
        NotificationAuditWrapper wrapper = caseData.getNotificationAuditWrapper();

        List<NotificationAuditCollectionItem> audits = Optional.ofNullable(wrapper.getNotificationsAudits())
            .orElseGet(ArrayList::new);
        List<NotificationToBeSentCollectionItem> pending = Optional.ofNullable(wrapper.getNotificationsToBeSent())
            .orElseGet(ArrayList::new);

        List<PartyOnCaseCollectionItem> partiesOnCase =
            Optional.ofNullable(hearing.getPartiesOnCase()).orElseGet(List::of);

        List<String> postalDocFilenames = filenamesOf(documentsToPost);

        for (PartyOnCaseCollectionItem partyItem : partiesOnCase) {
            String role = partyItem.getValue().getRole();
            NotificationParty notificationParty = NotificationParty.getNotificationPartyFromRole(role);
            NotificationType channel = predictParty(caseDetails, notificationParty);

            NotificationAudit auditRow = buildAuditRow(eventType, role, channel, emailTemplate, postalDocFilenames);

            UUID rowId = UUID.randomUUID();
            audits.add(NotificationAuditCollectionItem.builder()
                .id(rowId)
                .value(auditRow)
                .build());
            pending.add(NotificationToBeSentCollectionItem.builder()
                .id(UUID.randomUUID())
                .value(rowId)
                .build());

            log.info("Created notification audit row {} for party {} on case {} (channel: {})",
                rowId, role, caseDetails.getId(), channel);
        }

        wrapper.setNotificationsAudits(audits);
        wrapper.setNotificationsToBeSent(pending);
    }

    private NotificationType predictParty(FinremCaseDetails caseDetails, NotificationParty party) {
        boolean digitalAndEmailPopulated = switch (party) {
            case APPLICANT, APPLICANT_SOLICITOR_ONLY ->
                notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
            case RESPONDENT ->
                notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails);
            case INTERVENER_ONE ->
                notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(
                    caseDetails.getData().getIntervenerOne(), caseDetails);
            case INTERVENER_TWO ->
                notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(
                    caseDetails.getData().getIntervenerTwo(), caseDetails);
            case INTERVENER_THREE ->
                notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(
                    caseDetails.getData().getIntervenerThree(), caseDetails);
            case INTERVENER_FOUR ->
                notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(
                    caseDetails.getData().getIntervenerFour(), caseDetails);
            default -> false;
        };

        return digitalAndEmailPopulated ? NotificationType.EMAIL : NotificationType.POSTAL;
    }

    private NotificationAudit buildAuditRow(EventType eventType,
                                            String partyRole,
                                            NotificationType channel,
                                            EmailTemplateNames emailTemplate,
                                            List<String> postalDocFilenames) {

        NotificationAudit.NotificationAuditBuilder builder = NotificationAudit.builder()
            .createdAt(LocalDate.now())
            .wasSent(YesOrNo.NO)
            .eventId(eventType.getCcdType())
            .party(partyRole)
            .type(channel);

        if (channel == NotificationType.EMAIL) {
            builder.emailTemplate(emailTemplate == null ? null : emailTemplate.name());
        } else {
            builder.letterTemplate(FINANCIAL_REMEDY_PACK_LETTER_TYPE);
            builder.attachedPostalDocs(postalDocFilenames);
        }

        return builder.build();
    }

    private List<String> filenamesOf(List<CaseDocument> documents) {
        return Optional.ofNullable(documents).orElseGet(List::of).stream()
            .map(CaseDocument::getDocumentFilename)
            .toList();
    }
}