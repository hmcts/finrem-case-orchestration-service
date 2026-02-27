package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
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
        final String caseIdAsString = caseDetails.getCaseIdAsString();

        grantRespondentSolicitor(caseData, caseIdAsString);
        sendCorrespondenceWithRetry(caseDetails, caseIdAsString);

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

    private void sendCorrespondenceWithRetry(FinremCaseDetails caseDetails, String caseIdAsString) {
        executeWithRetrySafely(log,
            () -> corresponder.sendCorrespondence(caseDetails),
            caseIdAsString,
            "sending correspondence",
            3
        );
    }
}
