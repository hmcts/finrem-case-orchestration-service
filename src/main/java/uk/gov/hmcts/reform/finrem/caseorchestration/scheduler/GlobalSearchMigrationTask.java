package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CustomRequestScopeAttr;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.ScheduledTaskException;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Scheduled task to update cases for Global search
 * To enable the task to execute set environment variables:
 * <ul>
 *     <li>CRON_GLOBAL_SEARCH_MIGRATION_ENABLED=true</li>
 *     <li>TASK_NAME=GlobalSearchMigrationTask</li>
 *     <li>CRON_GLOBAL_SEARCH_MIGRATION_CASE_TYPE_ID=FinancialRemedyContested | FinancialRemedyMVP2</li>
 *     <li>CRON_GLOBAL_SEARCH_MIGRATION_BATCH_SIZE=number of cases to search for</li>
 * </ul>
 */
@Component
@Slf4j
public class GlobalSearchMigrationTask implements Runnable {

    private static final String TASK_NAME = "GlobalSearchMigrationTask";
    private static final String SUMMARY = "DFR-4961";

    @Value("${cron.globalSearchMigration.enabled:false}")
    private boolean taskEnabled;

    @Value("${cron.globalSearchMigration.caseTypeId:FinancialRemedyContested}")
    private String caseTypeId;
    @Value("${cron.globalSearchMigration.batchSize:500}")
    private int batchSize;
    @Value("${cron.batchsize:500}")
    private int bulkPrintBatchSize;
    @Value("${cron.wait-time-mins:10}")
    private int bulkPrintWaitTime;
    @Value("${cron.dryRun:false}")
    private boolean dryRun;
    @Value("${cron.supplementaryDataUpdate:false}")
    private boolean supplementaryDataRequired;
    @Autowired
    private CcdService ccdService;
    @Autowired
    private SystemUserService systemUserService;
    @Autowired
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    private HashMap<String, String> taskFailures = new HashMap<>();

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

                            if (supplementaryDataRequired) {
                                ccdService.submitSupplementaryDataToCcd(systemUserToken, caseId);
                                log.info("Global Search supplementary data added by {} for Case ID: {}",
                                    getTaskName(), caseId);
                            }
                        } else {
                            log.info("[DRY RUN] Updated {} for Case ID: {}", getTaskName(), caseId);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    handleException(caseId, e, "Interrupted while processing case");
                } catch (JsonProcessingException e) {
                    handleException(caseId, e, "JSON processing error");
                } catch (ScheduledTaskException e) {
                    handleException(caseId, e, "Scheduled task error");
                } catch (Exception e) {
                    handleException(caseId, e, "Unexpected error");
                } finally {
                    RequestContextHolder.resetRequestAttributes();
                }
            }
            long endTime = System.currentTimeMillis(); // End the timer
            log.info("Scheduled task {} completed. Total time taken: {} ms, with {} failures.", getTaskName(),
                (endTime - startTime), taskFailures.size());
        }
    }

    private List<CaseReference> getCaseReferences() {
        String searchQuery = getSearchQuery();
        String systemUserToken = getSystemUserToken();
        SearchResult searchResult = ccdService.esSearchCases(getCaseType(), searchQuery, systemUserToken);
        log.info("{} cases found for {}", searchResult.getTotal(), caseTypeId);

        return searchResult.getCases().stream()
            .map(caseDetails -> caseDetails.getId().toString())
            .map(CaseReference::new)
            .toList();
    }

    private String getTaskName() {
        return TASK_NAME;
    }

    private boolean isTaskEnabled() {
        return taskEnabled;
    }

    private CaseType getCaseType() {
        return CaseType.forValue(caseTypeId);
    }

    private String getSummary() {
        return SUMMARY;
    }

    private void executeTask(FinremCaseDetails finremCaseDetails) {
        FinremCaseData caseData = finremCaseDetails.getData();
        String ccdCaseId = String.valueOf(finremCaseDetails.getId());
        caseData.setCcdCaseId(ccdCaseId);
    }

    private String getSearchQuery() {

        BoolQueryBuilder stateQuery = QueryBuilders.boolQuery()
            .mustNot(new TermsQueryBuilder("state.keyword", "close", "consentOrderMade"));
        BoolQueryBuilder supplementaryQuery = QueryBuilders.boolQuery()
            .mustNot(new ExistsQueryBuilder("supplementary_data.HMCTSServiceId"));
        BoolQueryBuilder searchCriteriaQuery = QueryBuilders.boolQuery()
            .mustNot(new ExistsQueryBuilder("data.SearchCriteria"));
        QueryBuilder shouldQuery = QueryBuilders.boolQuery()
            .should(stateQuery)
            .should(supplementaryQuery)
            .should(searchCriteriaQuery);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            .size(batchSize)
            .query(shouldQuery);

        return searchSourceBuilder.toString();
    }

    protected String getSystemUserToken() {
        log.info("Getting system user token");
        return systemUserService.getSysUserToken();
    }

    /**
     * Check to determine if the case needs to be updated by the task.
     *
     * @param caseDetails the case to check.
     * @return true if the case needs to be updated, false otherwise.
     */
    protected boolean isUpdatedRequired(CaseDetails caseDetails) {
        return true;
    }

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
     *
     * @param finremCaseDetails the case details to be submitted in the event
     * @return the description to be used in the event submission
     */
    protected String getDescription(FinremCaseDetails finremCaseDetails) {
        return getSummary();
    }

    private void handleException(String caseId, Exception e, String errorMessage) {
        log.error("Cron task {}: {} for case {}", getTaskName(), errorMessage, caseId, e);
        taskFailures.put(caseId, errorMessage);
    }
}
