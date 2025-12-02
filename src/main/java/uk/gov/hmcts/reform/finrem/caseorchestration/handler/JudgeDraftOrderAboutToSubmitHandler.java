package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper.DocumentWarningsHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ContestedOrderApprovedLetterService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingOrderService;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Slf4j
@Service
public class JudgeDraftOrderAboutToSubmitHandler extends FinremCallbackHandler {

    private final HearingOrderService hearingOrderService;
    private final ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;
    private final DocumentWarningsHelper documentWarningsHelper;

    public JudgeDraftOrderAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, HearingOrderService hearingOrderService,
                                               ContestedOrderApprovedLetterService contestedOrderApprovedLetterService,
                                               DocumentWarningsHelper documentWarningsHelper) {
        super(finremCaseDetailsMapper);
        this.hearingOrderService = hearingOrderService;
        this.contestedOrderApprovedLetterService = contestedOrderApprovedLetterService;
        this.documentWarningsHelper = documentWarningsHelper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.JUDGE_DRAFT_ORDER.equals(eventType); // Event: Upload approved order
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();

        validateCaseData(callbackRequest);
        hearingOrderService.stampAndStoreJudgeApprovedOrders(finremCaseDetails, userAuthorisation);
        contestedOrderApprovedLetterService.generateAndStoreContestedOrderApprovedLetter(finremCaseDetails, userAuthorisation);
        
        FinremCaseData finremCaseData = finremCaseDetails.getData();
        clearJudgeApprovedOrderCollection(finremCaseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData)
            .warnings(documentWarningsHelper.getDocumentWarnings(callbackRequest, data ->
                emptyIfNull(data.getDraftDirectionWrapper().getDraftDirectionOrderCollection()).stream()
                    .map(DraftDirectionOrderCollection::getValue).toList(), userAuthorisation))
            .build();
    }

    private void clearJudgeApprovedOrderCollection(FinremCaseData finremCaseData) {
        finremCaseData.getDraftDirectionWrapper().setJudgeApprovedOrderCollection(null);
    }
}
