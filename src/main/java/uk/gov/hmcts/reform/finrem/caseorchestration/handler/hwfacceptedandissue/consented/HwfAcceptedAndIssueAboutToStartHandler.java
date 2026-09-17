package uk.gov.hmcts.reform.finrem.caseorchestration.handler.hwfacceptedandissue.consented;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.consented.AbstractIssueApplicationAboutToStartHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

@Slf4j
@Service
public class HwfAcceptedAndIssueAboutToStartHandler extends AbstractIssueApplicationAboutToStartHandler {

    public HwfAcceptedAndIssueAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                  OnStartDefaultValueService onStartDefaultValueService) {
        super(finremCaseDetailsMapper, onStartDefaultValueService);
    }

    @Override
    protected EventType supportedEventType() {
        return EventType.HWF_ACCEPTED_AND_ISSUE;
    }
}
