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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
                        CaseDetails updatedCaseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
                        startEventResponse.getCaseDetails().setData(
                            mergeCaseDataWithNullsForRemovedKeys(caseDetails, updatedCaseDetails)
                            //updatedCaseDetails.getData()
                        );
                        ccdService.submitEventForCaseWorker(startEventResponse, systemUserToken,
                            caseId,
                            getCaseType().getCcdType(),
                            EventType.AMEND_CASE_CRON.getCcdType(),
                            getSummary(),
                            description);
                        log.info("Updated {} for Case ID: {}", getTaskName(), caseId);
                    }
                } catch (InterruptedException | RuntimeException e) {
                    log.error("Cron task {}: Error processing case {}", getTaskName(), caseId, e);
                } finally {
                    RequestContextHolder.resetRequestAttributes();
                }
            }
        }
    }

    /**
     * It's an alternative way to make for adding @JsonInclude(JsonInclude.Include.ALWAYS) to POJO classes.
     *
     * <p>
     * Recursively merges the data maps from two {@link CaseDetails} objects.
     *
     * <p>
     * For each key in either the source (a) or target (b), the merged map uses the value from the target (b).
     * If a key is present in the source but missing in the target, it is treated as deleted
     * and added to the result with a {@code null} value.
     *
     * <p>
     * If both source and target values are maps, they are merged recursively using the same rules.
     *
     * @param a the original {@code CaseDetails} containing the source case data
     * @param b the updated {@code CaseDetails} containing the target case data
     * @return a merged {@code Map<String, Object>} where removed properties are explicitly set to {@code null}
     */
    private static Map<String, Object> mergeCaseDataWithNullsForRemovedKeys(CaseDetails a, CaseDetails b) {
        return mergeMapsRecursively(a.getData(), b.getData());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mergeMapsRecursively(Map<String, Object> aMap, Map<String, Object> bMap) {
        Map<String, Object> result = new HashMap<>();

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(aMap.keySet());
        allKeys.addAll(bMap.keySet());

        for (String key : allKeys) {
            Object aVal = aMap.get(key);
            Object bVal = bMap.get(key);

            if (aVal instanceof Map && bVal instanceof Map) {
                // Both are maps, so merge recursively
                result.put(key, mergeMapsRecursively((Map<String, Object>) aVal, (Map<String, Object>) bVal));
            } else if (bMap.containsKey(key)) {
                // Use value from b, even if it's null
                result.put(key, bVal);
            } else {
                // Key deleted from b — mark as null
                result.put(key, null);
            }
        }

        return result;
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
     * Get a description to be used in the update event submission.
     * @param finremCaseDetails the case details to be submitted in the event
     * @return the description to be used in the event submission
     */
    protected String getDescription(FinremCaseDetails finremCaseDetails) {
        return getSummary();
    }
}
