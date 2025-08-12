package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class BaseTask implements Runnable {

    protected final CcdService ccdService;
    private final SystemUserService systemUserService;
    protected final FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Value("${cron.batchsize:500}")
    private int bulkPrintBatchSize;
    @Value("${cron.wait-time-mins:10}")
    private int bulkPrintWaitTime;
    @Value("${cron.dryRun:false}")
    private boolean dryRun;

    protected BaseTask(CcdService ccdService, SystemUserService systemUserService,
                       FinremCaseDetailsMapper finremCaseDetailsMapper) {
        this.ccdService = ccdService;
        this.systemUserService = systemUserService;
        this.finremCaseDetailsMapper = finremCaseDetailsMapper;
    }

    @Override
    public void run() {
        log.info("Scheduled task {} isEnabled {}", getTaskName(), isTaskEnabled());
        if (isTaskEnabled()) {
            log.info("Scheduled task {} started to run for selected cases", getTaskName());
            List<CaseReference> caseReferences = getCaseReferences();
            int count = 0;
            int batchCount = 1;
            long startTime = System.currentTimeMillis(); // Start the timer
            for (CaseReference caseReference : caseReferences) {
                count++;
                final String caseId = caseReference.getCaseReference();
                try {
                    RequestContextHolder.setRequestAttributes(new CustomRequestScopeAttr());
                    if (count == bulkPrintBatchSize) {
                        log.info("Batch {} limit reached {}, pausing for {} minutes", batchCount, bulkPrintBatchSize, bulkPrintWaitTime);
                        TimeUnit.MINUTES.sleep(bulkPrintWaitTime);
                        count = 0;
                        batchCount++;
                    }
                    String systemUserToken = getSystemUserToken();
                    log.info("Process case reference {}, batch {}, count {}", caseId, batchCount, count);

                    SearchResult searchResult =
                        ccdService.getCaseByCaseId(caseId, getCaseType(), systemUserToken);
                    log.info("SearchResult count {}", searchResult.getTotal());
                    if (CollectionUtils.isNotEmpty(searchResult.getCases())) {
                        if (!isUpdatedRequired(searchResult.getCases().getFirst())) {
                            log.info("No update required for case reference {}", caseId);
                            continue;
                        }

                        StartEventResponse startEventResponse = ccdService.startEventForCaseWorker(systemUserToken,
                            caseId, getCaseType().getCcdType(), EventType.AMEND_CASE_CRON.getCcdType());

                        CaseDetails caseDetails = startEventResponse.getCaseDetails();
                        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
                        log.info("Updating {} for Case ID: {}", getTaskName(), caseId);
                        executeTask(finremCaseDetails);
                        String description = getDescription(finremCaseDetails);
                        CaseDetails updatedCaseDetails = finremCaseDetailsMapper.mapToCaseDetailsIncludingNulls(finremCaseDetails,
                            classesToOverrideJsonInclude());
                        startEventResponse.getCaseDetails().setData(updatedCaseDetails.getData());
                        if (!dryRun) {
                            ccdService.submitEventForCaseWorker(startEventResponse, systemUserToken,
                                caseId,
                                getCaseType().getCcdType(),
                                EventType.AMEND_CASE_CRON.getCcdType(),
                                getSummary(),
                                description);
                            log.info("Updated {} for Case ID: {}", getTaskName(), caseId);
                        } else {
                            log.info("[DRY RUN] Updated {} for Case ID: {}", getTaskName(), caseId);
                        }
                    }
                } catch (InterruptedException | RuntimeException | JsonProcessingException e) {
                    log.error("Cron task {}: Error processing case {}", getTaskName(), caseId, e);
                } finally {
                    RequestContextHolder.resetRequestAttributes();
                }
            }
            long endTime = System.currentTimeMillis(); // End the timer
            log.info("Scheduled task {} completed. Total time taken: {} ms", getTaskName(), (endTime - startTime));
        }
    }

    protected String getSystemUserToken() {
        log.info("Getting system user token");
        return systemUserService.getSysUserToken();
    }

    /**
     * Check to determine if the case needs to be updated by the task.
     * @param caseDetails the case to check.
     * @return true if the case needs to be updated, false otherwise.
     */
    protected boolean isUpdatedRequired(CaseDetails caseDetails) {
        return true;
    }

    protected abstract List<CaseReference> getCaseReferences();

    protected abstract String getTaskName();

    protected abstract boolean isTaskEnabled();

    protected abstract CaseType getCaseType();

    protected abstract String getSummary();

    protected abstract void executeTask(FinremCaseDetails finremCaseDetails);

    /**
     * Specifies the classes for which null values should be included during mapping.
     *
     * <p>
     * This is useful when you want to explicitly delete values by setting properties to {@code null}.
     * For example, if your service sets {@code propertyA} to {@code null}, you must pass
     * {@code "propertyA": null} in the map sent to the CCD API to perform a delete operation.
     *
     * <p>
     * Override this method to declare the classes that require null value inclusion during the mapping process.
     *
     * @return an array of classes for which null values should be included; defaults to an empty array.
     */
    protected Class[] classesToOverrideJsonInclude() {
        return new Class[0];
    }

    /**
     * Get a description to be used in the update event submission.
     * @param finremCaseDetails the case details to be submitted in the event
     * @return the description to be used in the event submission
     */
    protected String getDescription(FinremCaseDetails finremCaseDetails) {
        return getSummary();
    }
}
