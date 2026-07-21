package uk.gov.hmcts.reform.finrem.caseorchestration.handler.giveallocationdirection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ExpressCaseWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

@Slf4j
@Service
public class GiveAllocationDirectionAboutToStartHandler extends FinremCallbackHandler {

    private final ExpressCaseService expressCaseService;

    @Autowired
    public GiveAllocationDirectionAboutToStartHandler(FinremCaseDetailsMapper mapper,
                                                      ExpressCaseService expressCaseService) {
        super(mapper);
        this.expressCaseService = expressCaseService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.GIVE_ALLOCATION_DIRECTIONS_V2.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToStart(callbackRequest));

        FinremCaseData finremCaseData = callbackRequest.getFinremCaseData();
        ExpressCaseWrapper expressCaseWrapper = finremCaseData.getExpressCaseWrapper();

        expressCaseWrapper.setShowShouldAllocateToExpressPilot(YesOrNo.forValue(
            expressCaseService.canJudgeSetExpressPilotStatus(finremCaseData)
        ));

        return response(finremCaseData);
    }
}
