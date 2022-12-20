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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.SingleLetterOrEmailRespondentCorresponder;

@Component
@Slf4j
public class AssignToJudgeRespondentCorresponder extends SingleLetterOrEmailRespondentCorresponder {

    private final AssignedToJudgeDocumentService assignedToJudgeDocumentService;

    @Autowired
    public AssignToJudgeRespondentCorresponder(NotificationService notificationService, BulkPrintService bulkPrintService,
                                               AssignedToJudgeDocumentService assignedToJudgeDocumentService) {
        super(notificationService, bulkPrintService);
        this.assignedToJudgeDocumentService = assignedToJudgeDocumentService;
    }


    @Override
    protected void emailSolicitor(CaseDetails caseDetails) {
        notificationService.sendAssignToJudgeConfirmationEmailToRespondentSolicitor(caseDetails);
    }

    @Override
    public CaseDocument getDocumentToPrint(CaseDetails caseDetails, String authorisationToken, DocumentHelper.PaperNotificationRecipient recipient) {
        return assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(
            caseDetails, authorisationToken, recipient);
    }
}
