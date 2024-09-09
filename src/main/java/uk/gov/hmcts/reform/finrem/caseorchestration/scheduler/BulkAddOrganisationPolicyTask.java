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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;

import java.util.List;

/**
 * Scheduled task to add ApplicantOrganisationPolicy and RespondentOrganisationPolicy
 * data to cases where they is missing.
 * To enable the task to execute set environment variables:
 * <ul>
 *     <li>CRON_BULK_ADD_ORGANISATION_POLICY_ENABLED=true</li>
 *     <li>TASK_NAME=BulkAddOrganisationPolicyTask</li>
 *     <li>CRON_BULK_ADD_ORGANISATION_POLICY_CASE_TYPE_ID=FinancialRemedyContested | FinancialRemedyMVP2</li>
 *     <li>CRON_BULK_ADD_ORGANISATION_POLICY_BATCH_SIZE=number of cases to search for</li>
 * </ul>
 */
@Component
@Slf4j
public class BulkAddOrganisationPolicyTask extends BaseTask {

    private static final String TASK_NAME = "BulkAddOrganisationPolicyTask";
    private static final String SUMMARY = "DFR-3261";

    @Value("${cron.bulkAddOrganisationPolicy.enabled:false}")
    private boolean taskEnabled;

    @Value("${cron.bulkAddOrganisationPolicy.caseTypeId:FinancialRemedyContested}")
    private String caseTypeId;
    @Value("${cron.bulkAddOrganisationPolicy.batchSize:500}")
    private int batchSize;

    private final UpdateRepresentationWorkflowService updateRepresentationWorkflowService;

    protected BulkAddOrganisationPolicyTask(CcdService ccdService, SystemUserService systemUserService,
                                            FinremCaseDetailsMapper finremCaseDetailsMapper,
                                            UpdateRepresentationWorkflowService updateRepresentationWorkflowService) {
        super(ccdService, systemUserService, finremCaseDetailsMapper);
        this.updateRepresentationWorkflowService = updateRepresentationWorkflowService;
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

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        FinremCaseData caseData = finremCaseDetails.getData();
        caseData.setCcdCaseId(String.valueOf(finremCaseDetails.getId()));
        updateRepresentationWorkflowService.persistDefaultOrganisationPolicy(finremCaseDetails.getData());
    }

    private String getSearchQuery() {
        BoolQueryBuilder applicantOrganisationPolicyQuery = QueryBuilders.boolQuery()
            .mustNot(QueryBuilders.existsQuery("data.ApplicantOrganisationPolicy"));
        BoolQueryBuilder respondentOrganisationPolicyQuery = QueryBuilders.boolQuery()
            .mustNot(QueryBuilders.existsQuery("data.RespondentOrganisationPolicy"));

        BoolQueryBuilder missingOrganisationPolicyQuery = QueryBuilders.boolQuery()
            .minimumShouldMatch(1)
            .should(applicantOrganisationPolicyQuery)
            .should(respondentOrganisationPolicyQuery);

        BoolQueryBuilder stateQuery = QueryBuilders.boolQuery()
            .mustNot(new TermQueryBuilder("state.keyword", "close"));

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
            .must(missingOrganisationPolicyQuery)
            .must(stateQuery);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            .size(batchSize)
            .query(boolQueryBuilder);

        return searchSourceBuilder.toString();
    }

    void setTaskEnabled(boolean taskEnabled) {
        this.taskEnabled = taskEnabled;
    }

    void setCaseTypeContested() {
        this.caseTypeId = CaseType.CONTESTED.getCcdType();
    }
}
