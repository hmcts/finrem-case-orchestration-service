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
        assignPartiesAccessService.grantRespondentSolicitor(caseData);
        corresponder.sendCorrespondence(caseDetails);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).build();
    }
}
