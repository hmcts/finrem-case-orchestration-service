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

@Slf4j
@Service
public class IssueApplicationContestedSubmittedHandler extends FinremCallbackHandler {

    private final IssueApplicationContestedEmailCorresponder corresponder;

    private final AssignPartiesAccessService assignPartiesAccessService;

    public IssueApplicationContestedSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                     IssueApplicationContestedEmailCorresponder corresponder,
                                                     AssignPartiesAccessService assignPartiesAccessService) {
        super(finremCaseDetailsMapper);
        this.corresponder = corresponder;
        this.assignPartiesAccessService = assignPartiesAccessService;
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
            return submittedResponse("# Application Issued with Errors",
                toConfirmationBody(assignRespondentSolicitorError, sendCorrespondenceError));
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
            executeWithRetry(log,
                () -> assignPartiesAccessService.grantRespondentSolicitor(caseData),
                caseData.getCcdCaseId(),
                "granting respondent solicitor",
                3
            );
            return null;
        } catch (Exception ex) {
            return "There was a problem granting access to respondent solicitor %s".formatted(respSolEmail);
        }
    }

    private String sendCorrespondence(FinremCaseDetails caseDetails) {
        try {
            executeWithRetry(log,
                () -> corresponder.sendCorrespondence(caseDetails),
                caseDetails.getCaseIdAsString(),
                "sending correspondence",
                3
            );
            return null;
        } catch (Exception ex) {
            return "There was a problem sending correspondence.";
        }
    }
}
