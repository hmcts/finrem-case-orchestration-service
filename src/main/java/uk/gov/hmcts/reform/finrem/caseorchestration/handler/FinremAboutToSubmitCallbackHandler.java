package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

public abstract class FinremAboutToSubmitCallbackHandler extends FinremCallbackHandler {

    protected FinremAboutToSubmitCallbackHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
    }

    @Override
    protected final boolean shouldClearTemporaryFieldsAfterHandle() {
        return true;
    }

    @Override
    protected GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> postHandle(
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response,
        FinremCaseData finremCaseData, String userAuthorisation) {
        // Probably run super, then do your thing.
        return response;
    }
}
