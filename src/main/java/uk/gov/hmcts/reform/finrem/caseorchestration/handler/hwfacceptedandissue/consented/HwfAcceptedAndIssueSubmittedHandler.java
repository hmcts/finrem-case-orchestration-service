package uk.gov.hmcts.reform.finrem.caseorchestration.handler.hwfacceptedandissue.consented;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.IssueApplicationConsentCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hwf.HwfCorrespondenceService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class HwfAcceptedAndIssueSubmittedHandler extends FinremCallbackHandler {

    private static final String CONFIRMATION_HEADER_WITH_ERROR = "HWF accepted and issued with errors";

    private final HwfCorrespondenceService hwfNotificationsService;

    private final AssignPartiesAccessService assignPartiesAccessService;

    private final IssueApplicationConsentCorresponder issueApplicationConsentCorresponder;

    private final RetryExecutor retryExecutor;

    public HwfAcceptedAndIssueSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                               HwfCorrespondenceService hwfNotificationsService,
                                               AssignPartiesAccessService assignPartiesAccessService,
                                               IssueApplicationConsentCorresponder issueApplicationConsentCorresponder,
                                               RetryExecutor retryExecutor) {

        super(finremCaseDetailsMapper);
        this.hwfNotificationsService = hwfNotificationsService;
        this.retryExecutor = retryExecutor;
        this.assignPartiesAccessService = assignPartiesAccessService;
        this.issueApplicationConsentCorresponder = issueApplicationConsentCorresponder;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.HWF_ACCEPTED_AND_ISSUE.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();

        final String hwfCorrespondenceError = sendHwfCorrespondence(finremCaseDetails, userAuthorisation);
        final String issueApplicationCorrespondenceError = sendIssueApplicationCorrespondence(finremCaseDetails, userAuthorisation);
        final String grantRespondentSolicitorError = grantRespondentSolicitor(finremCaseDetails.getData());

        boolean isHavingErrors = !StringUtils.isAllBlank(hwfCorrespondenceError, issueApplicationCorrespondenceError,
            grantRespondentSolicitorError);
        if (isHavingErrors) {
            return submittedResponse(
                toConfirmationHeader(CONFIRMATION_HEADER_WITH_ERROR),
                toConfirmationBody(hwfCorrespondenceError, issueApplicationCorrespondenceError, grantRespondentSolicitorError));
        }
        return submittedResponse();
    }

    private String sendHwfCorrespondence(FinremCaseDetails finremCaseDetails, String userAuthorisation) {
        AtomicReference<String> error = new AtomicReference<>();
        retryExecutor.runWithRetryWithHandler(() -> hwfNotificationsService.sendCorrespondence(finremCaseDetails, userAuthorisation),
            "sending HWF correspondence", finremCaseDetails.getCaseIdAsString(),
            (exception, actionName, caseId1) ->
                error.set("There was a problem sending HWF correspondence. Please send it manually."));
        return error.get();
    }

    private String sendIssueApplicationCorrespondence(FinremCaseDetails caseDetails, String userAuthorisation) {
        AtomicReference<String> error = new AtomicReference<>();
        retryExecutor.runWithRetryWithHandler(() -> issueApplicationConsentCorresponder
                .sendCorrespondence(caseDetails, userAuthorisation),
            "sending issue application correspondence",
            caseDetails.getCaseIdAsString(),
            (exception, actionName, caseId1) ->
                error.set("There was a problem sending issue application correspondence. Please send it manually."));
        return error.get();
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
