package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.FinremAssignToJudgeCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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

    private final FinremAssignToJudgeCorresponder assignToJudgeCorresponder;

    public AssignToJudgeSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                         EvidenceManagementDeleteService evidenceManagementDeleteService,
                                         RetryExecutor retryExecutor,
                                         FinremAssignToJudgeCorresponder assignToJudgeCorresponder) {
        super(finremCaseDetailsMapper, evidenceManagementDeleteService, retryExecutor);
        this.assignToJudgeCorresponder = assignToJudgeCorresponder;
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
        errors.add(sendCorrespondence(caseDetails, userAuthorisation));

        boolean isHavingErrors = !StringUtils.isAllBlank(errors.toArray(new String[0]));

        if (isHavingErrors) {
            return submittedResponse(
                toConfirmationHeader("Assign to Judge event submitted with errors."),
                toConfirmationBody(errors.toArray(new String[0])));
        } else {
            return submittedResponse();
        }
    }

    private String sendCorrespondence(FinremCaseDetails caseDetails, String userAuthorisation) {
        AtomicReference<String> error = new AtomicReference<>();
        retryExecutor.runWithRetryWithHandler(() -> assignToJudgeCorresponder.sendCorrespondence(caseDetails, userAuthorisation),
            "sending assign to judge correspondence",
            caseDetails.getCaseIdAsString(),
            (exception, actionName, caseId1) ->
                error.set("There was a problem sending assign to judge : %s"));
        return error.get();
    }
}
