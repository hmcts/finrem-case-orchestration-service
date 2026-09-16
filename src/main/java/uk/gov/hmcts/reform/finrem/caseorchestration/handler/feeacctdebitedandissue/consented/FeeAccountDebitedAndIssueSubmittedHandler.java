package uk.gov.hmcts.reform.finrem.caseorchestration.handler.feeacctdebitedandissue.consented;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.consented.AbstractIssueApplicationSubmittedHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.IssueApplicationConsentCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

@Slf4j
@Service
public class FeeAccountDebitedAndIssueSubmittedHandler extends AbstractIssueApplicationSubmittedHandler {

    public FeeAccountDebitedAndIssueSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                     EvidenceManagementDeleteService evidenceManagementDeleteService,
                                                     RetryExecutor retryExecutor,
                                                     IssueApplicationConsentCorresponder issueApplicationConsentCorresponder,
                                                     AssignPartiesAccessService assignPartiesAccessService) {
        super(finremCaseDetailsMapper, evidenceManagementDeleteService,
            retryExecutor, issueApplicationConsentCorresponder, assignPartiesAccessService);
    }

    @Override
    protected EventType supportedEventType() {
        return EventType.FEE_ACCOUNT_DEBITED_AND_ISSUE;
    }

    @Override
    protected String getConfirmationHeader() {
        return "Fee account debited and issued with errors";
    }
}
