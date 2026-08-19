package uk.gov.hmcts.reform.finrem.caseorchestration.handler.feeacctdebitedandissue.consented;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.consented.AbstractIssueApplicationAboutToStartHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

@Slf4j
@Service
public class FeeAccountDebitedAndIssueAboutToStartHandler extends AbstractIssueApplicationAboutToStartHandler {

    public FeeAccountDebitedAndIssueAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                        OnStartDefaultValueService onStartDefaultValueService) {
        super(finremCaseDetailsMapper, onStartDefaultValueService);
    }

    @Override
    protected EventType supportedEventType() {
        return EventType.FEE_ACCOUNT_DEBITED_AND_ISSUE;
    }
}
