package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeneralApplicationRemoveTask implements Task {

    private final GeneralApplicationHelper generalApplicationHelper;

    @Value("${cron.generalApplicationRemove.enabled:false}")
    private boolean isGeneralApplicationRemoveTaskEnabled;


    @Override
    public String getCaseListFileName() {
        return "generalApplicationRemoveCaseReferenceList.csv";
    }

    @Override
    public String getTaskName() {
        return "GeneralApplicationRemoveTask";
    }

    @Override
    public boolean isTaskEnabled() {
        return isGeneralApplicationRemoveTaskEnabled;
    }

    @Override
    public CaseType getCaseType() {
        return CaseType.CONTESTED;
    }

    @Override
    public String getSummary() {
        return "Remove duplicate General application DFR-2388";
    }

    @Override
    public void executeTask(FinremCaseDetails finremCaseDetails, String authToken) {
        generalApplicationHelper.checkAndRemoveDuplicateGeneralApplications(finremCaseDetails.getData());
    }
}
