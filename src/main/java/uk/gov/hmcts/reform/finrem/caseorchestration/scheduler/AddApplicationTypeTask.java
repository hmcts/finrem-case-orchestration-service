package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AmendCaseService;

@Component
@Slf4j
public class AddApplicationTypeTask implements Task {

    @Value("${cron.applicationTypeAdd.enabled:false}")
    private boolean isApplicationTypeAddTaskEnabled;

    private final AmendCaseService amendCaseService;

    @Autowired
    public AddApplicationTypeTask(AmendCaseService amendCaseService) {
        this.amendCaseService = amendCaseService;
    }


    @Override
    public String getCaseListFileName() {
        return "applicationTypeAddCaseReferenceList.csv";
    }

    @Override
    public String getTaskName() {
        return "AddApplicationTypeTask";
    }

    @Override
    public boolean isTaskEnabled() {
        return isApplicationTypeAddTaskEnabled;
    }

    @Override
    public CaseType getCaseType() {
        return CaseType.CONTESTED;
    }

    @Override
    public String getSummary() {
        return "Added Application Type DFR-2476";
    }

    @Override
    public void executeTask(FinremCaseDetails finremCaseDetails, String authToken) {
        amendCaseService.addApplicationType(finremCaseDetails.getData());
    }

}
