package uk.gov.hmcts.reform.finrem.caseorchestration.utils.aac;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.aac.AacApproverAuthUtil;

@Service
@Slf4j
public class SwitchUserContextService {

    protected static final String FIRSTNAME = "firstname";
    protected static final String LASTNAME = "lastname";

    private final AacApproverAuthUtil aacApproverAuthUtil;

    public SwitchUserContextService(AacApproverAuthUtil aacApproverAuthUtil) {
        this.aacApproverAuthUtil = aacApproverAuthUtil;
    }

    public String getApproverAuthToken() {
        return aacApproverAuthUtil.getApproverToken();
    }

    public String switchUserContextToCaseWorker() {
        return null;
    }
}
