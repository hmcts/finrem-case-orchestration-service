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

        grantRespondentSolicitor(caseData, caseIdAsString, 3);
        sendCorrespondenceWithRetry(caseDetails, caseIdAsString, userAuthorisation, 3);

        return response(caseData);
    }

    private void grantRespondentSolicitor(FinremCaseData caseData, String caseIdAsString, int attemptsLeft) {
        try {
            assignPartiesAccessService.grantRespondentSolicitor(caseData);
        } catch (Exception e) {
            log.error("{} - Failed granting respondent solicitor. Attempts left: {}",
                caseIdAsString, attemptsLeft - 1, e);

            if (attemptsLeft > 1) {
                grantRespondentSolicitor(caseData, caseIdAsString, attemptsLeft - 1);
            } else {
                log.error("{} - All retry attempts exhausted while granting respondent solicitor",
                    caseIdAsString);
            }
        }
    }

    private void sendCorrespondenceWithRetry(FinremCaseDetails caseDetails, String caseIdAsString,
                                             String userAuthorisation, int attemptsLeft) {
        try {
            issueApplicationConsentCorresponder.sendCorrespondence(caseDetails, userAuthorisation);
        } catch (Exception e) {
            log.error("{} - Failed sending correspondence. Attempts left: {}",
                caseIdAsString, attemptsLeft, e);

            if (attemptsLeft > 1) {
                sendCorrespondenceWithRetry(caseDetails, caseIdAsString, userAuthorisation, attemptsLeft - 1);
            } else {
                log.error("{} - All retry attempts exhausted while sending correspondence",
                    caseIdAsString);
            }
        }
    }
}
