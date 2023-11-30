package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.DocumentCategoryAssigner;

@Component
@RequiredArgsConstructor
@Slf4j
public class CfvCategoriseCasesTask implements Task {


    private final DocumentCategoryAssigner documentCategoryAssigner;
    @Value("${cron.cfvCategoriseCasesTask.enabled:false}")
    private boolean isCfvCategoriseCasesTaskEnabled;

    @Override
    public String getCaseListFileName() {
        return null;
    }

    @Override
    public String getTaskName() {
        return "CfvCategoriseCasesTask";
    }

    @Override
    public boolean isTaskEnabled() {
        return isCfvCategoriseCasesTaskEnabled;
    }

    @Override
    public CaseType getCaseType() {
        return CaseType.CONTESTED;
    }

    @Override
    public String getSummary() {
        return "Categorise documents for cases DFR-2368";
    }

    @Override
    public void executeTask(FinremCaseDetails finremCaseDetails, String authToken) {

        log.info("CfvCategoriseCasesTask started for case id {}", finremCaseDetails.getId());
        documentCategoryAssigner.assignDocumentCategories(finremCaseDetails.getData());
        log.info("CfvCategoriseCasesTask completed for case id {}", finremCaseDetails.getId());

    }
}
