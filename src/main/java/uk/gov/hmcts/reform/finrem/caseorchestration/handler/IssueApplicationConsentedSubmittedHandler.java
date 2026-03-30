package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.IssueApplicationConsentCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

@Slf4j
@Service
public class IssueApplicationConsentedSubmittedHandler extends FinremCallbackHandler {

    private final IssueApplicationConsentCorresponder issueApplicationConsentCorresponder;

    private final AssignPartiesAccessService assignPartiesAccessService;

    private final RetryExecutor retryExecutor;

    @Autowired
    public IssueApplicationConsentedSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                     IssueApplicationConsentCorresponder issueApplicationConsentCorresponder,
                                                     AssignPartiesAccessService assignPartiesAccessService,
                                                     RetryExecutor retryExecutor) {
        super(finremCaseDetailsMapper);
        this.issueApplicationConsentCorresponder = issueApplicationConsentCorresponder;
        this.assignPartiesAccessService = assignPartiesAccessService;
        this.retryExecutor = retryExecutor;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.ISSUE_APPLICATION.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        String assignRespondentSolicitorError = grantRespondentSolicitor(caseData);
        String sendCorrespondenceError = sendCorrespondence(caseDetails, userAuthorisation);
        boolean isHavingErrors = !StringUtils.isAllBlank(assignRespondentSolicitorError, sendCorrespondenceError);

        if (isHavingErrors) {
            return submittedResponse(
                toConfirmationHeader("Application Issued with Errors"),
                toConfirmationBody(assignRespondentSolicitorError, sendCorrespondenceError));
        } else {
            return submittedResponse();
        }
    }

    private String grantRespondentSolicitor(FinremCaseData caseData) {
        ContactDetailsWrapper contactDetailsWrapper = caseData.getContactDetailsWrapper();
        String respSolEmail = YesOrNo.isYes(contactDetailsWrapper.getConsentedRespondentRepresented())
            ? caseData.getRespondentSolicitorEmail() : null;
        if (StringUtils.isBlank(respSolEmail)) {
            return null;
        }
        try {
            retryExecutor.runWithRetry(() -> assignPartiesAccessService
                .grantRespondentSolicitor(caseData),
                "granting respondent solicitor",
                caseData.getCcdCaseId()
            );
            return null;
        } catch (Exception ex) {
            log.error("Error granting respondent solicitor", ex);
            return "There was a problem granting access to respondent solicitor: %s".formatted(respSolEmail);
        }
    }

    private String sendCorrespondence(FinremCaseDetails caseDetails, String userAuthorisation) {
        try {
            retryExecutor.runWithRetry(() -> issueApplicationConsentCorresponder
                .sendCorrespondence(caseDetails, userAuthorisation),
                "sending correspondence",
                caseDetails.getCaseIdAsString()
            );
            return null;
        } catch (Exception ex) {
            log.error("Error sending correspondence", ex);
            return "There was a problem sending correspondence.";
        }
    }
}
