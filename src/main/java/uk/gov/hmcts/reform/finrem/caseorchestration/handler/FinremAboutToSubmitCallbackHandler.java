package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;

public abstract class FinremAboutToSubmitCallbackHandler extends FinremCallbackHandler {

    public FinremAboutToSubmitCallbackHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
    }

    @Override
    protected final boolean shouldClearTemporaryFields() {
        return true;
    }
}
