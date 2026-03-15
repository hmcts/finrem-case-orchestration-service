package uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry;

@FunctionalInterface
public interface RetryErrorHandler {
    void handle(Exception exception, String actionName, String caseId);
}
