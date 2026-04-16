package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managehearings;

import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing.ManageHearingsCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

@Slf4j
@Service
public class ManageHearingsSubmittedHandler extends FinremCallbackHandler {

    private final RetryExecutor retryExecutor;

    private final ManageHearingsCorresponder manageHearingsCorresponder;

    public ManageHearingsSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                          ManageHearingsCorresponder manageHearingsCorresponder,
                                          RetryExecutor retryExecutor) {
        super(finremCaseDetailsMapper);
        this.manageHearingsCorresponder = manageHearingsCorresponder;
        this.retryExecutor = retryExecutor;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.MANAGE_HEARINGS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));
        
        FinremCaseData finremCaseData = callbackRequest.getCaseDetails().getData();

        ManageHearingsWrapper manageHearingsWrapper = finremCaseData.getManageHearingsWrapper();

        ManageHearingsAction actionSelection = manageHearingsWrapper.getManageHearingsActionSelection();

        if (ManageHearingsAction.ADD_HEARING.equals(actionSelection)) {
            log.info("Beginning hearing correspondence for {} action. Case reference: {}",
                ManageHearingsAction.ADD_HEARING.getDescription(),
                callbackRequest.getCaseDetails().getCaseIdAsString());
            manageHearingsCorresponder.sendHearingCorrespondence(callbackRequest, userAuthorisation);
        }

        if (ManageHearingsAction.ADJOURN_OR_VACATE_HEARING.equals(actionSelection)) {
            log.info("Beginning hearing correspondence for {} action. Case reference: {}",
                ManageHearingsAction.ADJOURN_OR_VACATE_HEARING.getDescription(),
                callbackRequest.getCaseDetails().getCaseIdAsString());
            manageHearingsCorresponder.sendAdjournedOrVacatedHearingCorrespondence(callbackRequest, userAuthorisation);
        }

        return response(finremCaseData);
    }
}
