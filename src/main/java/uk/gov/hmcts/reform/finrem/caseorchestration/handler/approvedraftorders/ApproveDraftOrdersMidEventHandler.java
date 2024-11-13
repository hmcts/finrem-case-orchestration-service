package uk.gov.hmcts.reform.finrem.caseorchestration.handler.approvedraftorders;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.ReviewableDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.ReviewablePsa;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingService;

import java.util.List;

@Slf4j
@Service
public class ApproveDraftOrdersMidEventHandler extends FinremCallbackHandler {

    private final HearingService hearingService;

    public ApproveDraftOrdersMidEventHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, HearingService hearingService) {
        super(finremCaseDetailsMapper);
        this.hearingService = hearingService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return (CallbackType.MID_EVENT.equals(callbackType) || CallbackType.ABOUT_TO_START.equals(callbackType))
            && CaseType.CONTESTED.equals(caseType)
            && EventType.APPROVE_ORDERS.equals(eventType);
    }

    private String buildHearingInfoFromDraftOrdersReview(DraftOrdersReview draftOrdersReview) {
        return hearingService.formatHearingInfo(draftOrdersReview.getHearingType(),
            draftOrdersReview.getHearingDate(), draftOrdersReview.getHearingTime(), draftOrdersReview.getHearingJudge());
    }

    private List<ReviewableDraftOrder> getReviewableDraftOrders(List<DraftOrdersReviewCollection> outstanding) {
        return outstanding.stream()
            .map(DraftOrdersReviewCollection::getValue)
            .flatMap(draftOrdersReview -> {
                String hearingInfo = buildHearingInfoFromDraftOrdersReview(draftOrdersReview);
                return draftOrdersReview.getDraftOrderDocReviewCollection().stream()
                    .map(DraftOrderDocReviewCollection::getValue)
                    .filter(a -> OrderStatus.isJudgeReviewable(a.getOrderStatus()))
                    .map(a -> ReviewableDraftOrder.builder()
                        .hearingInfo(hearingInfo) // Set specific hearingInfo for each item
                        .document(a.getDraftOrderDocument())
                        .attachments(a.getAttachments())
                        .build());
            }) // Flatten the stream of streams
            .toList();
    }

    private ReviewableDraftOrder createReviewableDraftOrder(List<DraftOrdersReviewCollection> outstanding, int index) {
        // Build a collection of reviewable draft orders with specific hearingInfo for each item
        List<ReviewableDraftOrder> collection = getReviewableDraftOrders(outstanding);
        // Return the specified item if it exists in the collection, otherwise return null
        if (collection.size() < index) {
            return null;
        }
        return collection.get(index - 1);
    }

    private List<ReviewablePsa> getReviewablePsas(List<DraftOrdersReviewCollection> outstanding) {
        return outstanding.stream()
            .map(DraftOrdersReviewCollection::getValue)
            .flatMap(draftOrdersReview -> {
                String hearingInfo = buildHearingInfoFromDraftOrdersReview(draftOrdersReview);
                return draftOrdersReview.getPsaDocReviewCollection().stream()
                    .map(PsaDocReviewCollection::getValue)
                    .filter(a -> OrderStatus.isJudgeReviewable(a.getOrderStatus()))
                    .map(a -> ReviewablePsa.builder()
                        .hearingInfo(hearingInfo)
                        .document(a.getPsaDocument())
                        .build());
            }) // Flatten the stream of streams
            .toList();
    }

    private ReviewablePsa createReviewablePsa(List<DraftOrdersReviewCollection> outstanding, int index) {
        // Build a collection of reviewable draft orders with specific hearingInfo for each item
        List<ReviewablePsa> collection = getReviewablePsas(outstanding);
        // Return the specified item if it exists in the collection, otherwise return null
        if (collection.size() < index) {
            return null;
        }
        return collection.get(index - 1);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested {} mid event callback for Case ID: {}", callbackRequest.getEventType(), caseId);

        FinremCaseData finremCaseData = caseDetails.getData();
        DraftOrdersWrapper draftOrdersWrapper = finremCaseData.getDraftOrdersWrapper();
        List<DraftOrdersReviewCollection> outstanding = draftOrdersWrapper.getOutstandingDraftOrdersReviewCollection();

        draftOrdersWrapper.setJudgeApproval(JudgeApproval.builder()
            .reviewablePsa1(createReviewablePsa(outstanding,1))
            .reviewablePsa2(createReviewablePsa(outstanding,2))
            .reviewablePsa3(createReviewablePsa(outstanding,3))
            .reviewablePsa4(createReviewablePsa(outstanding,4))
            .reviewablePsa5(createReviewablePsa(outstanding,5))
            .reviewableDraftOrder1(createReviewableDraftOrder(outstanding, 1))
            .reviewableDraftOrder2(createReviewableDraftOrder(outstanding, 2))
            .reviewableDraftOrder3(createReviewableDraftOrder(outstanding, 3))
            .reviewableDraftOrder4(createReviewableDraftOrder(outstanding, 4))
            .reviewableDraftOrder5(createReviewableDraftOrder(outstanding, 5))
            .warningMessageToJudge(getReviewableDraftOrders(outstanding).size() > 5 || getReviewablePsas(outstanding).size() > 5
                ? ("This page is limited to showing only 5 draft orders/pension sharing annexes requiring review. "
                + "There are additional draft orders/pension sharing annexes requiring review that are not shown.") : null)
            .build());

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(finremCaseData).build();
    }
}
