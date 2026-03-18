package uk.gov.hmcts.reform.finrem.caseorchestration.handler.closecase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremAboutToSubmitCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.casemetrics.CaseMetrics;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CaseDataMetricsWrapper;

@Slf4j
@Service
public class CloseCaseAboutToSubmitHandler extends FinremAboutToSubmitCallbackHandler {

    public CloseCaseAboutToSubmitHandler(FinremCaseDetailsMapper mapper) {
        super(mapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && (CaseType.CONSENTED.equals(caseType) || CaseType.CONTESTED.equals(caseType))
            && EventType.CLOSE.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest, String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        CaseDataMetricsWrapper caseDataMetricsWrapper = caseData.getCaseDataMetricsWrapper();

        if (caseDataMetricsWrapper.getCaseMetrics() == null) {
            caseDataMetricsWrapper.setCaseMetrics(new CaseMetrics());
        }
        caseDataMetricsWrapper.getCaseMetrics().setCaseClosureDate(caseDataMetricsWrapper.getCaseClosureDateField());

        return response(caseData);
    }
}
