package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

/**
 * Corresponder for the "Assign to Judge" event in financial remedy cases.
 *
 * <p>
 * This class extends {@link AbstractAssignToJudgeCorresponder} and coordinates notifications to all relevant parties,
 * including generating letters for bulk print and sending emails to solicitors.
 * </p>
 *
 * <p>
 * From DFR-3877, this class now mirrors the behaviour of
 * {@link IssueApplicationConsentCorresponder} by overriding {@code shouldSendRespondentLetter}
 * to avoid sending letters to respondents who reside outside the UK.
 * </p>
 *
 * <p>
 * This is part of a broader effort to ensure international parties are not sent physical documents
 * where inappropriate or unnecessary.
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

    /**
     * Determines whether a paper letter should be sent to the respondent.
     * 
     * <p>
     * Letters will not be sent to respondents who are marked as residing outside the UK.
     * </p>
     *
     * @param caseDetails the case details
     * @return {@code true} if the respondent does not reside outside the UK; otherwise {@code false}
     */
    @Override
    protected boolean shouldSendRespondentLetter(FinremCaseDetails caseDetails) {
        return isNotInternationalParty(getContactDetailsWrapper(caseDetails).getRespondentResideOutsideUK());
    }
}
