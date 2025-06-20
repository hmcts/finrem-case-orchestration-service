package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Slf4j
@Component
public class IssueApplicationConsentCorresponder extends AbstractAssignToJudgeCorresponder {

    public IssueApplicationConsentCorresponder(NotificationService notificationService,
                                               BulkPrintService bulkPrintService,
                                               AssignedToJudgeDocumentService assignedToJudgeDocumentService) {
        super(notificationService, bulkPrintService, assignedToJudgeDocumentService);
    }

    @Override
    protected boolean shouldSendRespondentLetter(FinremCaseDetails caseDetails) {
        return isNotInternationalParty(getContactDetailsWrapper(caseDetails).getRespondentResideOutsideUK());
    }
}
