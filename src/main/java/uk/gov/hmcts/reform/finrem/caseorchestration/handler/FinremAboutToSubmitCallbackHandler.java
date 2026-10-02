package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;

public abstract class FinremAboutToSubmitCallbackHandler extends FinremCallbackHandler {

    protected FinremAboutToSubmitCallbackHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
    }

    @Override
    protected final boolean shouldClearTemporaryFieldsAfterHandle() {
        return true;
    }
}
