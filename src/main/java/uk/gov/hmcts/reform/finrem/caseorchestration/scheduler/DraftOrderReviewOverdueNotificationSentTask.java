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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DraftOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;

import java.util.List;

/**
 * Scheduled task to send notification about outstanding orders need review (contested).
 * To enable the task to execute set environment variables:
 * <ul>
 *     <li>CRON_DRAFT_ORDER_REVIEW_OVERDUE_NOTIFICATION_SENT_ENABLED=true</li>
 *     <li>TASK_NAME=DraftOrderReviewOverdueNotificationSentTask</li>
 *     <li>CRON_DRAFT_ORDER_REVIEW_OVERDUE_NOTIFICATION_SENT_BATCH_SIZE=number of cases to search for</li>
 *     <li>CRON_DRAFT_ORDER_REVIEW_OVERDUE_NOTIFICATION_SENT_DAYS_SINCE_ORDER_UPLOAD=The number of days
 *     after the agreed draft order or pension sharing annex upload before sending a notification to review outstanding orders.</li>
 * </ul>
 */
@Component
@Slf4j
public class DraftOrderReviewOverdueNotificationSentTask extends BaseTask {

    private static final String TASK_NAME = "DraftOrderReviewOverdueNotificationSentTask";
    private static final String SUMMARY = "Draft order review overdue notification sent";
    private static final String CASE_TYPE_ID = "FinancialRemedyContested";

    @Value("${cron.draftOrderReviewOverdueNotificationSent.enabled:false}")
    private boolean taskEnabled;

    @Value("${cron.draftOrderReviewOverdueNotificationSent.batchSize:500}")
    private int batchSize;

    @Value("${cron.draftOrderReviewOverdueNotificationSent.daysSinceOrderUpload:14}")
    private int daysSinceOrderUpload;

    private final NotificationService notificationService;
    private final DraftOrderService draftOrderService;

    protected DraftOrderReviewOverdueNotificationSentTask(CcdService ccdService, SystemUserService systemUserService,
                                                          FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                          NotificationService notificationService,
                                                          DraftOrderService draftOrderService) {
        super(ccdService, systemUserService, finremCaseDetailsMapper);
        this.notificationService = notificationService;
        this.draftOrderService = draftOrderService;
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
        return CaseType.forValue(CASE_TYPE_ID);
    }

    @Override
    protected String getSummary() {
        return SUMMARY;
    }

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        List<DraftOrdersReview> overdoneDraftOrderReviews =
            draftOrderService.getDraftOrderReviewOverdue(finremCaseDetails, daysSinceOrderUpload);

        if (overdoneDraftOrderReviews.isEmpty()) {
            log.info("{} - No draft order reviews overdue", finremCaseDetails.getId().toString());
        } else {
            overdoneDraftOrderReviews.forEach(draftOrderReview -> {
                notificationService.sendDraftOrderReviewOverdueToCaseworker(finremCaseDetails, draftOrderReview);
                finremCaseDetails.setData(draftOrderService.applyCurrentNotificationTimestamp(finremCaseDetails.getData(),
                    draftOrderReview));
            });
        }
    }

    @Override
    protected List<CaseReference> getCaseReferences() {
        String searchQuery = getSearchQuery();
        String systemUserToken = getSystemUserToken();

        SearchResult searchResult = ccdService.esSearchCases(getCaseType(), searchQuery, systemUserToken);
        log.info("{} 'To Be Reviewed' cases found for {}", searchResult.getTotal(), CASE_TYPE_ID);

        List<CaseReference> caseReferences = filterOverdueCases(searchResult);
        log.info("{} overdue cases found for {}", searchResult.getTotal(), CASE_TYPE_ID);
        return caseReferences;
    }

    private String getSearchQuery() {
        BoolQueryBuilder draftOrderOrderStatusQuery = QueryBuilders.boolQuery()
            .must(new TermQueryBuilder(
                "data.draftOrdersReviewCollection.value.draftOrderDocReviewCollection.value.orderStatus.keyword",
                OrderStatus.TO_BE_REVIEWED));
        BoolQueryBuilder psaOrderStatusQuery = QueryBuilders.boolQuery()
            .must(new TermQueryBuilder(
                "data.draftOrdersReviewCollection.value.psaDocReviewCollection.value.orderStatus.keyword",
                OrderStatus.TO_BE_REVIEWED));

        BoolQueryBuilder orderStatusQuery = QueryBuilders.boolQuery()
            .should(draftOrderOrderStatusQuery)
            .should(psaOrderStatusQuery)
            .minimumShouldMatch(1);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            .size(batchSize)
            .query(orderStatusQuery);

        return searchSourceBuilder.toString();
    }

    void setTaskEnabled(boolean taskEnabled) {
        this.taskEnabled = taskEnabled;
    }

    private List<CaseReference> filterOverdueCases(SearchResult searchResult) {
        return searchResult.getCases().stream()
            .map(finremCaseDetailsMapper::mapToFinremCaseDetails)
            .filter(caseDetails -> draftOrderService.isDraftOrderReviewOverdue(caseDetails, daysSinceOrderUpload))
            .map(caseDetails -> caseDetails.getId().toString())
            .map(CaseReference::new)
            .toList();
    }
}
