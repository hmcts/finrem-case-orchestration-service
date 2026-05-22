package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
     * Deletes files that have been marked for removal from the Evidence Management service
     * after the callback event has been processed.
     *
     * <p>This implementation overrides the parent {@code postHandle} method and intentionally does not
     * execute the parent logic that clears temporary fields after handling, as cleanup is delegated
     * to this post-processing step.
     *
     * <p>The method delegates deletion processing to {@code purgeBinFileUrls}, which:
     * <ul>
     *   <li>extracts file URLs from the {@code fileUrlsToBeDeleted} dynamic list within the case data bin</li>
     *   <li>filters out URLs still referenced in the case data</li>
     *   <li>deletes eligible files via {@code evidenceManagementDeleteService}</li>
     *   <li>applies retry logic to handle transient failures while suppressing final exceptions</li>
     * </ul>
     *
     * @param response the callback response produced by the main handler logic
     * @param finremCaseData the case data containing the bin and deletion list
     * @param userAuthorisation the user authorisation token used for Evidence Management API calls
     * @return the original callback response, unchanged
     */
    @Override
    protected GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> postHandle(
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response,
        FinremCaseData finremCaseData, String userAuthorisation) {

        purgeBinFileUrls(finremCaseData, userAuthorisation);

        return response;
    }

    private void purgeBinFileUrls(FinremCaseData finremCaseData, String userAuthorisation) {
        Set<String> attachedUrls = extractAttachedUrls(finremCaseData);

        ofNullable(finremCaseData.getBin().getFileUrlsToBeDeleted())
            .map(DynamicList::getListItems)
            .stream()
            .flatMap(List::stream)
            .map(DynamicListElement::getCode)
            .filter(url -> isNotAttachedToCase(url, attachedUrls))
            .forEach(url ->
                retryExecutor.runWithRetrySuppressException(
                    () -> evidenceManagementDeleteService.delete(url, userAuthorisation),
                    "EM File Deletion - %s".formatted(url),
                    finremCaseData.getCcdCaseId())
            );
    }

    private Set<String> extractAttachedUrls(FinremCaseData finremCaseData) {
        return finremCaseDetailsMapper.finremCaseDataToMap(finremCaseData)
            .values().stream()
            .filter(Map.class::isInstance)
            .map(value -> (Map<?, ?>) value)
            .map(map -> (String) map.get("document_url")) // Extracts the URL directly
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private boolean isNotAttachedToCase(String url, Set<String> attachedUrls) {
        return !attachedUrls.contains(url);
    }
}
