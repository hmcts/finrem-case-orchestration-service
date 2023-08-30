package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.generalapplication.service.RejectGeneralApplicationDocumentService;

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

    private final AssignedToJudgeDocumentService assignedToJudgeDocumentService;
    private final RejectGeneralApplicationDocumentService rejectGeneralApplicationDocumentService;
    private final BulkPrintService bulkPrintService;
    private final CaseDataService caseDataService;

    @SuppressWarnings("java:S1874")
    public void printAssignToJudgeNotification(CaseDetails caseDetails, String authToken) {
        log.info("Sending AssignedToJudge notification letter for bulk print for Case ID: {}", caseDetails.getId());

        Map<String, Object> caseData = caseDetails.getData();
        if (caseDataService.isPaperApplication(caseData)) {
            // Generate PDF notification letter
            CaseDocument assignedToJudgeNotificationLetter = assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(
                caseDetails, authToken, APPLICANT);

            // Send notification letter to Bulk Print
            bulkPrintService.sendDocumentForPrint(assignedToJudgeNotificationLetter, caseDetails, CCDConfigConstant.APPLICANT, authToken);
            log.info("Applicant notification letter sent to Bulk Print: {} for Case ID: {}", assignedToJudgeNotificationLetter,
                caseDetails.getId());
        }

        if (shouldPrintNotificationForRespondentSolicitor(caseDetails)) {
            UUID respondentLetterId = bulkPrintService.sendDocumentForPrint(
                assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(caseDetails, authToken, RESPONDENT),
                caseDetails, CCDConfigConstant.RESPONDENT, authToken);
            log.info("Respondent notification letter sent to Bulk Print: {} for Case ID: {}", respondentLetterId, caseDetails.getId());
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

    @SuppressWarnings("java:S1874")
    public void printApplicantRejectionGeneralApplication(CaseDetails caseDetails, String authToken) {
        CaseDocument applicantGeneralApplicationRejectDoc = rejectGeneralApplicationDocumentService.generateGeneralApplicationRejectionLetter(
            caseDetails, authToken, APPLICANT);
        bulkPrintService.sendDocumentForPrint(applicantGeneralApplicationRejectDoc, caseDetails, CCDConfigConstant.APPLICANT, authToken);
    }

    @SuppressWarnings("java:S1874")
    public void printRespondentRejectionGeneralApplication(CaseDetails caseDetails, String authToken) {
        CaseDocument applicantGeneralApplicationRejectDoc = rejectGeneralApplicationDocumentService.generateGeneralApplicationRejectionLetter(
            caseDetails, authToken, RESPONDENT);
        bulkPrintService.sendDocumentForPrint(applicantGeneralApplicationRejectDoc, caseDetails, CCDConfigConstant.RESPONDENT, authToken);
    }

    public void printIntervenerRejectionGeneralApplication(CaseDetails caseDetails, IntervenerWrapper intervenerWrapper, String authToken) {
        CaseDocument applicantGeneralApplicationRejectDoc = rejectGeneralApplicationDocumentService.generateGeneralApplicationRejectionLetter(
            caseDetails, authToken, DocumentHelper.getIntervenerPaperNotificationRecipient(intervenerWrapper));
        bulkPrintService.sendDocumentForPrint(applicantGeneralApplicationRejectDoc, caseDetails,
            intervenerWrapper.getIntervenerType().getTypeValue(), authToken);
    }
}
