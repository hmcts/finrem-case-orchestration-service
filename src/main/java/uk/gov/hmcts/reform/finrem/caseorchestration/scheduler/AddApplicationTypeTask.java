package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ScheduleOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

@Component
@Slf4j
public class AddApplicationTypeTask extends BaseTask {

    @Value("${cron.applicationTypeAdd.enabled:false}")
    private boolean isApplicationTypeAddTaskEnabled;

    @Autowired
    protected AddApplicationTypeTask(CaseReferenceCsvLoader csvLoader,
                                     CcdService ccdService,
                                     SystemUserService systemUserService,
                                     FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
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
    protected void updateCaseData(FinremCaseData finremCaseData) {
        if (finremCaseData.getScheduleOneWrapper().getTypeOfApplication() == null) {
            ScheduleOneWrapper scheduleOneWrapper = finremCaseData.getScheduleOneWrapper();
            boolean typeCheck = scheduleOneWrapper.getChildrenCollection() != null
                && !scheduleOneWrapper.getChildrenCollection().isEmpty();
            scheduleOneWrapper.setTypeOfApplication(typeCheck
                ? Schedule1OrMatrimonialAndCpList.SCHEDULE_1_CHILDREN_ACT_1989
                : Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS);
        }
    }
}
