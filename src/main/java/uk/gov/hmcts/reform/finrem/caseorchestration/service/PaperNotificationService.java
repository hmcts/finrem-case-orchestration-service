package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaperNotificationService {

    private final HelpWithFeesDocumentService helpWithFeesDocumentService;
    private final AssignedToJudgeDocumentService assignedToJudgeDocumentService;
    private final ManualPaymentDocumentService manualPaymentDocumentService;
    private final BulkPrintService bulkPrintService;
    private final CaseDataService caseDataService;

    public void printHwfSuccessfulNotification(CaseDetails caseDetails, String authToken) {
        log.info("Sending Consented HWF Successful notification letter for bulk print");

        Map<String, Object> caseData = caseDetails.getData();
        if (caseDataService.isPaperApplication(caseData)) {
            log.info("Case is paper application");

            // Generate PDF notification letter
            CaseDocument hwfSuccessfulNotificationLetter = helpWithFeesDocumentService.generateHwfSuccessfulNotificationLetter(
                caseDetails, authToken, APPLICANT);

            // Send notification letter to Bulk Print
            bulkPrintService.sendDocumentForPrint(hwfSuccessfulNotificationLetter, caseDetails);
            log.info("Applicant notification letter sent to Bulk Print: {} for Case ID: {}", hwfSuccessfulNotificationLetter,
                caseDetails.getId());
        }

        if (shouldPrintNotificationForRespondentSolicitor(caseDetails)) {
            UUID respondentLetterId = bulkPrintService.sendDocumentForPrint(
                helpWithFeesDocumentService.generateHwfSuccessfulNotificationLetter(caseDetails, authToken, RESPONDENT),
                caseDetails);
            log.info("Respondent notification letter sent to Bulk Print: {} for Case ID: {}", respondentLetterId, caseDetails.getId());
        }
    }

    public void printAssignToJudgeNotification(CaseDetails caseDetails, String authToken) {
        log.info("Sending AssignedToJudge notification letter for bulk print for Case ID: {}", caseDetails.getId());

        Map<String, Object> caseData = caseDetails.getData();
        if (caseDataService.isPaperApplication(caseData)) {
            // Generate PDF notification letter
            CaseDocument assignedToJudgeNotificationLetter = assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(
                caseDetails, authToken, APPLICANT);

            // Send notification letter to Bulk Print
            bulkPrintService.sendDocumentForPrint(assignedToJudgeNotificationLetter, caseDetails);
            log.info("Applicant notification letter sent to Bulk Print: {} for Case ID: {}", assignedToJudgeNotificationLetter,
                caseDetails.getId());
        }

        if (shouldPrintNotificationForRespondentSolicitor(caseDetails)) {
            UUID respondentLetterId = bulkPrintService.sendDocumentForPrint(
                assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(caseDetails, authToken, RESPONDENT),
                caseDetails);
            log.info("Respondent notification letter sent to Bulk Print: {} for Case ID: {}", respondentLetterId, caseDetails.getId());
        }
    }

    public void printConsentInContestedAssignToJudgeConfirmationNotification(CaseDetails caseDetails, String authToken) {
        if (shouldPrintForApplicant(caseDetails)) {
            log.info("Sending applicant Consent in Contested AssignedToJudge notification letter for bulk print for Case ID: {}",
                caseDetails.getId());

            CaseDocument applicantAssignedToJudgeNotificationLetter =
                assignedToJudgeDocumentService.generateConsentInContestedAssignedToJudgeNotificationLetter(caseDetails, authToken, APPLICANT);
            bulkPrintService.sendDocumentForPrint(applicantAssignedToJudgeNotificationLetter, caseDetails);
        }


        if (shouldPrintForRespondent(caseDetails)) {
            log.info("Sending respondent Consent in Contested AssignedToJudge notification letter for bulk print for Case ID: {}",
                caseDetails.getId());

            CaseDocument respondentAssignedToJudgeNotificationLetter =
                assignedToJudgeDocumentService.generateConsentInContestedAssignedToJudgeNotificationLetter(caseDetails, authToken, RESPONDENT);
            bulkPrintService.sendDocumentForPrint(respondentAssignedToJudgeNotificationLetter, caseDetails);
        }
    }

    public void printManualPaymentNotification(CaseDetails caseDetails, String authToken) {
        if (caseDataService.isContestedPaperApplication(caseDetails)) {
            CaseDocument applicantManualPaymentLetter = manualPaymentDocumentService.generateManualPaymentLetter(caseDetails, authToken, APPLICANT);
            bulkPrintService.sendDocumentForPrint(applicantManualPaymentLetter, caseDetails);
        }

        if (caseDataService.isContestedApplication(caseDetails) && shouldPrintNotificationForRespondentSolicitor(caseDetails)) {
            CaseDocument respondentManualPaymentLetter = manualPaymentDocumentService.generateManualPaymentLetter(caseDetails, authToken, RESPONDENT);
            bulkPrintService.sendDocumentForPrint(respondentManualPaymentLetter, caseDetails);
        }
    }

    public void printUpdateFrcInformationNotification(CaseDetails caseDetails, String authToken) {
        if (!caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            //send notification letter to app solicitor
        }
        if (!caseDataService.isRespondentSolicitorAgreeToReceiveEmails(caseDetails)) {
            //send notification letter to resp solicitor
        }
        if (!caseDataService.isApplicantRepresentedByASolicitor(caseDetails.getData())) {

        }
        if (!caseDataService.isRespondentRepresentedByASolicitor(caseDetails.getData())) {

        }
    }

    public boolean shouldPrintForApplicant(CaseDetails caseDetails) {
        return !caseDataService.isApplicantRepresentedByASolicitor(caseDetails.getData())
            || !caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)
            || caseDataService.isPaperApplication(caseDetails.getData());
    }

    public boolean shouldPrintForRespondent(CaseDetails caseDetails) {
        return !caseDataService.isRespondentRepresentedByASolicitor(caseDetails.getData())
            || !YES_VALUE.equalsIgnoreCase(nullToEmpty(caseDetails.getData().get(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT)));
    }

    private boolean shouldPrintNotificationForRespondentSolicitor(CaseDetails caseDetails) {
        return caseDataService.isRespondentRepresentedByASolicitor(caseDetails.getData())
            && !YES_VALUE.equalsIgnoreCase(nullToEmpty(caseDetails.getData().get(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT)));
    }
}
