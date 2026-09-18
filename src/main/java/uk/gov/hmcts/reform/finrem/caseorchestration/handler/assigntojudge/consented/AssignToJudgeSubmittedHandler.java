package uk.gov.hmcts.reform.finrem.caseorchestration.handler.assigntojudge.consented;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremSubmittedCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.AssignToJudgeCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AssignToJudgeSubmittedHandler extends FinremSubmittedCallbackHandler {

    private final List<EventType> assignToJudgeEvents =
        List.of(EventType.REFER_TO_JUDGE,
            EventType.REFER_TO_JUDGE_FROM_ORDER_MADE,
            EventType.REFER_TO_JUDGE_FROM_CONSENT_ORDER_APPROVED,
            EventType.REFER_TO_JUDGE_FROM_CONSENT_ORDER_MADE,
            EventType.REFER_TO_JUDGE_FROM_AWAITING_RESPONSE,
            EventType.REFER_TO_JUDGE_FROM_RESPOND_TO_ORDER,
            EventType.REFER_TO_JUDGE_FROM_CLOSE);

    private final AssignToJudgeCorresponder assignToJudgeCorresponder;

    protected final ApplicationEventPublisher applicationEventPublisher;

    public AssignToJudgeSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                         EvidenceManagementDeleteService evidenceManagementDeleteService,
                                         RetryExecutor retryExecutor,
                                         AssignToJudgeCorresponder assignToJudgeCorresponder,
                                         ApplicationEventPublisher applicationEventPublisher) {
        super(finremCaseDetailsMapper, evidenceManagementDeleteService, retryExecutor);
        this.assignToJudgeCorresponder = assignToJudgeCorresponder;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && assignToJudgeEvents.contains(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        List<String> errors = new ArrayList<>();
        errors.addAll(sendAssignToJudgeCorrespondences(callbackRequest.getEventType(), caseDetails, userAuthorisation));

        boolean isHavingErrors = !StringUtils.isAllBlank(errors.toArray(new String[0]));

        if (isHavingErrors) {
            return submittedResponse(
                toConfirmationHeader("Assign to judge event submitted with errors."),
                toConfirmationBody(errors.toArray(new String[0])));
        } else {
            return submittedResponse();
        }
    }

    private List<String> sendAssignToJudgeCorrespondences(EventType eventType, FinremCaseDetails caseDetails,
                                                          String userAuthorisation) {
        List<SendCorrespondenceEvent> events = assignToJudgeCorresponder
            .buildSendCorrespondenceEvents(eventType, caseDetails, userAuthorisation);

        List<String> errors = new ArrayList<>();
        for (SendCorrespondenceEvent event : events) {
            retryExecutor.runWithRetryWithHandler(() -> applicationEventPublisher.publishEvent(event),
                "sending assign to judge correspondence %s (%s)".formatted(
                    event.getNotificationTrackerId(),
                    event.describeNotificationParties()
                ),
                caseDetails.getCaseIdAsString(),
                (exception, actionName, caseId1) ->
                    errors.add("There was a problem sending assign to judge correspondence (%s). Please send it manually."
                        .formatted(event.describeNotificationParties())));
        }
        return errors;
    }
}
