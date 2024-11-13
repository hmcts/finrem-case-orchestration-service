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
import java.util.stream.Stream;

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

    private String buildHearingInfoFromDraftOrdersReview(Stream<DraftOrdersReviewCollection> selected) {
        return selected.findFirst().map(draftOrdersReviewCollection ->
                hearingService.formatHearingInfo(draftOrdersReviewCollection.getValue().getHearingType(),
                    draftOrdersReviewCollection.getValue().getHearingDate(),
                    draftOrdersReviewCollection.getValue().getHearingTime(),
                    draftOrdersReviewCollection.getValue().getHearingJudge()))
            .orElse(null);
    }

    private ReviewableDraftOrder createReviewableDraftOrder(DraftOrdersWrapper draftOrdersWrapper, int index) {
        String hearingInfo = buildHearingInfoFromDraftOrdersReview(draftOrdersWrapper.getSelectedOutstandingDraftOrdersReviewCollection());

        List<ReviewableDraftOrder> collection = draftOrdersWrapper.getSelectedOutstandingDraftOrdersReviewCollection()
            .map(DraftOrdersReviewCollection::getValue)
            .map(DraftOrdersReview::getDraftOrderDocReviewCollection)
            .flatMap(List::stream)
            .map(DraftOrderDocReviewCollection::getValue)
            .filter(a -> OrderStatus.isJudgeReviewable(a.getOrderStatus()))
            .map(a -> ReviewableDraftOrder.builder()
                .hearingInfo(hearingInfo)
                .document(a.getDraftOrderDocument())
                .attachments(a.getAttachments())
                .build())
            .toList();
        if (collection.size() < index) {
            return null;
        }
        return collection.get(index - 1);
    }

    private ReviewablePsa createReviewablePsa(DraftOrdersWrapper draftOrdersWrapper, int index) {
        String hearingInfo = buildHearingInfoFromDraftOrdersReview(draftOrdersWrapper.getSelectedOutstandingDraftOrdersReviewCollection());

        List<ReviewablePsa> collection = draftOrdersWrapper.getSelectedOutstandingDraftOrdersReviewCollection()
            .map(DraftOrdersReviewCollection::getValue)
            .map(DraftOrdersReview::getPsaDocReviewCollection)
            .flatMap(List::stream)
            .map(PsaDocReviewCollection::getValue)
            .map(a -> ReviewablePsa.builder()
                .hearingInfo(hearingInfo)
                .document(a.getPsaDocument())
                .build())
            .toList();
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

        draftOrdersWrapper.setJudgeApproval(JudgeApproval.builder()
            .reviewablePsa1(createReviewablePsa(draftOrdersWrapper,1))
            .reviewablePsa2(createReviewablePsa(draftOrdersWrapper,2))
            .reviewablePsa3(createReviewablePsa(draftOrdersWrapper,3))
            .reviewablePsa4(createReviewablePsa(draftOrdersWrapper,4))
            .reviewablePsa5(createReviewablePsa(draftOrdersWrapper,5))
            .reviewableDraftOrder1(createReviewableDraftOrder(draftOrdersWrapper, 1))
            .reviewableDraftOrder2(createReviewableDraftOrder(draftOrdersWrapper, 2))
            .reviewableDraftOrder3(createReviewableDraftOrder(draftOrdersWrapper, 3))
            .reviewableDraftOrder4(createReviewableDraftOrder(draftOrdersWrapper, 4))
            .reviewableDraftOrder5(createReviewableDraftOrder(draftOrdersWrapper, 5))
            .build());

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(finremCaseData).build();
    }
}
