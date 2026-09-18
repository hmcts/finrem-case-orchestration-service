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
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationAuditService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.AssignToJudgeCorresponder;

import java.util.List;

import static java.util.Objects.nonNull;

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

    private final NotificationAuditService notificationAuditService;

    public AssignToJudgeAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                             AssignToJudgeCorresponder assignToJudgeCorresponder,
                                             NotificationAuditService notificationAuditService) {
        super(finremCaseDetailsMapper);
        this.assignToJudgeCorresponder = assignToJudgeCorresponder;
        this.notificationAuditService = notificationAuditService;
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

        createAuditsForCorrespondence(callbackRequest, userAuthorisation);

        return response(caseDetails.getData());
    }

    private void createAuditsForCorrespondence(FinremCallbackRequest callbackRequest,
                                               String userAuthorisation) {
        List<SendCorrespondenceEvent> events = assignToJudgeCorresponder
            .buildSendCorrespondenceEvents(callbackRequest.getCaseDetails(), userAuthorisation);
        String trackerId = null;
        for (SendCorrespondenceEvent event : events) {
            if (nonNull(trackerId)) {
                event.setNotificationTrackerId(trackerId);
            }
            trackerId = notificationAuditService.createAuditsForCorrespondence(event, callbackRequest.getEventType());
        }
    }
}
