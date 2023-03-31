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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudgeconsentincontested.FinremAssignToJudgeConsentInContestedCorresponder;

@Slf4j
@Service
public class AssignToJudgeConsentInContestedSubmittedHandler extends FinremCallbackHandler {

    private final FinremAssignToJudgeConsentInContestedCorresponder corresponder;

    @Autowired
    public AssignToJudgeConsentInContestedSubmittedHandler(FinremCaseDetailsMapper mapper,
                                                           FinremAssignToJudgeConsentInContestedCorresponder corresponder) {
        super(mapper);
        this.corresponder = corresponder;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.ASSIGN_TO_JUDGE_CONSENT.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request to notify applicant and respondent for Judge successfully assigned to case for Case ID: {}",
            caseDetails.getId());

        validateCaseData(callbackRequest);

        corresponder.sendCorrespondence(caseDetails, userAuthorisation);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseDetails.getData()).build();
    }


}
