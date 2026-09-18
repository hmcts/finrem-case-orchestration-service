package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

/**
 * Handles correspondence for the "Issue Application" event in consented cases.
 *
 * <p>
 * This corresponder extends {@link AbstractAssignToJudgeCorresponder} and customises the behaviour
 * to ensure that letters are not sent to respondents residing outside the UK.
 * </p>
 *
 * <p>
 * The rest of the logic for notifying applicant/respondent/intervener solicitors and generating
 * the assign-to-judge letter is inherited from the base class.
 * </p>
 *
 * <p>
 * This class specifically overrides {@code shouldSendRespondentLetter} to apply international filtering.
 * </p>
 *
 * @see AbstractAssignToJudgeCorresponder
 */
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
