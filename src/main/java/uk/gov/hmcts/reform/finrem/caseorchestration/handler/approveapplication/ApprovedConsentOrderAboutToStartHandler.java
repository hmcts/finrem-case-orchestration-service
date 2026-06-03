package uk.gov.hmcts.reform.finrem.caseorchestration.handler.approveapplication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

@Slf4j
@Service
public class ApprovedConsentOrderAboutToStartHandler extends FinremCallbackHandler {

    private final OnStartDefaultValueService onStartDefaultValueService;

    public ApprovedConsentOrderAboutToStartHandler(FinremCaseDetailsMapper mapper,
                                                   OnStartDefaultValueService onStartDefaultValueService) {
        super(mapper);
        this.onStartDefaultValueService = onStartDefaultValueService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.APPROVE_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToStart(callbackRequest));

        FinremCaseData finremCaseData = callbackRequest.getFinremCaseData();

        onStartDefaultValueService.defaultConsentedOrderJudgeName(finremCaseData, userAuthorisation);
        onStartDefaultValueService.defaultConsentedOrderDate(finremCaseData);

        return response(finremCaseData);
    }
}
