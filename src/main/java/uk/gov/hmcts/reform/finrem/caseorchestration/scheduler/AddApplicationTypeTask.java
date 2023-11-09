package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AmendCaseService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

@Component
@Slf4j
public class AddApplicationTypeTask extends BaseTask {

    @Value("${cron.applicationTypeAdd.enabled:false}")
    private boolean isApplicationTypeAddTaskEnabled;

    private final AmendCaseService amendCaseService;

    @Autowired
    protected AddApplicationTypeTask(CaseReferenceCsvLoader csvLoader,
                                     CcdService ccdService,
                                     SystemUserService systemUserService,
                                     FinremCaseDetailsMapper finremCaseDetailsMapper,
                                     AmendCaseService amendCaseService) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
        this.amendCaseService = amendCaseService;
    }

    @Override
    protected String getCaseListFileName() {
        return "applicationTypeAddCaseReferenceList.csv";
    }

    @Override
    protected String getTaskName() {
        return "AddApplicationTypeTask";
    }

    @Override
    protected boolean isTaskEnabled() {
        return isApplicationTypeAddTaskEnabled;
    }

    @Override
    protected CaseType getCaseType() {
        return CaseType.CONTESTED;
    }

    @Override
    protected String getSummary() {
        return "Added Application Type DFR-2476";
    }

    @Override
    protected void executeTask(FinremCaseData finremCaseData) {
        amendCaseService.addApplicationType(finremCaseData);
    }
}
