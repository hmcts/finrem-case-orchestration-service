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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CfvUpdateTask extends BaseTask {

    private static final String TASK_NAME = "CfvUpdateTask";

    private static final List<State> STATES_TO_UPDATE =
        List.of(
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
            State.CONSENTED_ORDER_APPROVED,
            State.CONSENTED_ORDER_NOT_APPROVED,
            State.DRAFT_ORDER_NOT_APPROVED,
            State.GENERAL_APPLICATION,
            State.GENERAL_APPLICATION_OUTCOME,
            State.JUDGE_DRAFT_ORDER,
            State.ORDER_DRAWN,
            State.ORDER_MADE,
            State.ORDER_SENT,
            State.PAPER_CASE_ADDED,
            State.SOLICITOR_DRAFT_ORDER,
            State.RESPONSE_RECEIVED,
            State.SCHEDULE_RAISE_DIRECTIONS_ORDER,
            State.SCHEDULING_AND_HEARING
        );

    @Value("${cron.cfvUpdate.task.enabled:false}")
    private boolean taskEnabled;

    @Value("${cron.cfvUpdate.batchSize:50}")
    private int updateBatchSize;

    private final CfvUpdateTaskDocumentCategoriser documentCategoriser;

    public CfvUpdateTask(CcdService ccdService, SystemUserService systemUserService,
                         FinremCaseDetailsMapper finremCaseDetailsMapper,
                         CfvUpdateTaskDocumentCategoriser documentCategoriser) {
        super(ccdService, systemUserService, finremCaseDetailsMapper);
        this.documentCategoriser = documentCategoriser;
    }

    @Override
    public List<CaseReference> getCaseReferences() {
        log.info("Getting case references for CFV update");
        List<CaseReference> caseReferences = new ArrayList<>();
        try {
            String systemUserToken = getSystemUserToken();
            log.info("Getting case references for CFV update with system user token {}", systemUserToken);
            for (State state : STATES_TO_UPDATE) {
                log.info("Getting case references for state {} with case reference size {}", state, caseReferences.size());
                if (caseReferences.size() >= updateBatchSize) {
                    break;
                }
                int remaining = updateBatchSize - caseReferences.size();
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
        }
        return caseReferences;
    }

    private String buildSearchString(final String state, int pageSize) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
            .mustNot(QueryBuilders.existsQuery("data.cfvMigrationVersion"))
            .filter(new TermQueryBuilder("state.keyword", state));

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            .size(pageSize)
            .query(boolQueryBuilder);

        return searchSourceBuilder.toString();
    }

    private List<CaseReference> getCaseReferencesFromSearchResult(SearchResult searchResult) {
        List<CaseReference> caseReferences = new ArrayList<>();
        searchResult.getCases().forEach(caseDetails ->
            caseReferences.add(new CaseReference(caseDetails.getId().toString())));
        return caseReferences;
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
        return CaseType.CONTESTED;
    }

    @Override
    protected String getSummary() {
        return "Update cases for CFV - DFR-3032";
    }

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        FinremCaseData finremCaseData = finremCaseDetails.getData();
        log.info("Executing {} for case id {}", TASK_NAME, finremCaseDetails.getId());
        documentCategoriser.categoriseDocuments(finremCaseData);
        finremCaseData.getCfvMigrationWrapper().setCfvMigrationVersion("1");
        log.info("Executed {} for case id {}", TASK_NAME, finremCaseDetails.getId());
    }
}
