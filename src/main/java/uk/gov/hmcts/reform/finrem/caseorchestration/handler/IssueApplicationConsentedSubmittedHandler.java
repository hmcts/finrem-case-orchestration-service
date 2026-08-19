package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.consented.AbstractDefaultIssueApplicationSubmittedHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.IssueApplicationConsentCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

@Slf4j
@Service
public class IssueApplicationConsentedSubmittedHandler extends AbstractDefaultIssueApplicationSubmittedHandler {

    public IssueApplicationConsentedSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                     EvidenceManagementDeleteService evidenceManagementDeleteService,
                                                     RetryExecutor retryExecutor,
                                                     IssueApplicationConsentCorresponder issueApplicationConsentCorresponder,
                                                     AssignPartiesAccessService assignPartiesAccessService) {
        super(finremCaseDetailsMapper, evidenceManagementDeleteService,
            retryExecutor, issueApplicationConsentCorresponder, assignPartiesAccessService);
    }

    @Override
    protected EventType supportedEventType() {
        return EventType.ISSUE_APPLICATION;
    }
}
