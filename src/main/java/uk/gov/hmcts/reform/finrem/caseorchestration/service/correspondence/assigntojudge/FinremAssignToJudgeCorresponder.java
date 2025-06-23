package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

/**
 * Corresponder for the "Assign to Judge" event in financial remedy cases.
 * <p>
 * This class extends {@link AbstractAssignToJudgeCorresponder} and inherits all default behaviour for sending
 * assign-to-judge notifications, including sending letters to all parties (applicant, respondent, and interveners),
 * and sending emails to solicitors where applicable.
 * </p>
 *
 * <p>
 * Unlike {@link IssueApplicationConsentCorresponder}, this implementation does not override any behaviour,
 * meaning it sends letters to all recipients regardless of whether they reside outside the UK.
 * </p>
 *
 * <p>
 * It is suitable for general use in events where international filtering is not required.
 * </p>
 *
 * @see AbstractAssignToJudgeCorresponder
 * @see IssueApplicationConsentCorresponder
 */
@Slf4j
@Component
public class FinremAssignToJudgeCorresponder extends AbstractAssignToJudgeCorresponder {

    @Autowired
    public FinremAssignToJudgeCorresponder(NotificationService notificationService,
                                           BulkPrintService bulkPrintService,
                                           AssignedToJudgeDocumentService assignedToJudgeDocumentService) {
        super(notificationService, bulkPrintService, assignedToJudgeDocumentService);
    }

}
