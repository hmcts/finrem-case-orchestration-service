package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.List;

public abstract class CsvFileProcessingTask extends BaseTask {
    protected final CaseReferenceCsvLoader csvLoader;

    protected CsvFileProcessingTask(CaseReferenceCsvLoader csvLoader,
                                    CcdService ccdService,
                                    SystemUserService systemUserService,
                                    FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(ccdService, systemUserService, finremCaseDetailsMapper);
        this.csvLoader = csvLoader;
    }

    protected List<CaseReference> getCaseReferences() {
        return csvLoader.loadCaseReferenceList(getCaseListFileName());
    }

    protected abstract String getCaseListFileName();
}
