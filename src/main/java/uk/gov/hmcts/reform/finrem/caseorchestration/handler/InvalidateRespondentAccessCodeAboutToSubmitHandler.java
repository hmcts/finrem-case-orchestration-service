package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InvalidateAccessCodeService;

import java.util.List;

@Slf4j
@Service
public class InvalidateRespondentAccessCodeAboutToSubmitHandler extends InvalidateAccessCodeAboutToSubmitHandler {

    public InvalidateRespondentAccessCodeAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                              InvalidateAccessCodeService invalidateAccessCodeService) {
        super(finremCaseDetailsMapper, invalidateAccessCodeService);
    }

    @Override
    protected EventType handledEventType() {
        return EventType.INVALIDATE_RESPONDENT_ACCESS_CODE;
    }

    @Override
    protected List<AccessCodeCollection> getAccessCodes(FinremCaseData data) {
        return data.getRespondentAccessCodes();
    }

    @Override
    protected void setAccessCodes(FinremCaseData data, List<AccessCodeCollection> accessCodes) {
        data.setRespondentAccessCodes(accessCodes);
    }
}
