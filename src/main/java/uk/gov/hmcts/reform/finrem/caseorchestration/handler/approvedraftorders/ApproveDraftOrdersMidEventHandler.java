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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.ReviewableDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.ReviewablePsa;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.ReviewablePsaCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;

import java.util.List;

@Slf4j
@Service
public class ApproveDraftOrdersMidEventHandler extends FinremCallbackHandler {

    public ApproveDraftOrdersMidEventHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.APPROVE_ORDERS.equals(eventType);
    }

    private List<ReviewableDraftOrderCollection> createReviewableDraftOrderCollection(DraftOrdersWrapper draftOrdersWrapper) {
        return draftOrdersWrapper.getDraftOrdersReviewCollection().stream()
            .map(DraftOrdersReviewCollection::getValue)
            .map(DraftOrdersReview::getDraftOrderDocReviewCollection)
            .flatMap(List::stream)
            .map(DraftOrderDocReviewCollection::getValue)
            .map(a -> ReviewableDraftOrderCollection.builder()
                .value(ReviewableDraftOrder.builder()
                    .document(a.getDraftOrderDocument())
                    .attachments(a.getAttachments())
                    .build())
                .build())
            .toList();
    }

    private List<ReviewablePsaCollection> createReviewablePsaCollection(DraftOrdersWrapper draftOrdersWrapper) {
        return draftOrdersWrapper.getDraftOrdersReviewCollection().stream()
            .map(DraftOrdersReviewCollection::getValue)
            .map(DraftOrdersReview::getPsaDocReviewCollection)
            .flatMap(List::stream)
            .map(PsaDocReviewCollection::getValue)
            .map(a -> ReviewablePsaCollection.builder()
                .value(ReviewablePsa.builder()
                    .document(a.getPsaDocument())
                    .build())
                .build())
            .toList();
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested {} mid event callback for Case ID: {}", callbackRequest.getEventType(), caseId);

        FinremCaseData finremCaseData = caseDetails.getData();
        DraftOrdersWrapper draftOrdersWrapper = finremCaseData.getDraftOrdersWrapper();

        // TODO depending on the selected hearing
        List<ReviewableDraftOrderCollection> reviewableDraftOrderCollection = createReviewableDraftOrderCollection(draftOrdersWrapper);
        List<ReviewablePsaCollection> reviewablePsaCollection = createReviewablePsaCollection(draftOrdersWrapper);

        draftOrdersWrapper.setJudgeApproval(JudgeApproval.builder()
            .reviewablePsaCollection(reviewablePsaCollection)
            .reviewableDraftOrderCollection(reviewableDraftOrderCollection)
            .build());

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(finremCaseData).build();
    }
}
