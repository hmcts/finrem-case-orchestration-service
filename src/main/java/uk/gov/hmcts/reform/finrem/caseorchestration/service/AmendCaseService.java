package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ScheduleOneWrapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class AmendCaseService {

    public void addApplicationType(FinremCaseData finremCaseData) {
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
