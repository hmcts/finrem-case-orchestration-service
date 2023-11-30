package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.AllArgsConstructor;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@AllArgsConstructor
public class FixCaseDataJob implements Runnable {

    private final CaseReferenceCsvLoader csvLoader;
    private final CcdService ccdService;
    private final SystemUserService systemUserService;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    private final List<Task> tasks;

    @Value("${cron.batchsize:500}")
    private int bulkPrintBatchSize;
    @Value("${cron.wait-time-mins:10}")
    private int bulkPrintWaitTime;

    @Value("${cron.taskToRun}")
    private String taskNameToRun;


    @Override
    public void run() {
        Task task = tasks.stream().filter(t -> t.getTaskName().equals(taskNameToRun)).findFirst().orElse(null);
        if (task != null) {
            if (task.isTaskEnabled()) {
                log.info("Scheduled task {} started to run for selected cases", task.getTaskName());
                List<CaseReference> caseReferences = getCaseReferences(task);
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
                            ccdService.getCaseByCaseId(caseReference.getCaseReference(), task.getCaseType(), getSystemUserToken());
                        log.info("SearchResult count {}", searchResult.getTotal());
                        if (CollectionUtils.isNotEmpty(searchResult.getCases())) {
                            StartEventResponse startEventResponse = ccdService.startEventForCaseWorker(getSystemUserToken(),
                                caseReference.getCaseReference(), task.getCaseType().getCcdType(), EventType.AMEND_CASE_CRON.getCcdType());

                            CaseDetails caseDetails = startEventResponse.getCaseDetails();
                            FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
                            log.info("Updating {} for Case ID: {}", task.getTaskName(), caseDetails.getId());
                            task.executeTask(finremCaseDetails, getSystemUserToken());
                            CaseDetails updatedCaseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
                            startEventResponse.getCaseDetails().setData(updatedCaseDetails.getData());
                            ccdService.submitEventForCaseWorker(startEventResponse, getSystemUserToken(),
                                caseDetails.getId().toString(),
                                task.getCaseType().getCcdType(),
                                EventType.AMEND_CASE_CRON.getCcdType(),
                                task.getSummary(),
                                task.getSummary());
                            log.info("Updated {} for Case ID: {}", task.getTaskName(), caseDetails.getId());
                        }

                    } catch (InterruptedException | RuntimeException e) {
                        log.error("Error processing caseRef {} and error is ", caseReference.getCaseReference(), e);
                    } finally {
                        RequestContextHolder.resetRequestAttributes();
                    }

                }
            } else {
                log.info("Scheduled task {} is not enabled", task.getTaskName());
            }
        } else {
            log.info("Task {} not found", taskNameToRun);
        }

    }

    protected String getSystemUserToken() {
        return systemUserService.getSysUserToken();
    }

    private List<CaseReference> getCaseReferences(Task task) {
        List<CaseReference> caseReferences = csvLoader.loadCaseReferenceList(task.getCaseListFileName());
        return caseReferences;
    }

}
