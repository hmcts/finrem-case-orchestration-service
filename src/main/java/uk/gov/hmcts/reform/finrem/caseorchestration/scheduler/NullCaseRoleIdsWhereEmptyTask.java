package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.*;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;

import java.util.List;
import java.util.Optional;

/**
 * Scheduled task to find cases where the CaseRoleId is "CaseRoleId": {"value": null, "list_items": null}.
 * Then update so that CaseRoleId becomes "CaseRoleId": null"
 * To enable the task to execute set environment variables:
 * <ul>
 *     <li>CRON_NULL_CASEROLEIDS_WHERE_EMPTY_ENABLED=true</li>
 *     <li>TASK_NAME=NullCaseRoleIdsWhereEmptyTask</li>
 *     <li>CRON_NULL_CASEROLEIDS_WHERE_EMPTY_CASE_TYPE_ID=FinancialRemedyContested | FinancialRemedyMVP2</li>
 *     <li>CRON_NULL_CASEROLEIDS_WHERE_EMPTY_BATCH_SIZE=number of cases to search for</li>
 * </ul>
 */
@Component
@Slf4j
public class NullCaseRoleIdsWhereEmptyTask extends BaseTask {

    private static final String TASK_NAME = "NullCaseRoleIdsWhereEmptyTask";
    private static final String SUMMARY = "DFR-3351";

    @Value("${cron.nullCaseRoleIdsWhereEmpty.enabled:false}")
    private boolean taskEnabled;

    @Value("${cron.nullCaseRoleIdsWhereEmpty.caseTypeId:FinancialRemedyContested}")
    private String caseTypeId;
    @Value("${cron.nullCaseRoleIdsWhereEmpty.batchSize:500}")
    private int batchSize;

    protected NullCaseRoleIdsWhereEmptyTask(CcdService ccdService, SystemUserService systemUserService,
                                            FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(ccdService, systemUserService, finremCaseDetailsMapper);
    }

    @Override
    protected List<CaseReference> getCaseReferences() {
        String searchQuery = getSearchQuery();
        String systemUserToken = getSystemUserToken();
        SearchResult searchResult = ccdService.esSearchCases(getCaseType(), searchQuery, systemUserToken);
        log.info("{} cases found for {}", searchResult.getTotal(), caseTypeId);

        return searchResult.getCases().stream()
            .map(caseDetails -> caseDetails.getId().toString())
            .map(CaseReference::new)
            .toList();
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
        return CaseType.forValue(caseTypeId);
    }

    @Override
    protected String getSummary() {
        return SUMMARY;
    }

    // Needs tweaking.  Acts on caseRoleId = null - should skip those.  Easy refactor.
    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        FinremCaseData caseData = finremCaseDetails.getData();

        if (caseData.getChangeOrganisationRequestField() != null
                && caseData.getChangeOrganisationRequestField().getCaseRoleId() != null
                && caseData.getChangeOrganisationRequestField().getCaseRoleId().getValue() == null) {

            // log to be removed when logic confirmed as working
            log.info("Executing NullCaseRoleIdsWhereEmptyTask for {}", finremCaseDetails.getId());
            // then do it
            // caseData.setCcdCaseId(String.valueOf(finremCaseDetails.getId()));
            // caseData.getChangeOrganisationRequestField().setCaseRoleId(null);

        }

        // This optional code needs enhancement.
        // when the caseRoleId or is null (which doesn't need attention) get value stills run so expression is true.
        // same if the ChangeOrganisationRequest is null.
        if (Optional.ofNullable(caseData.getChangeOrganisationRequestField())
                .map(ChangeOrganisationRequest::getCaseRoleId)
                .map(DynamicList::getValue)
                .isEmpty()) {
            log.info("gone in - optional");
        }
    }

    private String getSearchQuery() {

        // Check that CaseRoleId is present in ES, indicating that postgres contains something not null.
        // BoolQueryBuilder caseRoleIdExistsQuery = QueryBuilders.boolQuery()
        // .must(QueryBuilders.existsQuery("data.changeOrganisationRequestField.CaseRoleId"));

        // Check that CaseRoleId.value is not present in ES, indicating that Postgres contains something null.
        // BoolQueryBuilder caseRoleIdValueMissingQuery = QueryBuilders.boolQuery()
        // .mustNot(QueryBuilders.existsQuery("data.changeOrganisationRequestField.CaseRoleId.value"));

        // BoolQueryBuilder stateQuery = QueryBuilders.boolQuery()
        // .mustNot(new TermQueryBuilder("state.keyword", "close"));

        // BoolQueryBuilder finalQueryBuilder = QueryBuilders.boolQuery()
        // .must(caseRoleIdExistsQuery)
        // .must(stateQuery);
        //
        // SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
        // .size(batchSize)
        // .query(finalQueryBuilder);

        //above is what I want - below is debugging

//        ExistsQueryBuilder changeOrganisationRequestFieldExistsQuery = QueryBuilders.existsQuery("data.changeOrganisationRequestField");
        BoolQueryBuilder stateQuery = QueryBuilders.boolQuery()
                .mustNot(new TermQueryBuilder("state.keyword", "close"));

        BoolQueryBuilder RespondemntRequestFieldExistsQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.existsQuery("data.RespondentOrganisationPolicy"));

        BoolQueryBuilder changeOrganisationRequestFieldExistsQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.existsQuery("data.changeOrganisationRequestField"));

        BoolQueryBuilder finalQueryBuilder = QueryBuilders.boolQuery()
        .must(RespondemntRequestFieldExistsQuery)
//        .must(changeOrganisationRequestFieldExistsQuery)
        .must(stateQuery);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(finalQueryBuilder);


        return searchSourceBuilder.toString();
    }

    void setTaskEnabled(boolean taskEnabled) {
        this.taskEnabled = taskEnabled;
    }

    void setCaseTypeContested() {
        this.caseTypeId = CaseType.CONTESTED.getCcdType();
    }
}
