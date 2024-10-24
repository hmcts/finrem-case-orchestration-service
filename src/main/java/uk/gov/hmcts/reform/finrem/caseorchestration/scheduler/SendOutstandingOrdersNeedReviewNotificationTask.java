package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
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
 *     <li>CRON_SEND_OUTSTANDING_ORDERS_NEED_REVIEW_ENABLED=true</li>
 *     <li>TASK_NAME=SendOutstandingOrdersNeedReviewNotificationTask</li>
 *     <li>CRON_SEND_OUTSTANDING_ORDERS_NEED_REVIEW_BATCH_SIZE=number of cases to search for</li>
 *     <li>CRON_SEND_OUTSTANDING_ORDERS_NEED_REVIEW_DAYS_SINCE_ORDER_UPLOAD=The number of days
 *     after the agreed draft order or pension sharing annex upload before sending a notification to review outstanding orders.</li>
 * </ul>
 */
@Component
@Slf4j
public class SendOutstandingOrdersNeedReviewNotificationTask extends BaseTask {

    private static final String TASK_NAME = "SendOutstandingOrdersNeedReviewNotificationTask";
    private static final String SUMMARY = "DFR-3329";
    private static final String CASE_TYPE_ID = "FinancialRemedyContested";

    @Value("${cron.sendOutstandingOrdersNeedReviewNotification.enabled:false}")
    private boolean taskEnabled;

    @Value("${cron.sendOutstandingOrdersNeedReviewNotification.batchSize:500}")
    private int batchSize;

    @Value("${cron.sendOutstandingOrdersNeedReviewNotification.daysSinceOrderUpload:14}")
    private int daysSinceOrderUpload;

    private final NotificationService notificationService;

    private final DraftOrderService draftOrderService;

    protected SendOutstandingOrdersNeedReviewNotificationTask(CcdService ccdService, SystemUserService systemUserService,
                                                              FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                              NotificationService notificationService,
                                                              DraftOrderService draftOrderService) {
        super(ccdService, systemUserService, finremCaseDetailsMapper);
        this.notificationService = notificationService;
        this.draftOrderService = draftOrderService;
    }

    @Override
    protected List<CaseReference> getCaseReferences() {
        String searchQuery = getSearchQuery();
        String systemUserToken = getSystemUserToken();
        SearchResult searchResult = ccdService.esSearchCases(getCaseType(), searchQuery, systemUserToken);
        log.info("{} cases found for {}", searchResult.getTotal(), CASE_TYPE_ID);

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
        return CaseType.forValue(CASE_TYPE_ID);
    }

    @Override
    protected String getSummary() {
        return SUMMARY;
    }

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        draftOrderService.getOutstandingOrdersToBeReviewed(finremCaseDetails, daysSinceOrderUpload).forEach(draftOrderReview ->
                notificationService.sendContestedOutstandingOrdersNeedReviewEmailToCaseworker(finremCaseDetails, draftOrderReview));
    }

    private String getSearchQuery() {
        // TODO Elastic Query
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            .size(batchSize) //.query(boolQueryBuilder)
            ;

        return searchSourceBuilder.toString();
    }

    void setTaskEnabled(boolean taskEnabled) {
        this.taskEnabled = taskEnabled;
    }

}
