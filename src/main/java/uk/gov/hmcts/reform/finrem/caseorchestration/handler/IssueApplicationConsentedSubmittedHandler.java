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

        String assignRespondentSolicitorError = grantRespondentSolicitor(caseData);
        String sendCorrespondenceError = sendCorrespondence(caseDetails, userAuthorisation);
        boolean isHavingErrors = !StringUtils.isAllBlank(assignRespondentSolicitorError, sendCorrespondenceError);

        if (isHavingErrors) {
            return submittedResponse("# Application Issued with Errors",
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
            executeWithRetry(log,
                () -> assignPartiesAccessService.grantRespondentSolicitor(caseData),
                caseData.getCcdCaseId(),
                "granting respondent solicitor"
            );
            return null;
        } catch (Exception ex) {
            return "There was a problem granting access to respondent solicitor %s".formatted(respSolEmail);
        }
    }

    private String sendCorrespondence(FinremCaseDetails caseDetails, String userAuthorisation) {
        try {
            executeWithRetry(log,
                () -> issueApplicationConsentCorresponder.sendCorrespondence(caseDetails, userAuthorisation),
                caseDetails.getCaseIdAsString(),
                "sending correspondence"
            );
            return null;
        } catch (Exception ex) {
            return "There was a problem sending correspondence.";
        }
    }
}
