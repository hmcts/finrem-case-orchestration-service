package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.List;

import static java.util.Optional.ofNullable;

public abstract class FinremSubmittedCallbackHandler extends FinremCallbackHandler {

    protected final EvidenceManagementDeleteService evidenceManagementDeleteService;

    protected final RetryExecutor retryExecutor;

    protected FinremSubmittedCallbackHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                             EvidenceManagementDeleteService evidenceManagementDeleteService,
                                             RetryExecutor retryExecutor) {
        super(finremCaseDetailsMapper);
        this.evidenceManagementDeleteService = evidenceManagementDeleteService;
        this.retryExecutor = retryExecutor;
    }

    @Override
    protected final boolean shouldClearTemporaryFieldsAfterHandle() {
        throw new IllegalStateException("Should not be called");
    }

    /**
     * Prevents the bin from being cleared before the handler executes.
     *
     * <p>This override is required because the submitted handler performs physical
     * file cleanup during {@code postHandle}. Clearing the bin before handling would
     * remove the file references required for deletion from the Evidence Management
     * service.
     *
     * @return {@code false} to retain the bin contents until {@code postHandle} completes
     */
    @Override
    protected final boolean shouldClearBinBeforeHandle() {
        return false;
    }

    /**
     * Deletes all physical files selected for removal from the Evidence Management service
     * after the callback event has been processed.
     *
     * <p>This implementation completely overrides the parent {@code postHandle} method
     * and does not execute the parent logic that removes temporary fields after handling.
     *
     * <p>The method retrieves the list of file URLs marked for deletion from the
     * {@code fileUrlsToBeDeleted} dynamic list within the case data bin. For each URL,
     * it attempts to delete the corresponding file using the
     * {@code evidenceManagementDeleteService}.
     *
     * <p>File deletion is executed through the retry executor to provide resilience
     * against transient failures. Any exception raised during deletion is suppressed
     * after all retry attempts are exhausted, allowing callback processing to continue
     * without interruption.
     *
     * @param response the callback response returned from the main handler logic
     * @param finremCaseData the case data containing the file URLs to delete
     * @param userAuthorisation the user authorisation token used to authenticate
     *                          deletion requests
     * @return the original callback response
     */
    @Override
    protected GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> postHandle(
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response,
        FinremCaseData finremCaseData, String userAuthorisation) {
        ofNullable(finremCaseData.getBin().getFileUrlsToBeDeleted())
            .map(DynamicList::getListItems)
            .stream()
            .flatMap(List::stream)
            .map(DynamicListElement::getCode).forEach(url ->
                retryExecutor.runWithRetrySuppressException(
                    () -> evidenceManagementDeleteService.delete(url, userAuthorisation),
                    "Physical File Deletion - %s".formatted(url), finremCaseData.getCcdCaseId())
            );
        return response;
    }
}
