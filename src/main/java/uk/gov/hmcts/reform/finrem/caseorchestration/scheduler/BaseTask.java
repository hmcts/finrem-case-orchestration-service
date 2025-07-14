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

import java.util.ArrayList;
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
                            // Comment original code: updatedCaseDetails.getData()
                            mergeCaseDataWithNullsForRemovedKeys(caseDetails, updatedCaseDetails)
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
     * Provides an alternative approach to adding {@code @JsonInclude(JsonInclude.Include.ALWAYS)}
     * to POJO classes by explicitly setting removed properties to {@code null} in merged maps.
     *
     * <p>Recursively merges the data maps from two {@link CaseDetails} instances.
     *
     * <p>For each key present in either the source ({@code a}) or the target ({@code b}):
     * If the key exists in the target, its value is used in the merged result.
     * If the key exists in the source but is missing in the target, it is considered deleted
     * and included in the result with a {@code null} value.
     * If both values are maps, they are recursively merged using the same logic.
     * If both values are lists, the lists are merged element-wise by index, recursively merging
     * elements if they are maps.
     *
     * @param a the original {@code CaseDetails} containing the source case data
     * @param b the updated {@code CaseDetails} containing the target case data
     * @return a merged {@code Map<String, Object>} where keys removed in {@code b} are explicitly set to {@code null}
     */
    private static Map<String, Object> mergeCaseDataWithNullsForRemovedKeys(CaseDetails a, CaseDetails b) {
        return mergeMapsRecursively(a.getData(), b.getData());
    }

    /**
     * Recursively merges two maps according to the rules described in {@link #mergeCaseDataWithNullsForRemovedKeys(CaseDetails, CaseDetails)}.
     *
     * @param mapA the original source map
     * @param mapB the updated target map
     * @return a merged map containing all keys from both maps; keys only in {@code mapA} have {@code null} values in the result
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> mergeMapsRecursively(Map<String, Object> mapA, Map<String, Object> mapB) {
        Map<String, Object> result = new HashMap<>();

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(mapA.keySet());
        allKeys.addAll(mapB.keySet());

        for (String key : allKeys) {
            Object valA = mapA.get(key);
            Object valB = mapB.get(key);

            if (valA instanceof Map && valB instanceof Map) {
                result.put(key, mergeMapsRecursively((Map<String, Object>) valA, (Map<String, Object>) valB));
            } else if (valA instanceof List && valB instanceof List) {
                result.put(key, mergeListOfMapsByIndex((List<Object>) valA, (List<Object>) valB));
            } else if (mapB.containsKey(key)) {
                result.put(key, valB); // use value from bMap (even if null)
            } else {
                result.put(key, null); // key deleted
            }
        }

        return result;
    }

    /**
     * Merges two lists element-wise by index. If elements at the same index are maps,
     * they are merged recursively; otherwise, the element from the target list is used.
     *
     * @param listA the original source list
     * @param listB the updated target list
     * @return a merged list combining elements from both lists
     */
    @SuppressWarnings("unchecked")
    private static List<Object> mergeListOfMapsByIndex(List<Object> listA, List<Object> listB) {
        List<Object> result = new ArrayList<>();

        int maxSize = Math.max(listA.size(), listB.size());

        for (int i = 0; i < maxSize; i++) {
            Object elementA = i < listA.size() ? listA.get(i) : null;
            Object elementB = i < listB.size() ? listB.get(i) : null;

            // missing in bList, keep null
            if (elementA instanceof Map && elementB instanceof Map) {
                result.add(mergeMapsRecursively((Map<String, Object>) elementA, (Map<String, Object>) elementB));
            } else {
                result.add(elementB); // use from bList
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
