package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudgeconsentincontested;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremSingleLetterOrEmailAllPartiesCorresponder;


@Component
@Slf4j
public class FinremAssignToJudgeConsentInContestedCorresponder extends FinremSingleLetterOrEmailAllPartiesCorresponder {
    private final AssignedToJudgeDocumentService assignedToJudgeDocumentService;

    @Autowired
    public FinremAssignToJudgeConsentInContestedCorresponder(NotificationService notificationService,
                                                             BulkPrintService bulkPrintService,
                                                             AssignedToJudgeDocumentService assignedToJudgeDocumentService) {
        super(notificationService, bulkPrintService);
        this.assignedToJudgeDocumentService = assignedToJudgeDocumentService;
    }

    @Override
    public CaseDocument getDocumentToPrint(FinremCaseDetails caseDetails, String authorisationToken,
                                           DocumentHelper.PaperNotificationRecipient recipient) {
        return assignedToJudgeDocumentService.generateConsentInContestedAssignedToJudgeNotificationLetter(
            caseDetails, authorisationToken, recipient);
    }

    @Override
    protected void emailApplicantSolicitor(FinremCaseDetails caseDetails) {
        log.info("Not sending email correspondence to applicant for case: {}", caseDetails.getId());
    }

    @Override
    protected void emailRespondentSolicitor(FinremCaseDetails caseDetails) {
        log.info("Not sending email correspondence to Respondent for case: {}", caseDetails.getId());
    }

    @Override
    protected void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, FinremCaseDetails caseDetails) {
        log.info("Not sending email correspondence to Intervener for case: {}", caseDetails.getId());
    }

}
