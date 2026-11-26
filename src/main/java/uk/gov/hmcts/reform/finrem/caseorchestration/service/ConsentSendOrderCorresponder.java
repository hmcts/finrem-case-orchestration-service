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
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.RESPONDENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsentSendOrderCorresponder {

    private final ConsentOrderPrintService consentOrderPrintService;
    private final FinremNotificationRequestMapper finremNotificationRequestMapper;

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
        CaseDocument applicantCoverLetter = caseData.getConsentOrderApprovedCoverLetterApp();
        CaseDocument respondentCoverLetter = caseData.getConsentOrderApprovedCoverLetterResp();

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
}
