package uk.gov.hmcts.reform.finrem.caseorchestration.handler.reassignjudge;

import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.FinremAssignToJudgeCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class ReassignJudgeSubmittedHandler extends FinremSubmittedCallbackHandler {

    private final FinremAssignToJudgeCorresponder assignToJudgeCorresponder;

    public ReassignJudgeSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
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
            && EventType.REASSIGN_JUDGE.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        //
        String error = sendCorrespondence(caseDetails, userAuthorisation);

        if (error != null) {
            return submittedResponse(
                toConfirmationHeader("Reassign Judge event submitted with errors."),
                toConfirmationBody(error));
        } else {
            return submittedResponse();
        }
    }

    /**
     * Sends "reassign judge" correspondence for the given case, retrying on failure
     * according to the configured retry policy.
     *
     * <p>
     * If all retry attempts fail, an error message is captured via the retry handler
     * and returned to the caller; otherwise {@code null} is returned, indicating
     * the correspondence was sent successfully.
     *
     * @param caseDetails       the case details for which correspondence should be sent
     * @param userAuthorisation the authorisation token of the user triggering the action
     * @return an error message describing the failure if sending ultimately failed
     *         after retries, or {@code null} if the correspondence was sent without error
     */
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
