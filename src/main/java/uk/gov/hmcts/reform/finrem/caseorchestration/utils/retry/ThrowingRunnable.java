package uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry;

@FunctionalInterface
public interface ThrowingRunnable {
    void run() throws Exception;
}
