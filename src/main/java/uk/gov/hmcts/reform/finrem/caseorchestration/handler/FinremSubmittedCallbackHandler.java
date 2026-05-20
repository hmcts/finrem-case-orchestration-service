package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

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
        return false;
    }

    @Override
    protected final boolean shouldClearBinBeforeHandle() {
        return false;
    }

    @Override
    protected final boolean shouldHandleBin() {
        return true;
    }

    @Override
    protected void handleBin(FinremCaseData finremCaseData, String userAuthorisation) {
        ofNullable(finremCaseData.getBin().getFileUrlsToBeDeleted())
            .map(DynamicList::getListItems)
            .stream()
            .flatMap(List::stream)
            .map(DynamicListElement::getCode).forEach(url ->
                    retryExecutor.runWithRetrySuppressException(
                        () -> evidenceManagementDeleteService.delete(url, userAuthorisation),
                        "Physical File Deletion", finremCaseData.getCcdCaseId())
                );
    }
}
