package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class BaseTask implements Runnable {

    private final CaseReferenceCsvLoader csvLoader;
    private final CcdService ccdService;
    private final SystemUserService systemUserService;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Value("${cron.batchsize:500}")
    private int bulkPrintBatchSize;
    @Value("${cron.wait-time-mins:10}")
    private int bulkPrintWaitTime;

    protected BaseTask(CaseReferenceCsvLoader csvLoader, CcdService ccdService, SystemUserService systemUserService,
                       FinremCaseDetailsMapper finremCaseDetailsMapper) {
        this.csvLoader = csvLoader;
        this.ccdService = ccdService;
        this.systemUserService = systemUserService;
        this.finremCaseDetailsMapper = finremCaseDetailsMapper;

    }

    @Override
    public void run() {
        log.info("Scheduled task {} isEnabled {}", getTaskName(), isTaskEnabled());
        if (isTaskEnabled()) {
            log.info("Scheduled task ApplicationTypeAddTask started to run for selected cases");
            List<CaseReference> caseReferences = getCaseReferences();
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
                        ccdService.getCaseByCaseId(caseReference.getCaseReference(), getCaseType(), systemUserService.getSysUserToken());
                    log.info("SearchResult count {}", searchResult.getTotal());
                    if (CollectionUtils.isNotEmpty(searchResult.getCases())) {
                        StartEventResponse startEventResponse = ccdService.startEventForCaseWorker(systemUserService.getSysUserToken(),
                            caseReference.getCaseReference(), getCaseType().getCcdType(), EventType.AMEND_CASE_CRON.getCcdType());

                        CaseDetails caseDetails = startEventResponse.getCaseDetails();
                        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
                        FinremCaseData finremCaseData = finremCaseDetails.getData();
                        log.info("Updating application type for Case ID: {}", caseDetails.getId());
                        updateCaseData(finremCaseData);
                        CaseDetails updatedCaseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
                        startEventResponse.getCaseDetails().setData(updatedCaseDetails.getData());
                        ccdService.submitEventForCaseWorker(startEventResponse, systemUserService.getSysUserToken(),
                            caseDetails.getId().toString(),
                            getCaseType().getCcdType(),
                            EventType.AMEND_CASE_CRON.getCcdType(),
                            getSummary(),
                            getSummary());
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

    private List<CaseReference> getCaseReferences() {
        List<CaseReference> caseReferences = csvLoader.loadCaseReferenceList(getCaseListFileName());
        return caseReferences;
    }

    protected abstract String getCaseListFileName();

    protected abstract String getTaskName();

    protected abstract boolean isTaskEnabled();

    protected abstract CaseType getCaseType();

    protected abstract String getSummary();

    protected abstract void updateCaseData(FinremCaseData finremCaseData);
}
