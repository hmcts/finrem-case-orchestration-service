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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    public static final String NOC_FIX_APPLIED_FLAG_FIELD = "isNocFixAppliedFlag";
    private static final String CASE_DATA_NOC_FIX_APPLIED_FLAG = String.format("data.%s", NOC_FIX_APPLIED_FLAG_FIELD);

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
        log.info("Getting case references for NOC migration");
        String searchQuery = getSearchQuery();

        String systemUserToken = getSystemUserToken();
        SearchResult searchResult = ccdService.esSearchCases(getCaseType(), searchQuery, systemUserToken);

        log.info("{} cases found for {}, returning first {} for NOC Fix", searchResult.getTotal(), caseTypeId, batchSize);

        List<CaseReference> caseReferences = searchResult.getCases().stream()
                .limit(batchSize)
                .map(caseDetails -> caseDetails.getId().toString())
                .map(CaseReference::new)
                .toList();

        log.info("caseReferences has {} cases.", caseReferences.size());

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
        return CaseType.forValue(caseTypeId);
    }

    @Override
    protected String getSummary() {
        return SUMMARY;
    }

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        FinremCaseData caseData = finremCaseDetails.getData();
        ChangeOrganisationRequest noc = caseData.getChangeOrganisationRequestField();

        if (noc != null && noc.getCaseRoleId() != null && noc.getCaseRoleId().getValue() == null) {
            log.info("Case {} will have caseRoleId set to null", finremCaseDetails.getId());
            caseData.setCcdCaseId(String.valueOf(finremCaseDetails.getId()));
            caseData.getChangeOrganisationRequestField().setCaseRoleId(null);
            caseData.setIsNocFixAppliedFlag(YesOrNo.YES);
        } else {
            log.info("Case {} not affected by caseRoleId NOC bug", finremCaseDetails.getId());
            caseData.setIsNocFixAppliedFlag(YesOrNo.NO);
        }
    }

    private String getSearchQuery() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        LocalDateTime nocBugReleaseDateTime = LocalDateTime.parse("30-07-2024 23:59", formatter);

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
            .mustNot(QueryBuilders.existsQuery(CASE_DATA_NOC_FIX_APPLIED_FLAG))
            .mustNot(new TermQueryBuilder("state.keyword", "close"))
            .filter(QueryBuilders.rangeQuery("last_modified").from(nocBugReleaseDateTime));

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            .query(boolQueryBuilder)
            .size(batchSize);

        return searchSourceBuilder.toString();
    }

    void setTaskEnabled(boolean taskEnabled) {
        this.taskEnabled = taskEnabled;
    }

    void setCaseTypeContested() {
        this.caseTypeId = CaseType.CONTESTED.getCcdType();
    }
}
