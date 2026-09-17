package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryErrorHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.INTERNAL_CHANGE_UPDATE_CASE;

@Service
@RequiredArgsConstructor
public class CorrespondenceEventAuditOrchestrationService {

    private final RetryExecutor retryExecutor;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final NotificationAuditService notificationAuditService;
    private final CoreCaseDataService coreCaseDataService;

    public void createPendingAudits(Optional<SendCorrespondenceEvent> event, EventType eventType) {
        event.ifPresent(sendCorrespondenceEvent ->
            notificationAuditService.createAuditsForCorrespondence(sendCorrespondenceEvent, eventType)
        );
    }

    public boolean publishEvent(SendCorrespondenceEvent event, String actionName) {
        AtomicBoolean success = new AtomicBoolean(true);

        retryExecutor.runWithRetryWithHandler(
            () -> applicationEventPublisher.publishEvent(event),
            actionName,
            event.getCaseId(),
            (exception, action, caseId) -> {
                success.set(false);
            }
        );

        return success.get();
    }

    public void reconcileAndPersistAudits(FinremCaseDetails caseDetails,
                                          SendCorrespondenceEvent event,
                                          String actionName) {
        Map<String, Object> updatedFields = notificationAuditService.reconcileNotificationAudits(event);

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
