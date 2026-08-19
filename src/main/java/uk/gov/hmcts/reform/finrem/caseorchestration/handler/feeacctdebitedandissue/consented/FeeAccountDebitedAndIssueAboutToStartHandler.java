package uk.gov.hmcts.reform.finrem.caseorchestration.handler.feeacctdebitedandissue.consented;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.consented.AbstractDefaultIssueDateAboutToStartHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

@Slf4j
@Service
public class FeeAccountDebitedAndIssueAboutToStartHandler extends AbstractDefaultIssueDateAboutToStartHandler {

    public FeeAccountDebitedAndIssueAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                        OnStartDefaultValueService onStartDefaultValueService) {
        super(finremCaseDetailsMapper, onStartDefaultValueService);
    }

    @Override
    protected EventType supportedEventType() {
        return EventType.ISSUE_APPLICATION;
    }
}
