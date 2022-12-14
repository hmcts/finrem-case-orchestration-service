package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.SingleLetterOrEmailAllLitigantsCorresponder;


@Component
@Slf4j
public class AssignToJudgeCorresponder extends SingleLetterOrEmailAllLitigantsCorresponder {

    private final AssignedToJudgeDocumentService assignedToJudgeDocumentService;

    @Autowired
    public AssignToJudgeCorresponder(NotificationService notificationService,
                                     BulkPrintService bulkPrintService,
                                     AssignedToJudgeDocumentService assignedToJudgeDocumentService) {
        super(notificationService, bulkPrintService);
        this.assignedToJudgeDocumentService = assignedToJudgeDocumentService;
    }

    @Override
    public CaseDocument getDocumentToPrint(CaseDetails caseDetails, DocumentHelper.PaperNotificationRecipient recipient, String authorisationToken) {
        return assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(
            caseDetails, authorisationToken, recipient);

    }

    @Override
    protected void emailApplicant(CaseDetails caseDetails) {
        notificationService.sendAssignToJudgeConfirmationEmailToApplicantSolicitor(caseDetails);
    }

    @Override
    protected void emailRespondent(CaseDetails caseDetails) {
        notificationService.sendAssignToJudgeConfirmationEmailToRespondentSolicitor(caseDetails);
    }
}
