package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_AVAILABLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_NOT_APPROVED_SENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.RESPONDENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsentSendOrderCorresponder {

    private final ConsentOrderPrintService consentOrderPrintService;
    private final FinremNotificationRequestMapper finremNotificationRequestMapper;
    private final ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;

    /**
     * Builds the applicant and respondent correspondence events for an approved consent order.
     * Each party is sent their cover letter followed by the order documents.
     *
     * @param caseDetails        the current case details holding the stored cover letters
     * @param caseDetailsBefore  the case details before the event
     * @param authorisationToken the authorisation token
     * @return the events to publish, ordered applicant then respondent
     */
    public List<SendCorrespondenceEvent> buildApprovedOrderEvents(FinremCaseDetails caseDetails,
                                                                  FinremCaseDetails caseDetailsBefore,
                                                                  String authorisationToken) {

        List<CaseDocument> orders = consentOrderPrintService.getApprovedOrderDocumentsToSend(caseDetails, caseDetailsBefore, authorisationToken);
        FinremCaseData caseData = caseDetails.getData();
        List<CaseDocument> applicantDocuments = new ArrayList<>();
        List<CaseDocument> respondentDocuments = new ArrayList<>();
        CaseDocument applicantCoverLetter = caseData.getConsentSendOrderWrapper().getConsentOrderApprovedCoverLetterApp();
        CaseDocument respondentCoverLetter = caseData.getConsentSendOrderWrapper().getConsentOrderApprovedCoverLetterResp();

        Optional.ofNullable(applicantCoverLetter).ifPresent(applicantDocuments::add);
        applicantDocuments.addAll(orders);

        Optional.ofNullable(respondentCoverLetter).ifPresent(respondentDocuments::add);
        respondentDocuments.addAll(orders);

        List<SendCorrespondenceEvent> events = new ArrayList<>();

        SendCorrespondenceEvent applicantEvent = SendCorrespondenceEvent.builder()
            .notificationParties(List.of(APPLICANT))
            .emailNotificationRequest(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails))
            .emailTemplate(FR_CONSENT_ORDER_AVAILABLE)
            .documentsToPost(applicantDocuments)
            .caseDetails(caseDetails)
            .authToken(authorisationToken)
            .build();
        events.add(applicantEvent);

        SendCorrespondenceEvent respondentEvent = SendCorrespondenceEvent.builder()
            .notificationParties(List.of(RESPONDENT))
            .emailNotificationRequest(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails))
            .emailTemplate(FR_CONSENT_ORDER_AVAILABLE)
            .documentsToPost(respondentDocuments)
            .caseDetails(caseDetails)
            .authToken(authorisationToken)
            .build();
        events.add(respondentEvent);

        return events;
    }

    /**
     * Builds the applicant and respondent correspondence events for a consent order that was not approved.
     * Each party is sent their cover letter followed by the order documents.
     * A party who receives post (has a stored cover letter) is skipped when there is no order to send,
     * so a cover letter is never posted on its own. Digital solicitors are always emailed.
     *
     * @param caseDetails        the current case details holding the stored cover letters
     * @param authorisationToken the authorisation token
     * @return the events to publish, ordered applicant then respondent
     */
    public List<SendCorrespondenceEvent> buildNotApprovedOrderEvents(FinremCaseDetails caseDetails,
                                                                     String authorisationToken) {

        List<CaseDocument> orders = consentOrderNotApprovedDocumentService.getNotApprovedConsentOrders(caseDetails, authorisationToken);
        FinremCaseData caseData = caseDetails.getData();
        CaseDocument applicantCoverLetter = caseData.getConsentSendOrderWrapper().getConsentOrderNotApprovedCoverLetterApp();
        CaseDocument respondentCoverLetter = caseData.getConsentSendOrderWrapper().getConsentOrderNotApprovedCoverLetterResp();

        List<SendCorrespondenceEvent> events = new ArrayList<>();

        if (applicantCoverLetter != null && orders.isEmpty()) {
            log.info("No not approved order to send, skipping applicant letter for Case ID: {}", caseDetails.getId());
        } else {
            List<CaseDocument> applicantDocuments = new ArrayList<>();
            if (applicantCoverLetter != null) {
                applicantDocuments.add(applicantCoverLetter);
            }
            applicantDocuments.addAll(orders);

            events.add(SendCorrespondenceEvent.builder()
                .notificationParties(List.of(APPLICANT))
                .emailNotificationRequest(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails))
                .emailTemplate(FR_CONSENT_ORDER_NOT_APPROVED_SENT)
                .documentsToPost(applicantDocuments)
                .caseDetails(caseDetails)
                .authToken(authorisationToken)
                .build());
        }

        if (respondentCoverLetter != null && orders.isEmpty()) {
            log.info("No not approved order to send, skipping respondent letter for Case ID: {}", caseDetails.getId());
        } else {
            List<CaseDocument> respondentDocuments = new ArrayList<>();
            if (respondentCoverLetter != null) {
                respondentDocuments.add(respondentCoverLetter);
            }
            respondentDocuments.addAll(orders);

            events.add(SendCorrespondenceEvent.builder()
                .notificationParties(List.of(RESPONDENT))
                .emailNotificationRequest(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails))
                .emailTemplate(FR_CONSENT_ORDER_NOT_APPROVED_SENT)
                .documentsToPost(respondentDocuments)
                .caseDetails(caseDetails)
                .authToken(authorisationToken)
                .build());
        }

        return events;
    }
}
