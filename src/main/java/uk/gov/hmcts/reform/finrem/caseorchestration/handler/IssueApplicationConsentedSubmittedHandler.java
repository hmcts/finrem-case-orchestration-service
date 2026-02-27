package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.IssueApplicationConsentCorresponder;

@Slf4j
@Service
public class IssueApplicationConsentedSubmittedHandler extends FinremCallbackHandler {

    private final IssueApplicationConsentCorresponder issueApplicationConsentCorresponder;

    private final AssignPartiesAccessService assignPartiesAccessService;

    @Autowired
    public IssueApplicationConsentedSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                     IssueApplicationConsentCorresponder issueApplicationConsentCorresponder,
                                                     AssignPartiesAccessService assignPartiesAccessService) {
        super(finremCaseDetailsMapper);
        this.issueApplicationConsentCorresponder = issueApplicationConsentCorresponder;
        this.assignPartiesAccessService = assignPartiesAccessService;
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
        final String caseIdAsString = caseDetails.getCaseIdAsString();

        grantRespondentSolicitor(caseData, caseIdAsString);
        sendCorrespondenceWithRetry(caseDetails, caseIdAsString, userAuthorisation);

        return response(caseData);
    }

    private void grantRespondentSolicitor(FinremCaseData caseData, String caseIdAsString) {
        executeWithRetrySafely(log,
            () -> assignPartiesAccessService.grantRespondentSolicitor(caseData),
            caseIdAsString,
            "granting respondent solicitor",
            3
        );
    }

    private void sendCorrespondenceWithRetry(FinremCaseDetails caseDetails, String caseIdAsString, String userAuthorisation) {
        executeWithRetrySafely(log,
            () -> issueApplicationConsentCorresponder.sendCorrespondence(caseDetails, userAuthorisation),
            caseIdAsString,
            "sending correspondence",
            3
        );
    }
}
