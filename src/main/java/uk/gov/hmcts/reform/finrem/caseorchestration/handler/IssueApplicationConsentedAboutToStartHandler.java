package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.consented.AbstractIssueApplicationAboutToStartHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

@Slf4j
@Service
public class IssueApplicationConsentedAboutToStartHandler extends AbstractIssueApplicationAboutToStartHandler {

    public IssueApplicationConsentedAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                        OnStartDefaultValueService onStartDefaultValueService) {
        super(finremCaseDetailsMapper, onStartDefaultValueService);
    }

    @Override
    protected EventType supportedEventType() {
        return EventType.ISSUE_APPLICATION;
    }
}
