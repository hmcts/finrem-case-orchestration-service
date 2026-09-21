package uk.gov.hmcts.reform.finrem.caseorchestration.handler.assigntojudge.consented;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.consented.AssignToJudgeCorresponder;

import java.util.List;

@Slf4j
@Service
public class AssignToJudgeAboutToSubmitHandler extends FinremAboutToSubmitCallbackHandler {

    private final List<EventType> assignToJudgeEvents =
        List.of(EventType.REFER_TO_JUDGE,
            EventType.REFER_TO_JUDGE_FROM_ORDER_MADE,
            EventType.REFER_TO_JUDGE_FROM_CONSENT_ORDER_APPROVED,
            EventType.REFER_TO_JUDGE_FROM_CONSENT_ORDER_MADE,
            EventType.REFER_TO_JUDGE_FROM_AWAITING_RESPONSE,
            EventType.REFER_TO_JUDGE_FROM_RESPOND_TO_ORDER,
            EventType.REFER_TO_JUDGE_FROM_CLOSE);

    private final AssignToJudgeCorresponder assignToJudgeCorresponder;

    public AssignToJudgeAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                             AssignToJudgeCorresponder assignToJudgeCorresponder) {
        super(finremCaseDetailsMapper);
        this.assignToJudgeCorresponder = assignToJudgeCorresponder;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && assignToJudgeEvents.contains(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        assignToJudgeCorresponder.createAuditsForCorrespondence(callbackRequest.getEventType(),
            caseDetails, userAuthorisation);

        return response(caseDetails.getData());
    }
}
