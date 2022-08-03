package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.service.UpdateFrcInfoApplicantDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.service.UpdateFrcInfoRespondentDocumentService;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaperNotificationService {

    private final HelpWithFeesDocumentService helpWithFeesDocumentService;
    private final AssignedToJudgeDocumentService assignedToJudgeDocumentService;
    private final ManualPaymentDocumentService manualPaymentDocumentService;
    private final UpdateFrcInfoApplicantDocumentService updateFrcInfoApplicantDocumentService;
    private final UpdateFrcInfoRespondentDocumentService updateFrcInfoRespondentDocumentService;
    private final BulkPrintService bulkPrintService;

    public void printHwfSuccessfulNotification(FinremCaseDetails caseDetails, String authToken) {
        log.info("Sending Consented HWF Successful notification letter for bulk print");

        FinremCaseData caseData = caseDetails.getCaseData();
        if (caseData.isPaperCase()) {
            log.info("Case is paper application");

            // Generate PDF notification letter
            Document hwfSuccessfulNotificationLetter = helpWithFeesDocumentService.generateHwfSuccessfulNotificationLetter(
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

    public void printAssignToJudgeNotification(FinremCaseDetails caseDetails, String authToken) {
        log.info("Sending AssignedToJudge notification letter for bulk print for Case ID: {}", caseDetails.getId());

        FinremCaseData caseData = caseDetails.getCaseData();
        if (caseData.isPaperCase()) {
            // Generate PDF notification letter
            Document assignedToJudgeNotificationLetter = assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(
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

    public void printConsentInContestedAssignToJudgeConfirmationNotification(FinremCaseDetails caseDetails, String authToken) {
        if (shouldPrintForApplicant(caseDetails)) {
            log.info("Sending applicant Consent in Contested AssignedToJudge notification letter for bulk print for Case ID: {}",
                caseDetails.getId());

            Document applicantAssignedToJudgeNotificationLetter =
                assignedToJudgeDocumentService.generateConsentInContestedAssignedToJudgeNotificationLetter(caseDetails, authToken, APPLICANT);
            bulkPrintService.sendDocumentForPrint(applicantAssignedToJudgeNotificationLetter, caseDetails);
        }


        if (shouldPrintForRespondent(caseDetails)) {
            log.info("Sending respondent Consent in Contested AssignedToJudge notification letter for bulk print for Case ID: {}",
                caseDetails.getId());

            Document respondentAssignedToJudgeNotificationLetter =
                assignedToJudgeDocumentService.generateConsentInContestedAssignedToJudgeNotificationLetter(caseDetails, authToken, RESPONDENT);
            bulkPrintService.sendDocumentForPrint(respondentAssignedToJudgeNotificationLetter, caseDetails);
        }
    }

    public void printManualPaymentNotification(FinremCaseDetails caseDetails, String authToken) {
        if (caseDetails.getCaseData().isContestedPaperApplication()) {
            Document applicantManualPaymentLetter = manualPaymentDocumentService.generateManualPaymentLetter(caseDetails, authToken, APPLICANT);
            bulkPrintService.sendDocumentForPrint(applicantManualPaymentLetter, caseDetails);
        }

        if (caseDetails.getCaseData().isContestedApplication() && shouldPrintNotificationForRespondentSolicitor(caseDetails)) {
            Document respondentManualPaymentLetter = manualPaymentDocumentService.generateManualPaymentLetter(caseDetails, authToken, RESPONDENT);
            bulkPrintService.sendDocumentForPrint(respondentManualPaymentLetter, caseDetails);
        }
    }

    public void printUpdateFrcInformationNotification(FinremCaseDetails caseDetails, String authToken) {
        printApplicantUpdateFrcInfoNotification(caseDetails, authToken);
        printRespondentUpdateFrcInfoNotification(caseDetails, authToken);
    }

    private void printApplicantUpdateFrcInfoNotification(FinremCaseDetails caseDetails, String authToken) {
        Optional<Document> applicantLetter = updateFrcInfoApplicantDocumentService.getUpdateFrcInfoLetter(caseDetails,
            authToken);
        applicantLetter.ifPresent(letter -> bulkPrintService.sendDocumentForPrint(letter, caseDetails));
    }

    private void printRespondentUpdateFrcInfoNotification(FinremCaseDetails caseDetails, String authToken) {
        Optional<Document> respondentLetter = updateFrcInfoRespondentDocumentService.getUpdateFrcInfoLetter(caseDetails,
            authToken);
        respondentLetter.ifPresent(letter -> bulkPrintService.sendDocumentForPrint(letter, caseDetails));
    }

    public boolean shouldPrintForApplicant(FinremCaseDetails caseDetails) {
        return !caseDetails.getCaseData().isApplicantRepresentedByASolicitor()
            || !caseDetails.getCaseData().isApplicantSolicitorAgreeToReceiveEmails()
            || caseDetails.getCaseData().isPaperCase();
    }

    public boolean shouldPrintForRespondent(FinremCaseDetails caseDetails) {
        return !caseDetails.getCaseData().isRespondentRepresentedByASolicitor()
            || !caseDetails.getCaseData().isRespondentSolicitorAgreeToReceiveEmails();
    }

    private boolean shouldPrintNotificationForRespondentSolicitor(FinremCaseDetails caseDetails) {
        return caseDetails.getCaseData().isRespondentRepresentedByASolicitor()
            && !caseDetails.getCaseData().isRespondentSolicitorAgreeToReceiveEmails();
    }
}
