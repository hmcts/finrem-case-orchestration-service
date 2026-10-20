package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_AVAILABLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.RESPONDENT;

/**
 * Builds the correspondence events for the consented approved Send Order event.
 * Letter packs are read from the case; they are prepared in about-to-submit.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsentOrderApprovedCorresponder {

    private final FinremNotificationRequestMapper finremNotificationRequestMapper;
    private final NotificationService notificationService;

    /**
     * Builds the applicant and respondent correspondence events using each party's stored letter pack.
     * No event is built for a party who would receive a letter but whose pack is empty.
     *
     * @param caseDetails       the case details holding the stored letter packs
     * @param userAuthorisation the authorisation token
     * @return the events to publish, ordered applicant then respondent
     */
    public List<SendCorrespondenceEvent> buildCorrespondenceEvents(FinremCaseDetails caseDetails, String userAuthorisation) {
        FinremCaseData caseData = caseDetails.getData();
        String caseId = caseDetails.getCaseIdAsString();
        List<SendCorrespondenceEvent> events = new ArrayList<>();

        List<CaseDocument> applicantDocuments = toCaseDocuments(caseData.getConsentOrderApplicantLetterPack());
        boolean applicantGetsLetter = !notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
        if (applicantGetsLetter && applicantDocuments.isEmpty()) {
            log.info("{} - Applicant letter pack is empty. Nothing sent to applicant.", caseId);
        } else {
            events.add(buildEvent(APPLICANT,
                finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails),
                applicantDocuments, caseDetails, userAuthorisation));
        }

        List<CaseDocument> respondentDocuments = toCaseDocuments(caseData.getConsentOrderRespondentLetterPack());
        boolean respondentGetsLetter = !notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails);
        if (respondentGetsLetter && respondentDocuments.isEmpty()) {
            log.info("{} - Respondent letter pack is empty. Nothing sent to respondent.", caseId);
        } else {
            events.add(buildEvent(RESPONDENT,
                finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails),
                respondentDocuments, caseDetails, userAuthorisation));
        }

        return events;
    }

    private SendCorrespondenceEvent buildEvent(NotificationParty party,
                                               NotificationRequest notificationRequest,
                                               List<CaseDocument> documentsToPost,
                                               FinremCaseDetails caseDetails,
                                               String userAuthorisation) {

        return SendCorrespondenceEvent.builder()
            .notificationParties(List.of(party))
            .emailNotificationRequest(notificationRequest)
            .emailTemplate(FR_CONSENT_ORDER_AVAILABLE)
            .documentsToPost(documentsToPost)
            .caseDetails(caseDetails)
            .authToken(userAuthorisation)
            .build();
    }

    private List<CaseDocument> toCaseDocuments(List<DocumentCollectionItem> pack) {
        return emptyIfNull(pack).stream()
            .map(DocumentCollectionItem::getValue)
            .filter(Objects::nonNull)
            .toList();
    }
}
