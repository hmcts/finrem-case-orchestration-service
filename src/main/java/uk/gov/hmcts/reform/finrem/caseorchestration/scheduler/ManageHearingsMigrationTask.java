package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
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
import java.util.function.Predicate;

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

    @Value("${cron.manageHearingsMigration.mhMigrationVersion:1}")
    private String mhMigrationVersion;

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
        String searchQuery = getSearchQuery(mhMigrationVersion);
        String systemUserToken = getSystemUserToken();

        SearchResult searchResult = ccdService.esSearchCases(getCaseType(), searchQuery, systemUserToken);
        log.info("{} non-migrated manage hearings cases found for {}", searchResult.getTotal(), CASE_TYPE.getCcdType());

        Predicate<FinremCaseDetails> filter = finremCaseDetails -> true;
        return extractCaseReferences(searchResult, filter);
    }

    private String getSearchQuery(String currentMigrationVersion) {
        // Clause 1: mhMigrationVersion does NOT exist (null)
        QueryBuilder mhMigrationVersionIsNull = QueryBuilders.boolQuery()
            .mustNot(QueryBuilders.existsQuery("data.mhMigrationVersion"));

        // Clause 2: mhMigrationVersion is NOT equal to "1"
        QueryBuilder mhMigrationVersionNotOne = QueryBuilders.boolQuery()
            .mustNot(new TermQueryBuilder("data.mhMigrationVersion.keyword", currentMigrationVersion));

        // Combine with OR (should)
        BoolQueryBuilder mhMigrationVersionCondition = QueryBuilders.boolQuery()
            .should(mhMigrationVersionIsNull)
            .should(mhMigrationVersionNotOne)
            .minimumShouldMatch(1); // At least one condition must match

        // Exclude cases in state = "close"
        BoolQueryBuilder stateQuery = QueryBuilders.boolQuery()
            .mustNot(new TermQueryBuilder("state.keyword", "close"));

        // Combine all queries
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
            .must(mhMigrationVersionCondition)
            .must(stateQuery);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            .size(batchSize)
            .query(boolQueryBuilder);

        return searchSourceBuilder.toString();
    }

    void setTaskEnabled(boolean taskEnabled) {
        this.taskEnabled = taskEnabled;
    }

    private List<CaseReference> extractCaseReferences(SearchResult searchResult, Predicate<FinremCaseDetails> filter) {
        return searchResult.getCases().stream()
            .map(finremCaseDetailsMapper::mapToFinremCaseDetails)
            .filter(filter)
            .map(caseDetails -> caseDetails.getId().toString())
            .map(CaseReference::new)
            .toList();
    }
}
