package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;

import java.util.List;

public abstract class ElasticSearchResultProcessingTask extends BaseTask {
    protected ElasticSearchResultProcessingTask(CcdService ccdService,
                                                SystemUserService systemUserService,
                                                FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(ccdService, systemUserService, finremCaseDetailsMapper);
    }

    @Override
    protected List<CaseReference> getCaseReferences() {
        return null;
    }
}
