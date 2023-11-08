package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CustomRequestScopeAttr;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ScheduleOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApplicationTypeAddTask implements Runnable {

    private final CaseReferenceCsvLoader csvLoader;
    private final CcdService ccdService;
    private final SystemUserService systemUserService;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Value("${cron.batchsize:500}")
    private int bulkPrintBatchSize;
    @Value("${cron.wait-time-mins:10}")
    private int bulkPrintWaitTime;

    @Value("${cron.applicationTypeAdd.enabled:false}")
    private boolean isApplicationTypeAddTaskEnabled;

    @Override
    public void run() {
        log.info("Scheduled task ApplicationTypeAddTask isEnabled {}", isApplicationTypeAddTaskEnabled);
        if (isApplicationTypeAddTaskEnabled) {
            log.info("Scheduled task ApplicationTypeAddTask started to run for selected cases");
            List<CaseReference> caseReferences = csvLoader.loadCaseReferenceList("applicationTypeAddCaseReferenceList.csv");
            int count = 0;
            int batchCount = 1;
            for (CaseReference caseReference : caseReferences) {
                count++;
                try {
                    RequestContextHolder.setRequestAttributes(new CustomRequestScopeAttr());
                    if (count == bulkPrintBatchSize) {
                        log.info("Batch {} limit reached {}, pausing for {} minutes", batchCount, bulkPrintBatchSize, bulkPrintWaitTime);
                        TimeUnit.MINUTES.sleep(bulkPrintWaitTime);
                        count = 0;
                        batchCount++;
                    }

                    log.info("Process case reference {}, batch {}, count {}", caseReference.getCaseReference(), batchCount, count);
                    SearchResult searchResult =
                        ccdService.getCaseByCaseId(caseReference.getCaseReference(), CaseType.CONTESTED, systemUserService.getSysUserToken());
                    log.info("SearchResult count {}", searchResult.getTotal());
                    if (CollectionUtils.isNotEmpty(searchResult.getCases())) {
                        StartEventResponse startEventResponse = ccdService.startEventForCaseWorker(systemUserService.getSysUserToken(),
                            caseReference.getCaseReference(), CaseType.CONTESTED.getCcdType(), EventType.AMEND_CASE_CRON.getCcdType());

                        CaseDetails caseDetails = startEventResponse.getCaseDetails();
                        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
                        FinremCaseData finremCaseData = finremCaseDetails.getData();
                        log.info("Updating application type for Case ID: {}", caseDetails.getId());
                        if (finremCaseData.getScheduleOneWrapper().getTypeOfApplication() == null) {
                            ScheduleOneWrapper scheduleOneWrapper = finremCaseData.getScheduleOneWrapper();
                            boolean typeCheck = scheduleOneWrapper.getChildrenCollection() != null
                                && !scheduleOneWrapper.getChildrenCollection().isEmpty();
                            scheduleOneWrapper.setTypeOfApplication(typeCheck
                                ? Schedule1OrMatrimonialAndCpList.SCHEDULE_1_CHILDREN_ACT_1989
                                : Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS);
                        }
                        CaseDetails updatedCaseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
                        startEventResponse.getCaseDetails().setData(updatedCaseDetails.getData());
                        ccdService.submitEventForCaseWorker(startEventResponse, systemUserService.getSysUserToken(),
                            caseDetails.getId().toString(),
                            CaseType.CONTESTED.getCcdType(),
                            EventType.AMEND_CASE_CRON.getCcdType(),
                            "Added Application Type DFR-2476",
                            "Added Application Type DFR-2476");
                        log.info("Updated application type for Case ID: {}", caseDetails.getId());
                    }

                } catch (InterruptedException | RuntimeException e) {
                    log.error("Error processing caseRef {} and error is {}", caseReference.getCaseReference(), e);
                } finally {
                    RequestContextHolder.resetRequestAttributes();
                }

            }
        }
    }
}
