package uk.gov.hmcts.reform.finrem.caseorchestration.handler.judgeapproval;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DraftOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval.ApproveOrderService;

@Slf4j
@Service
public class ApproveDraftOrdersAboutToSubmitHandler extends FinremCallbackHandler {

    private final ApproveOrderService approveOrderService;

    private final DraftOrderService draftOrderService;

    public ApproveDraftOrdersAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, ApproveOrderService approveOrderService,
                                                  DraftOrderService draftOrderService) {
        super(finremCaseDetailsMapper);
        this.approveOrderService = approveOrderService;
        this.draftOrderService = draftOrderService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType) && CaseType.CONTESTED.equals(caseType) && EventType.APPROVE_ORDERS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested {} about-to-submit event callback for Case ID: {}", callbackRequest.getEventType(), caseId);

        FinremCaseData finremCaseData = caseDetails.getData();
        DraftOrdersWrapper draftOrdersWrapper = finremCaseData.getDraftOrdersWrapper();
        // Clear field in case it contains ids from previously processed refused orders
        // The ids are handled in the submitted callback so can't be removed
        // and clearing them in the about to start callback doesn't work with CCD
        draftOrdersWrapper.setRefusalOrderIdsToBeSent(null);
        approveOrderService.populateJudgeDecisions(caseDetails, draftOrdersWrapper, userAuthorisation);
        clearInputFields(draftOrdersWrapper);
        draftOrderService.clearEmptyOrdersInDraftOrdersReviewCollection(finremCaseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(finremCaseData).build();
    }

    private void clearInputFields(DraftOrdersWrapper draftOrdersWrapper) {
        draftOrdersWrapper.setJudgeApproval1(null);
        draftOrdersWrapper.setJudgeApproval2(null);
        draftOrdersWrapper.setJudgeApproval3(null);
        draftOrdersWrapper.setJudgeApproval4(null);
        draftOrdersWrapper.setJudgeApproval5(null);
        draftOrdersWrapper.setHearingInstruction(null);
        draftOrdersWrapper.setShowWarningMessageToJudge(null);
        draftOrdersWrapper.setExtraReportFieldsInput(null);
    }
}
