package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.CaseDetailsSingleLetterOrEmailAllPartiesCorresponder;


@Component
@Slf4j
public class AssignToJudgeCorresponder extends CaseDetailsSingleLetterOrEmailAllPartiesCorresponder {

    private final AssignedToJudgeDocumentService assignedToJudgeDocumentService;

    @Autowired
    public AssignToJudgeCorresponder(NotificationService notificationService,
                                     BulkPrintService bulkPrintService,
                                     FinremCaseDetailsMapper finremCaseDetailsMapper,
                                     AssignedToJudgeDocumentService assignedToJudgeDocumentService) {
        super(notificationService, bulkPrintService, finremCaseDetailsMapper);
        this.assignedToJudgeDocumentService = assignedToJudgeDocumentService;
    }

    @Override
    public CaseDocument getDocumentToPrint(CaseDetails caseDetails, String authorisationToken, DocumentHelper.PaperNotificationRecipient recipient) {
        return assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(
            caseDetails, authorisationToken, recipient);
    }

    @Override
    protected void emailApplicantSolicitor(CaseDetails caseDetails) {
        notificationService.sendAssignToJudgeConfirmationEmailToApplicantSolicitor(caseDetails);
    }

    @Override
    protected void emailRespondentSolicitor(CaseDetails caseDetails) {
        notificationService.sendAssignToJudgeConfirmationEmailToRespondentSolicitor(caseDetails);
    }

    @Override
    protected void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, CaseDetails caseDetails) {
        notificationService.sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(caseDetails,
            notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerWrapper));
    }
}
