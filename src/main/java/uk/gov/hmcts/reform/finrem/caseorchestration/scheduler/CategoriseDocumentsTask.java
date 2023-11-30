package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.DocumentCategoryAssigner;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

@Component
@Slf4j
public class CategoriseDocumentsTask extends BaseTask {

    private final DocumentCategoryAssigner documentCategoryAssigner;

    @Autowired
    protected CategoriseDocumentsTask(CaseReferenceCsvLoader csvLoader,
                                      CcdService ccdService,
                                      SystemUserService systemUserService,
                                      FinremCaseDetailsMapper finremCaseDetailsMapper,
                                      DocumentCategoryAssigner documentCategoryAssigner) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);

        this.documentCategoryAssigner = documentCategoryAssigner;
    }

    @Override
    protected String getCaseListFileName() {
        return "null";
    }

    @Override
    protected String getTaskName() {
        return "CategoriseDocumentsTask";
    }

    @Override
    protected boolean isTaskEnabled() {
        return true;
    }

    @Override
    protected CaseType getCaseType() {
        return CaseType.CONTESTED;
    }

    @Override
    protected String getSummary() {
        return "DFR-2368 - Categorise documents";
    }

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {

        documentCategoryAssigner.assignDocumentCategories(finremCaseDetails.getData());
    }
}
