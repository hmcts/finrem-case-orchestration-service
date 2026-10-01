package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.globalsearch.GlobalSearchService;


public abstract class FinremAboutToSubmitCallbackHandler extends FinremCallbackHandler {

    @Autowired
    protected  GlobalSearchService globalSearchService;

    protected FinremAboutToSubmitCallbackHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
    }

    @Override
    protected GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> postHandle(
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response,
        FinremCaseData finremCaseData, String userAuthorisation) {

        response = super.postHandle(response, finremCaseData, userAuthorisation);
        globalSearchService.setGlobalSearchDataByMap(finremCaseData);

        return response;
    }

    @Override
    protected final boolean shouldClearTemporaryFieldsAfterHandle() {
        return true;
    }
}
