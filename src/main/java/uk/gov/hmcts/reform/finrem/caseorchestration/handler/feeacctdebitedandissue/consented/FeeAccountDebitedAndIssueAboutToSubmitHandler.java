package uk.gov.hmcts.reform.finrem.caseorchestration.handler.feeacctdebitedandissue.consented;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.consented.AbstractIssueApplicationAboutToSubmitHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.issueapplication.IssueApplicationService;

@Slf4j
@Service
public class FeeAccountDebitedAndIssueAboutToSubmitHandler extends AbstractIssueApplicationAboutToSubmitHandler {

    public FeeAccountDebitedAndIssueAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                         OnlineFormDocumentService onlineFormDocumentService,
                                                         IssueApplicationService issueApplicationService) {
        super(finremCaseDetailsMapper, onlineFormDocumentService, issueApplicationService);
    }

    @Override
    protected EventType supportedEventType() {
        return EventType.FEE_ACCOUNT_DEBITED_AND_ISSUE;
    }
}
