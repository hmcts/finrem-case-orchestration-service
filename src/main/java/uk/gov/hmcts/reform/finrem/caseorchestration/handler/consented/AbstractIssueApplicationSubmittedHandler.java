package uk.gov.hmcts.reform.finrem.caseorchestration.handler.consented;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.AssignToJudgeCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractIssueApplicationSubmittedHandler extends FinremSubmittedCallbackHandler {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final AssignToJudgeCorresponder assignToJudgeCorresponder;

    protected final AssignPartiesAccessService assignPartiesAccessService;

    protected final ApplicationEventPublisher applicationEventPublisher;

    protected AbstractIssueApplicationSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                       EvidenceManagementDeleteService evidenceManagementDeleteService,
                                                       RetryExecutor retryExecutor,
                                                       AssignToJudgeCorresponder assignToJudgeCorresponder,
                                                       AssignPartiesAccessService assignPartiesAccessService,
                                                       ApplicationEventPublisher applicationEventPublisher) {
        super(finremCaseDetailsMapper, evidenceManagementDeleteService, retryExecutor);
        this.assignToJudgeCorresponder = assignToJudgeCorresponder;
        this.assignPartiesAccessService = assignPartiesAccessService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    protected abstract EventType supportedEventType();

    protected String getConfirmationHeader() {
        return "Application Issued with Errors";
    }

    /**
     * A unit of post-issue work that may fail. Implementations should return {@code null}
     * (or a blank string) on success, and a human-readable error message on failure.
     */
    @FunctionalInterface
    protected interface SubmittedTask {
        String execute(FinremCaseDetails caseDetails, String userAuthorisation);
    }

    /**
     * Hook for subclasses to contribute additional tasks to run after the standard
     * "grant respondent solicitor" and "send correspondence" tasks. Each task's returned
     * error (if any) is folded into the overall error check and confirmation body.
     *
     * @return list of additional tasks to execute; empty by default
     */
    protected List<SubmittedTask> additionalTasks() {
        return List.of();
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && supportedEventType().equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        List<String> errors = new ArrayList<>();
        errors.add(grantRespondentSolicitor(caseData));
        errors.addAll(sendIssueApplicationCorrespondences(caseDetails, userAuthorisation));
        additionalTasks().forEach(task -> errors.add(task.execute(caseDetails, userAuthorisation)));

        boolean isHavingErrors = !StringUtils.isAllBlank(errors.toArray(new String[0]));

        if (isHavingErrors) {
            return submittedResponse(
                toConfirmationHeader(getConfirmationHeader()),
                toConfirmationBody(errors.toArray(new String[0])));
        } else {
            return submittedResponse();
        }
    }

    private List<String> sendIssueApplicationCorrespondences(FinremCaseDetails caseDetails, String userAuthorisation) {
        List<SendCorrespondenceEvent> events = assignToJudgeCorresponder
            .buildSendCorrespondenceEvents(caseDetails, userAuthorisation);

        List<String> errors = new ArrayList<>();
        for (SendCorrespondenceEvent event : events) {
            retryExecutor.runWithRetryWithHandler(() -> applicationEventPublisher.publishEvent(event),
                "sending issue application correspondence %s (%s)".formatted(
                    event.getNotificationTrackerId(),
                    event.describeNotificationParties()
                ),
                caseDetails.getCaseIdAsString(),
                (exception, actionName, caseId1) ->
                    errors.add("There was a problem sending issue application correspondence. Please send it manually."));
        }
        return errors;
    }

    private String grantRespondentSolicitor(FinremCaseData caseData) {
        ContactDetailsWrapper contactDetailsWrapper = caseData.getContactDetailsWrapper();
        String respSolEmail = YesOrNo.isYes(contactDetailsWrapper.getConsentedRespondentRepresented())
            ? caseData.getRespondentSolicitorEmail() : null;
        if (StringUtils.isBlank(respSolEmail)) {
            return null;
        }

        AtomicReference<String> error = new AtomicReference<>();
        retryExecutor.runWithRetryWithHandler(() -> assignPartiesAccessService.grantRespondentSolicitor(caseData),
            "granting respondent solicitor",
            caseData.getCcdCaseId(),
            (exception, actionName, caseId1) ->
                error.set("There was a problem granting access to respondent solicitor: %s".formatted(respSolEmail)));
        return error.get();
    }
}
