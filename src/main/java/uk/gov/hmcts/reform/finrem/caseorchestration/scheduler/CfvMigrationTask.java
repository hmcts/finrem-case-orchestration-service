package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.DocumentCategoryAssigner;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CfvMigrationTask extends BaseTask {


    public static final String CFV_CATEGORIES_APPLIED_FLAG_FIELD = "isCfvCategoriesAppliedFlag";
    private static final String CASE_DATA_CFV_CATEGORIES_APPLIED_FLAG = String.format("data.%s", CFV_CATEGORIES_APPLIED_FLAG_FIELD);

    private static List<State> STATES_TO_CATEGORISE = List.of(State.PREPARE_FOR_HEARING, State.ORDER_MADE, State.AWAITING_RESPONSE);

    @Value("${cron.cfvCategorisation.task.enabled:false}")
    private boolean isCfvMigrationTaskEnabled;

    @Value("${cron.cfvCategorisation.batchSize:50}")
    private int cfvCategorisationBatchSize;

    @Value("${cron.cfvCategorisation.cfvReleaseDate:01-01-2023}")
    private static String cfvReleaseDate;

    private final DocumentCategoryAssigner documentCategoryAssigner;

    protected CfvMigrationTask(CcdService ccdService, SystemUserService systemUserService,
                               FinremCaseDetailsMapper finremCaseDetailsMapper, DocumentCategoryAssigner documentCategoryAssigner) {
        super(ccdService, systemUserService, finremCaseDetailsMapper);
        this.documentCategoryAssigner = documentCategoryAssigner;
    }


    @Override
    public List<CaseReference> getCaseReferences() {
        List<CaseReference> caseReferences = new ArrayList<>();
        String systemUserToken = getSystemUserToken();
        for (State state : STATES_TO_CATEGORISE) {
            if (caseReferences.size() >= cfvCategorisationBatchSize) {
                break;
            }
            int remaining = cfvCategorisationBatchSize - caseReferences.size();
            String esSearchString = buildSearchString(state.toString(), remaining);
            SearchResult searchResult = ccdService.esSearchCases(CaseType.CONTESTED, esSearchString, systemUserToken);
            caseReferences.addAll(getCaseReferencesFromSearchResult(searchResult));
        }
        return caseReferences;
    }


    private String buildSearchString(final String state, int pageSize) {


        BoolQueryBuilder cfvFlagMustNotExist = QueryBuilders
            .boolQuery()
            .mustNot(QueryBuilders.existsQuery(CASE_DATA_CFV_CATEGORIES_APPLIED_FLAG));


        QueryBuilder stateQuery = QueryBuilders.matchQuery("state", state);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-YYYY");
        LocalDateTime cfvReleaseDateTime = LocalDateTime.parse(cfvReleaseDate, formatter);
        QueryBuilder caseCreatedDateBeforeCfv = QueryBuilders.rangeQuery("created_date").lt(cfvReleaseDateTime);

        QueryBuilder query = QueryBuilders
            .boolQuery()
            .must(stateQuery)
            .must(cfvFlagMustNotExist)
            .must(caseCreatedDateBeforeCfv);

        int from = 0;

        SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .query(query)
            .from(from)
            .size(pageSize);

        return sourceBuilder.toString();
    }

    private List<CaseReference> getCaseReferencesFromSearchResult(SearchResult searchResult) {
        List<CaseReference> caseReferences = new ArrayList<>();
        searchResult.getCases().forEach(caseDetails -> {
            caseReferences.add(new CaseReference(caseDetails.getId().toString()));
        });
        return caseReferences;
    }

    @Override
    protected String getTaskName() {
        return "CfvMigrationTask";
    }

    @Override
    protected boolean isTaskEnabled() {
        return isCfvMigrationTaskEnabled;
    }

    @Override
    protected CaseType getCaseType() {
        return CaseType.CONTESTED;
    }

    @Override
    protected String getSummary() {
        return "Categorise cases for CFV migration - DFR-2368";
    }

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        FinremCaseData finremCaseData = finremCaseDetails.getData();
        documentCategoryAssigner.assignDocumentCategories(finremCaseData);
        finremCaseData.setIsCfvCategoriesAppliedFlag(YesOrNo.YES);

    }
}
