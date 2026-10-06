package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.INTERNAL_CHANGE_UPDATE_CASE;

/**
 * Orchestrates correspondence events and auditing for callback handlers.
 *
 * <p>The expected correspondence flow is:
 * <ol>
 *     <li>Create pending audit rows before submit</li>
 *     <li>Publish correspondence event on submit</li>
 *     <li>Reconcile and persist audit rows after successful publish</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class CorrespondenceEventAuditOrchestrationService {

    private final RetryExecutor retryExecutor;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final NotificationAuditService notificationAuditService;
    private final CoreCaseDataService coreCaseDataService;

    /**
     * Creates pending notification audit rows for a correspondence event.
     *
     * @param event correspondence event
     * @param eventType callback event type used for audit metadata
     */
    public void createPendingAudits(SendCorrespondenceEvent event, EventType eventType) {
        notificationAuditService.createAuditsForCorrespondence(event, eventType);
    }

    /**
     * Publishes a correspondence event with retries.
     *
     * @param event correspondence event to publish
     * @param actionName action label used for retry/audit logging
     * @return true when publishing succeeds; false when retries are exhausted
     */
    public boolean publishEvent(SendCorrespondenceEvent event, String actionName) {
        AtomicBoolean success = new AtomicBoolean(true);
        publishEvent(event, actionName, () -> success.set(false));
        return success.get();
    }

    /**
     * Publishes a correspondence event with retry, invoking a callback if publishing
     * ultimately fails.
     *
     * @param event the correspondence event to publish
     * @param actionName action label used for retry/audit logging
     * @param onFailure invoked once if publishing still fails after retries
     */
    public void publishEvent(SendCorrespondenceEvent event, String actionName, Runnable onFailure) {
        retryExecutor.runWithRetryWithHandler(
            () -> applicationEventPublisher.publishEvent(event),
            actionName,
            event.getCaseId(),
            (exception, action, caseId) -> onFailure.run()
        );
    }

    /**
     * Reconciles notification audit rows and persists any updates to CCD.
     *
     * @param caseDetails case details used for post-submit update
     * @param event published correspondence event used for reconciliation
     * @param actionName action label used for retry/audit logging
     * @deprecated use
     *     {@link #reconcileAndPersistAudits(FinremCaseDetails, String, SendCorrespondenceEvent...)}
     *     instead, which accepts one or more events in a single call.
     */
    @Deprecated(forRemoval = true)
    public void reconcileAndPersistAudits(FinremCaseDetails caseDetails,
                                          SendCorrespondenceEvent event,
                                          String actionName) {
        reconcileAndPersistAudits(caseDetails, actionName, event);
    }

    /**
     * Reconciles notification audit rows for one or more correspondence events and persists
     * any resulting updates to CCD in a single post-submit callback.
     *
     * <p>If reconciliation produces no updates, CCD is not called. Failures while persisting
     * are retried and then suppressed, so this method does not throw if the CCD update
     * ultimately fails.</p>
     *
     * @param caseDetails case details used for the post-submit update
     * @param actionName action label used for retry/audit logging
     * @param events published correspondence events used for reconciliation
     */
    public void reconcileAndPersistAudits(FinremCaseDetails caseDetails,
                                          String actionName,
                                          SendCorrespondenceEvent... events) {
        Map<String, Object> updatedFields = notificationAuditService.reconcileNotificationAudits(events);

        if (!updatedFields.isEmpty()) {
            retryExecutor.runWithRetrySuppressException(
                () -> coreCaseDataService.performPostSubmitCallback(
                    caseDetails.getCaseType(),
                    caseDetails.getId(),
                    INTERNAL_CHANGE_UPDATE_CASE.getCcdType(),
                    latestCaseDetails -> updatedFields
                ),
                actionName,
                caseDetails.getCaseIdAsString()
            );
        }
    }
}
