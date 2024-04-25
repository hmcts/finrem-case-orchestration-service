package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
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

    private static List<State> STATES_TO_CATEGORISE =
        List.of(
            State.SOLICITOR_DRAFT_ORDER,
            State.PREPARE_FOR_HEARING,
            State.CASE_FILE_SUBMITTED,
            State.GATE_KEEPING_AND_ALLOCATION,
            State.GENERAL_APPLICATION_AWAITING_JUDICIARY_RESPONSE,
            State.REVIEW_ORDER,
            State.APPLICATION_SUBMITTED,
            State.AWAITING_JUDICIARY_RESPONSE,
            State.AWAITING_JUDICIARY_RESPONSE_CONSENT,
            State.CONSENTED_ORDER_ASSIGN_JUDGE,
            State.AWAITING_RESPONSE,
            State.AWAITING_HWF_DECISION,
            State.APPLICATION_ISSUED,
            State.AWAITING_PAYMENT_RESPONSE,
            State.AWAITING_PAYMENT,
            State.CASE_ADDED,
            State.CONSENT_ORDER_APPROVED,
            State.CONSENT_ORDER_NOT_APPROVED,
            State.CONSENTED_ORDER_SUBMITTED,
            State.DRAFT_ORDER_NOT_APPROVED,
            State.GENERAL_APPLICATION,
            State.GENERAL_APPLICATION_OUTCOME,
            State.JUDGE_DRAFT_ORDER,
            State.ORDER_DRAWN,
            State.ORDER_MADE,
            State.ORDER_SENT,
            State.PAPER_CASE_ADDED,
            State.RESPONSE_RECEIVED,
            State.SCHEDULE_RAISE_DIRECTIONS_ORDER,
            State.SCHEDULING_AND_HEARING
        );

    @Value("${cron.cfvCategorisation.task.enabled:true}")
    private boolean isCfvMigrationTaskEnabled;

    @Value("${cron.cfvCategorisation.batchSize:50}")
    private int cfvCategorisationBatchSize;

    @Value("${cron.cfvCategorisation.cfvReleaseDate:01-03-2024 23:59}")
    private String cfvReleaseDate;

    private final DocumentCategoryAssigner documentCategoryAssigner;

    protected CfvMigrationTask(CcdService ccdService, SystemUserService systemUserService,
                               FinremCaseDetailsMapper finremCaseDetailsMapper, DocumentCategoryAssigner documentCategoryAssigner) {
        super(ccdService, systemUserService, finremCaseDetailsMapper);
        this.documentCategoryAssigner = documentCategoryAssigner;
    }

    @Override
    public List<CaseReference> getCaseReferences() {
        log.info("Getting case references for CFV migration");
        List<CaseReference> caseReferences = new ArrayList<>();
        try {
            String systemUserToken = getSystemUserToken();
            log.info("Getting case references for CFV migration with system user token {}", systemUserToken);
            for (State state : STATES_TO_CATEGORISE) {
                log.info("Getting case references for state {} with case reference size {}", state, caseReferences.size());
                if (caseReferences.size() >= cfvCategorisationBatchSize) {
                    break;
                }
                int remaining = cfvCategorisationBatchSize - caseReferences.size();
                log.info("Getting case references for state {} with remaining case reference size {}", state, remaining);
                String esSearchString = buildSearchString(state.getStateId(), remaining);
                log.info("Getting case references for state {} with search string {}", state, esSearchString);
                try {
                    SearchResult searchResult = ccdService.esSearchCases(CaseType.CONTESTED, esSearchString, systemUserToken);
                    log.info("Getting case references for state {} with search result total {}", state, searchResult.getTotal());
                    caseReferences.addAll(getCaseReferencesFromSearchResult(searchResult));
                } catch (RuntimeException e) {
                    log.error("Error occurred while searching for state {}", state, e);
                }
            }
        } catch (RuntimeException e) {
            log.error("Error occurred while running CFV migration task", e);
            e.printStackTrace();
        }
        return caseReferences;
    }

    private String buildSearchString(final String state, int pageSize) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        LocalDateTime cfvReleaseDateTime = LocalDateTime.parse(cfvReleaseDate, formatter);

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
            .mustNot(QueryBuilders.existsQuery(CASE_DATA_CFV_CATEGORIES_APPLIED_FLAG))
            .filter(QueryBuilders.rangeQuery("created_date").lt(cfvReleaseDateTime))
            .filter(new TermQueryBuilder("state.keyword", state));

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            .size(pageSize)
            .query(boolQueryBuilder);

        return searchSourceBuilder.toString();
    }

    private List<CaseReference> getCaseReferencesFromSearchResult(SearchResult searchResult) {
        List<CaseReference> caseReferences = new ArrayList<>();
        searchResult.getCases().forEach(caseDetails -> log.info("Found case {}", caseDetails.getId()));
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
        log.info("Executing {} for case id {}", getTaskName(), finremCaseDetails.getId());
        documentCategoryAssigner.assignDocumentCategories(finremCaseData);
        finremCaseData.setIsCfvCategoriesAppliedFlag(YesOrNo.YES);
        log.info("Executed {} for case id {}", getTaskName(), finremCaseDetails.getId());

    }
}
