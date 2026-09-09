package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Scheduled task to update cases for Global search.
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
public class GlobalSearchMigrationTask extends BaseTask {

    private static final String TASK_NAME = "GlobalSearchMigrationTask";
    private static final String SUMMARY = "DFR-4961";

    @Value("${cron.globalSearchMigration.enabled:false}")
    private boolean taskEnabled;
    @Value("${cron.globalSearchMigration.caseTypeId:FinancialRemedyContested}")
    private String caseTypeId;
    @Value("${cron.globalSearchMigration.batchSize:500}")
    private int gsQuerySize;

    public GlobalSearchMigrationTask(CcdService ccdService,
                                     SystemUserService systemUserService,
                                     FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(ccdService, systemUserService, finremCaseDetailsMapper);
    }

    @Override
    public void executeTask(FinremCaseDetails finremCaseDetails) {
        FinremCaseData caseData = finremCaseDetails.getData();
        String ccdCaseId = String.valueOf(finremCaseDetails.getId());
        caseData.setCcdCaseId(ccdCaseId);
    }

    @Override
    public List<CaseReference> getCaseReferences() {
        String searchQuery = getSearchQuery();
        log.info("Search query: {}", searchQuery);
        String systemUserToken = getSystemUserToken();
        SearchResult searchResult = ccdService.esSearchCases(getCaseType(), searchQuery, systemUserToken);
        log.info("{} cases found for {}", searchResult.getTotal(), caseTypeId);

        return searchResult.getCases().stream()
            .map(caseDetails -> caseDetails.getId().toString())
            .map(CaseReference::new)
            .toList();
    }

    private String getSearchQuery() {

        BoolQueryBuilder stateQuery = QueryBuilders.boolQuery()
            .mustNot(new TermsQueryBuilder("state.keyword", "close", "consentOrderMade"));
        BoolQueryBuilder supplementaryQuery = QueryBuilders.boolQuery()
            .mustNot(new ExistsQueryBuilder("supplementary_data.HMCTSServiceId"));
        BoolQueryBuilder searchCriteriaQuery = QueryBuilders.boolQuery()
            .mustNot(new ExistsQueryBuilder("data.SearchCriteria"));
        QueryBuilder shouldQuery = QueryBuilders.boolQuery()
            .must(stateQuery)
            .must(supplementaryQuery)
            .must(searchCriteriaQuery);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            .size(gsQuerySize)
            .query(shouldQuery);

        return searchSourceBuilder.toString();
    }

    private List<CaseReference> getCaseReferencesFromSearchResult(SearchResult searchResult) {
        List<CaseReference> caseReferences = new ArrayList<>();
        searchResult.getCases().forEach(caseDetails ->
            caseReferences.add(new CaseReference(caseDetails.getId().toString())));
        return caseReferences;
    }

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public boolean isTaskEnabled() {
        return taskEnabled;
    }

    @Override
    public CaseType getCaseType() {
        return CaseType.forValue(caseTypeId);
    }

    @Override
    protected String getSummary() {
        return SUMMARY;
    }
}
