package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public abstract class FinremSingleLetterOrEmailAllPartiesCorresponder extends EmailAndLettersCorresponderBase<FinremCaseDetails> {

    protected final NotificationService notificationService;

    protected final BulkPrintService bulkPrintService;

    @Deprecated
    public void sendCorrespondence(FinremCaseDetails caseDetails, String authToken) {
        sendApplicantCorrespondence(caseDetails, authToken);
        sendRespondentCorrespondence(caseDetails, authToken);
        if (caseDetails.isContestedApplication()) {
            sendIntervenerCorrespondence(caseDetails, authToken);
        }
    }

    public List<SendCorrespondenceEvent> buildSendCorrespondenceEvents(FinremCaseDetails finremCaseDetails,
                                                                       String authToken) {
        List<SendCorrespondenceEvent> events = new ArrayList<>();
        events.add(
            // replacing sendApplicantCorrespondence
            SendCorrespondenceEvent.builder()
                .caseDetails(finremCaseDetails)
                .notificationParties(List.of(NotificationParty.APPLICANT))
                .emailTemplate(getApplicantEmailTemplate())
                .emailNotificationRequest(getApplicantEmailNotificationRequest(finremCaseDetails))
                .documentsToPost(List.of(
                    getDocumentToPrint(finremCaseDetails, authToken, DocumentHelper.PaperNotificationRecipient.APPLICANT)
                ))
                .authToken(authToken)
                .build()
        );
        events.add(
            // replacing sendRespondentCorrespondence
            SendCorrespondenceEvent.builder()
                .caseDetails(finremCaseDetails)
                .notificationParties(List.of(NotificationParty.RESPONDENT))
                .emailTemplate(getRespondentEmailTemplate())
                .emailNotificationRequest(getRespondentEmailNotificationRequest(finremCaseDetails))
                .documentsToPost(List.of(
                    getDocumentToPrint(finremCaseDetails, authToken, DocumentHelper.PaperNotificationRecipient.RESPONDENT)
                ))
                .authToken(authToken)
                .build()
        );
        if (finremCaseDetails.isContestedApplication()) {
            // replacing sendIntervenerCorrespondence
            List<IntervenerWrapper> interveners = finremCaseDetails.getData().getInterveners();
            interveners.forEach(intervenerWrapper ->
                events.add(
                    SendCorrespondenceEvent.builder()
                        .caseDetails(finremCaseDetails)
                        .notificationParties(List.of(NotificationParty.getNotificationPartyFromRole(intervenerWrapper
                            .getIntervenerSolicitorCaseRole().name())))
                        .emailTemplate(getIntervenerEmailTemplate())
                        .emailNotificationRequest(getIntervenerEmailNotificationRequest(finremCaseDetails,
                            notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerWrapper)))
                        .documentsToPost(List.of(
                            getDocumentToPrint(finremCaseDetails, authToken, intervenerWrapper.getPaperNotificationRecipient())
                        ))
                        .authToken(authToken)
                        .build()
                )
            );
        }

        return events;
    }

    public abstract CaseDocument getDocumentToPrint(FinremCaseDetails caseDetails, String authorisationToken,
                                                    DocumentHelper.PaperNotificationRecipient recipient);

    @Deprecated
    protected void sendApplicantCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for Case ID: {}", caseDetails.getId());
            this.emailApplicantSolicitor(caseDetails);
        } else if (shouldSendApplicantLetter(caseDetails)) {
            log.info("Sending letter correspondence to applicant for Case ID: {}", caseDetails.getId());
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(
                    caseDetails,
                    authorisationToken,
                    DocumentHelper.PaperNotificationRecipient.APPLICANT), caseDetails, CCDConfigConstant.APPLICANT, authorisationToken);
        } else {
            log.info("Nothing is sent to applicant for Case ID: {}", caseDetails.getId());
        }
    }

    @Deprecated
    protected void sendRespondentCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendRespondentSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to respondent for Case ID: {}", caseDetails.getId());
            this.emailRespondentSolicitor(caseDetails);
        } else if (shouldSendRespondentLetter(caseDetails)) {
            log.info("Sending letter correspondence to respondent for Case ID: {}", caseDetails.getId());
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(
                    caseDetails,
                    authorisationToken,
                    DocumentHelper.PaperNotificationRecipient.RESPONDENT), caseDetails, CCDConfigConstant.RESPONDENT, authorisationToken);
        } else {
            log.info("Nothing is sent to respondent for Case ID: {}", caseDetails.getId());
        }
    }

    @Deprecated
    protected void sendIntervenerCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        FinremCaseData caseData = caseDetails.getData();
        List<IntervenerWrapper> interveners = caseData.getInterveners();
        interveners.forEach(intervenerWrapper -> {
            if (shouldSendIntervenerSolicitorEmail(intervenerWrapper, caseDetails)) {
                log.info("Sending email correspondence to {} for Case ID: {}",
                    intervenerWrapper.getIntervenerType().getTypeValue(),
                    caseDetails.getId());
                this.emailIntervenerSolicitor(intervenerWrapper, caseDetails);
            } else if (shouldSendIntervenerLetter(intervenerWrapper)) {
                log.info("Sending letter correspondence to {} for Case ID: {}",
                    intervenerWrapper.getIntervenerType().getTypeValue(),
                    caseDetails.getId());
                bulkPrintService.sendDocumentForPrint(
                    getDocumentToPrint(
                        caseDetails,
                        authorisationToken,
                        intervenerWrapper.getPaperNotificationRecipient()), caseDetails,
                    intervenerWrapper.getIntervenerType().getTypeValue(), authorisationToken);
            } else {
                log.info("Nothing is sent to intervener for Case ID: {}", caseDetails.getId());
            }
        });
    }

    protected boolean shouldSendApplicantSolicitorEmail(FinremCaseDetails caseDetails) {
        return notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    protected boolean shouldSendRespondentSolicitorEmail(FinremCaseDetails caseDetails) {
        return notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    protected boolean shouldSendIntervenerSolicitorEmail(IntervenerWrapper intervenerWrapper, FinremCaseDetails caseDetails) {
        return notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(intervenerWrapper, caseDetails);
    }

    protected boolean shouldSendApplicantLetter(FinremCaseDetails caseDetails) {
        return true;
    }

    protected boolean shouldSendRespondentLetter(FinremCaseDetails caseDetails) {
        return true;
    }

    protected abstract boolean shouldSendIntervenerLetter(IntervenerWrapper intervenerWrapper);

    protected abstract void emailApplicantSolicitor(FinremCaseDetails caseDetails);

    protected abstract void emailRespondentSolicitor(FinremCaseDetails caseDetails);

    protected abstract void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, FinremCaseDetails caseDetails);

    protected EmailTemplateNames getApplicantEmailTemplate() {
        return null;
    }

    protected EmailTemplateNames getRespondentEmailTemplate() {
        return null;
    }

    protected EmailTemplateNames getIntervenerEmailTemplate() {
        return null;
    }

    protected NotificationRequest getApplicantEmailNotificationRequest(FinremCaseDetails caseDetails) {
        return null;
    }

    protected NotificationRequest getRespondentEmailNotificationRequest(FinremCaseDetails caseDetails) {
        return null;
    }

    protected NotificationRequest getIntervenerEmailNotificationRequest(FinremCaseDetails caseDetails,
                                                                        SolicitorCaseDataKeysWrapper dataKeysWrapper) {
        return null;
    }
}
