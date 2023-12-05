package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

@Component
@Slf4j
public class GeneralApplicationRemoveTask extends BaseTask {

    private final GeneralApplicationHelper generalApplicationHelper;

    @Value("${cron.generalApplicationRemove.enabled:false}")
    private boolean isGeneralApplicationRemoveTaskEnabled;

    @Autowired
    protected GeneralApplicationRemoveTask(CaseReferenceCsvLoader csvLoader,
                                           CcdService ccdService,
                                           SystemUserService systemUserService,
                                           FinremCaseDetailsMapper finremCaseDetailsMapper,
                                           GeneralApplicationHelper generalApplicationHelper) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
        this.generalApplicationHelper = generalApplicationHelper;
    }

    @Override
    protected String getCaseListFileName() {
        return "generalApplicationRemoveCaseReferenceList.csv";
    }

    @Override
    protected String getTaskName() {
        return "GeneralApplicationRemoveTask";
    }

    @Override
    protected boolean isTaskEnabled() {
        return isGeneralApplicationRemoveTaskEnabled;
    }

    @Override
    protected CaseType getCaseType() {
        return CaseType.CONTESTED;
    }

    @Override
    protected String getSummary() {
        return "Remove duplicate General application DFR-2388";
    }

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        generalApplicationHelper.checkAndRemoveDuplicateGeneralApplications(finremCaseDetails.getData());
    }
}
