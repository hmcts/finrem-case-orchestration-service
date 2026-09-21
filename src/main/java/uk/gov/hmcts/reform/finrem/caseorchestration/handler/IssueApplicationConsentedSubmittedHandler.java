package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.consented.AbstractIssueApplicationSubmittedHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationAuditService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.consented.AssignToJudgeCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

@Slf4j
@Service
public class IssueApplicationConsentedSubmittedHandler extends AbstractIssueApplicationSubmittedHandler {

    public IssueApplicationConsentedSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                     EvidenceManagementDeleteService evidenceManagementDeleteService,
                                                     RetryExecutor retryExecutor,
                                                     AssignToJudgeCorresponder assignToJudgeCorresponder,
                                                     AssignPartiesAccessService assignPartiesAccessService,
                                                     ApplicationEventPublisher applicationEventPublisher,
                                                     NotificationAuditService notificationAuditService) {
        super(finremCaseDetailsMapper, evidenceManagementDeleteService, retryExecutor, assignToJudgeCorresponder,
            assignPartiesAccessService, applicationEventPublisher, notificationAuditService);
    }

    @Override
    protected EventType supportedEventType() {
        return EventType.ISSUE_APPLICATION;
    }
}
