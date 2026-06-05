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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.issueapplication.IssueApplicationContestedEmailCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

@Slf4j
@Service
public class IssueApplicationContestedSubmittedHandler extends FinremCallbackHandler {

    private final AssignPartiesAccessService assignPartiesAccessService;
    private final IssueApplicationContestedEmailCorresponder corresponder;
    private final RetryExecutor retryExecutor;

    public IssueApplicationContestedSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                     AssignPartiesAccessService assignPartiesAccessService,
                                                     IssueApplicationContestedEmailCorresponder corresponder,
                                                     RetryExecutor retryExecutor) {
        super(finremCaseDetailsMapper);
        this.assignPartiesAccessService = assignPartiesAccessService;
        this.corresponder = corresponder;
        this.retryExecutor = retryExecutor;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.ISSUE_APPLICATION.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));

        validateCaseData(callbackRequest);

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        String assignRespondentSolicitorError = grantRespondentSolicitor(caseData);
        String sendCorrespondenceError = sendCorrespondence(caseDetails);
        boolean isHavingErrors = !StringUtils.isAllBlank(assignRespondentSolicitorError, sendCorrespondenceError);

        if (isHavingErrors) {
            return submittedResponse(
                toConfirmationHeader("Application Issued with Errors"),
                toConfirmationBody(assignRespondentSolicitorError, sendCorrespondenceError)
            );
        } else {
            return submittedResponse();
        }
    }

    private String grantRespondentSolicitor(FinremCaseData caseData) {
        ContactDetailsWrapper contactDetailsWrapper = caseData.getContactDetailsWrapper();
        String respSolEmail = YesOrNo.isYes(contactDetailsWrapper.getContestedRespondentRepresented())
            ? caseData.getRespondentSolicitorEmail() : null;
        if (StringUtils.isBlank(respSolEmail)) {
            return null;
        }
        try {
            retryExecutor.runWithRetry(() -> assignPartiesAccessService.grantRespondentSolicitor(caseData),
                "granting respondent solicitor", caseData.getCcdCaseId());
            return null;
        } catch (Exception ex) {
            log.error("Error granting respondent solicitor", ex);
            return "There was a problem granting access to respondent solicitor: %s".formatted(respSolEmail);
        }
    }

    private String sendCorrespondence(FinremCaseDetails caseDetails) {
        try {
            retryExecutor.runWithRetry(() -> corresponder.sendCorrespondence(caseDetails),
                "sending correspondence", caseDetails.getCaseIdAsString());

            return null;
        } catch (Exception ex) {
            log.error("Error sending correspondence", ex);
            return "There was a problem sending correspondence.";
        }
    }
}
