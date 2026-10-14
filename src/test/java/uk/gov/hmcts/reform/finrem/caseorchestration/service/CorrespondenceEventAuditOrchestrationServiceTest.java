package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryErrorHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.ThrowingRunnable;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.INTERNAL_CHANGE_UPDATE_CASE;

@ExtendWith(MockitoExtension.class)
class CorrespondenceEventAuditOrchestrationServiceTest {

    private static final String CASE_ID = "12345";
    private static final String ACTION_NAME = "test action";

    @Mock
    private RetryExecutor retryExecutor;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private NotificationAuditService notificationAuditService;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

    @Test
    void shouldCreatePendingAudits() {
        SendCorrespondenceEvent event = event();

        correspondenceEventAuditOrchestrationService
            .createPendingAudits(event, EventType.CUI_APPLICANT_DOCUMENT_UPLOAD);

        verify(notificationAuditService)
            .createAuditsForCorrespondence(event, EventType.CUI_APPLICANT_DOCUMENT_UPLOAD);
    }

    @Test
    void shouldReturnTrueWhenPublishEventSucceeds() {
        SendCorrespondenceEvent event = event();

        doAnswer(invocation -> {
            ThrowingRunnable action = invocation.getArgument(0);
            action.run();
            return null;
        }).when(retryExecutor)
            .runWithRetryWithHandler(any(ThrowingRunnable.class), eq(ACTION_NAME), eq(CASE_ID), any(RetryErrorHandler.class));

        boolean published = correspondenceEventAuditOrchestrationService.publishEvent(event, ACTION_NAME);

        assertThat(published).isTrue();
        verify(applicationEventPublisher).publishEvent(event);
    }

    @Test
    void shouldReturnFalseWhenPublishEventFails() {
        SendCorrespondenceEvent event = event();

        doAnswer(invocation -> {
            RetryErrorHandler errorHandler = invocation.getArgument(3);
            errorHandler.handle(new RuntimeException("publish failed"), ACTION_NAME, CASE_ID);
            return null;
        }).when(retryExecutor)
            .runWithRetryWithHandler(any(ThrowingRunnable.class), eq(ACTION_NAME), eq(CASE_ID), any(RetryErrorHandler.class));

        boolean published = correspondenceEventAuditOrchestrationService
            .publishEvent(event, ACTION_NAME);

        assertThat(published).isFalse();
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldNotPersistAuditsWhenNoUpdatedFieldsExist() {
        SendCorrespondenceEvent event = event();
        FinremCaseDetails caseDetails = caseDetails();

        when(notificationAuditService.reconcileNotificationAudits(event)).thenReturn(Map.of());

        correspondenceEventAuditOrchestrationService
            .reconcileAndPersistAudits(caseDetails, event, ACTION_NAME);

        verify(retryExecutor, never()).runWithRetrySuppressException(any(), any(), any());
        verify(coreCaseDataService, never()).performPostSubmitCallback(any(), any(), any(), any());
    }

    @Test
    void shouldPersistAuditsWhenUpdatedFieldsExist() {
        SendCorrespondenceEvent event = event();
        FinremCaseDetails caseDetails = caseDetails();
        Map<String, Object> updatedFields = Map.of("notificationsAudits", "updated");

        when(notificationAuditService.reconcileNotificationAudits(event)).thenReturn(updatedFields);
        doAnswer(invocation -> {
            ThrowingRunnable action = invocation.getArgument(0);
            action.run();
            return null;
        }).when(retryExecutor).runWithRetrySuppressException(any(), eq(ACTION_NAME), eq(CASE_ID));

        correspondenceEventAuditOrchestrationService
            .reconcileAndPersistAudits(caseDetails, event, ACTION_NAME);

        verify(retryExecutor).runWithRetrySuppressException(any(), eq(ACTION_NAME), eq(CASE_ID));
        verify(coreCaseDataService).performPostSubmitCallback(
            eq(CaseType.CONTESTED),
            eq(12345L),
            eq(INTERNAL_CHANGE_UPDATE_CASE.getCcdType()),
            any()
        );
    }

    private SendCorrespondenceEvent event() {
        return SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails())
            .build();
    }

    private FinremCaseDetails caseDetails() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseId(CASE_ID);

        return FinremCaseDetails.builder()
            .id(12345L)
            .caseType(CaseType.CONTESTED)
            .data(caseData)
            .build();
    }
}
