package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingsMigrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;

import java.util.List;

@Component
@Slf4j
public class ManageHearingsMigrationTask extends BaseTask {

    private static final String TASK_NAME = "ManageHearingsMigrationTask";

    private static final String SUMMARY = "Manage Hearings migration";

    private static final CaseType CASE_TYPE = CaseType.CONTESTED;

    @Value("${cron.manageHearingsMigration.enabled:false}")
    private boolean taskEnabled;

    @Value("${cron.manageHearingsMigration.batchSize:500}")
    private int batchSize;

    private final ManageHearingsMigrationService manageHearingsMigrationService;

    protected ManageHearingsMigrationTask(CcdService ccdService, SystemUserService systemUserService,
                                          FinremCaseDetailsMapper finremCaseDetailsMapper,
                                          ManageHearingsMigrationService manageHearingsMigrationService) {
        super(ccdService, systemUserService, finremCaseDetailsMapper);
        this.manageHearingsMigrationService = manageHearingsMigrationService;
    }

    @Override
    protected String getTaskName() {
        return TASK_NAME;
    }

    @Override
    protected boolean isTaskEnabled() {
        return taskEnabled;
    }

    @Override
    protected CaseType getCaseType() {
        return CASE_TYPE;
    }

    @Override
    protected String getSummary() {
        return SUMMARY;
    }

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        // TODO manageHearingsMigrationService
    }

    @Override
    protected List<CaseReference> getCaseReferences() {
        String searchQuery = getSearchQuery();
        String systemUserToken = getSystemUserToken();

        SearchResult searchResult = ccdService.esSearchCases(getCaseType(), searchQuery, systemUserToken);
        log.info("{} non-migrated manage hearings cases found for {}", searchResult.getTotal(), CASE_TYPE.getCcdType());

        return extractCaseReferences(searchResult);
    }

    private String getSearchQuery() {
        // Build a query for data.manageHearingsMigrated = "NO"
        BoolQueryBuilder notMigratedQuery = QueryBuilders.boolQuery()
            .should(new TermQueryBuilder("data.manageHearingsMigrated.keyword", "NO"))
            .should(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("data.manageHearingsMigrated")))
            .minimumShouldMatch(1); // Either "NO" or null

        // Exclude cases in state = "close"
        BoolQueryBuilder stateQuery = QueryBuilders.boolQuery()
            .mustNot(new TermQueryBuilder("state.keyword", "close"));

        // Combine queries
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
            .must(notMigratedQuery)
            .must(stateQuery);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            .size(batchSize)
            .query(boolQueryBuilder);

        return searchSourceBuilder.toString();
    }

    void setTaskEnabled(boolean taskEnabled) {
        this.taskEnabled = taskEnabled;
    }

    private List<CaseReference> extractCaseReferences(SearchResult searchResult) {
        return searchResult.getCases().stream()
            .map(finremCaseDetailsMapper::mapToFinremCaseDetails)
            .map(caseDetails -> caseDetails.getId().toString())
            .map(CaseReference::new)
            .toList();
    }
}
