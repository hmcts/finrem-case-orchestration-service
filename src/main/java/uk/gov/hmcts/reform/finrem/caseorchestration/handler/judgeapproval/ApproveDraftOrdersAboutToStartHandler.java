package uk.gov.hmcts.reform.finrem.caseorchestration.handler.judgeapproval;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.SortKey;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.isJudgeReviewable;

@Slf4j
@Service
public class ApproveDraftOrdersAboutToStartHandler extends FinremCallbackHandler {

    private static final String NO_ORDERS_TO_BE_REVIEWED_MESSAGE = "There are no draft orders or pension sharing annexes to review.";

    private final HearingService hearingService;

    public ApproveDraftOrdersAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, HearingService hearingService) {
        super(finremCaseDetailsMapper);
        this.hearingService = hearingService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.APPROVE_ORDERS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested {} about to start event callback for Case ID: {}", callbackRequest.getEventType(), caseId);

        FinremCaseData finremCaseData = caseDetails.getData();
        DraftOrdersWrapper draftOrdersWrapper = finremCaseData.getDraftOrdersWrapper();

        List<String> errors = validateDraftOrdersWrapper(draftOrdersWrapper);
        if (errors.isEmpty()) {
            List<DraftOrdersReviewCollection> outstanding = draftOrdersWrapper.getOutstandingDraftOrdersReviewCollection();

            int totalOutstandingReviews = outstanding.stream()
                .mapToInt(outstandingItem ->
                    outstandingItem.getValue().getDraftOrderDocReviewCollection().stream()
                        .filter(draftOrderDoc -> isJudgeReviewable(draftOrderDoc.getValue().getOrderStatus()))
                        .toList().size() +
                        outstandingItem.getValue().getPsaDocReviewCollection().stream()
                            .filter(psa -> isJudgeReviewable(psa.getValue().getOrderStatus()))
                            .toList().size()
                )
                .sum();

            draftOrdersWrapper.setShowWarningMessageToJudge(YesOrNo.forValue(totalOutstandingReviews > 5));
            draftOrdersWrapper.setJudgeApproval1(createReviewableItems(outstanding, 1));
            draftOrdersWrapper.setJudgeApproval2(createReviewableItems(outstanding, 2));
            draftOrdersWrapper.setJudgeApproval3(createReviewableItems(outstanding, 3));
            draftOrdersWrapper.setJudgeApproval4(createReviewableItems(outstanding, 4));
            draftOrdersWrapper.setJudgeApproval5(createReviewableItems(outstanding, 5));
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(finremCaseData).errors(errors).build();
    }

    private List<String> validateDraftOrdersWrapper(DraftOrdersWrapper draftOrdersWrapper) {
        List<String> errors = new ArrayList<>();
        if (ObjectUtils.isEmpty(draftOrdersWrapper.getDraftOrdersReviewCollection())) {
            errors.add(NO_ORDERS_TO_BE_REVIEWED_MESSAGE);
            return errors;
        }
        boolean hasReviewableHearing = draftOrdersWrapper.getDraftOrdersReviewCollection().stream()
            .map(DraftOrdersReviewCollection::getValue)
            .anyMatch(review -> review.getDraftOrderDocReviewCollection().stream()
                .anyMatch(doc -> isJudgeReviewable(doc.getValue().getOrderStatus()))
                || review.getPsaDocReviewCollection().stream()
                .anyMatch(psa -> isJudgeReviewable(psa.getValue().getOrderStatus()))
            );

        if (!hasReviewableHearing) {
            errors.add(NO_ORDERS_TO_BE_REVIEWED_MESSAGE);
        }
        return errors;
    }

    private String buildHearingInfoFromDraftOrdersReview(DraftOrdersReview draftOrdersReview) {
        return hearingService.formatHearingInfo(draftOrdersReview.getHearingType(),
            draftOrdersReview.getHearingDate(), draftOrdersReview.getHearingTime(), draftOrdersReview.getHearingJudge());
    }

    private List<JudgeApproval> getReviewableItems(List<DraftOrdersReviewCollection> outstanding) {
        return outstanding.stream()
            .map(DraftOrdersReviewCollection::getValue)
            .flatMap(draftOrdersReview -> {
                String hearingInfo = buildHearingInfoFromDraftOrdersReview(draftOrdersReview);

                // Process Draft Orders
                Stream<JudgeApproval> draftOrderStream = draftOrdersReview.getDraftOrderDocReviewCollection().stream()
                    .map(DraftOrderDocReviewCollection::getValue)
                    .filter(a -> OrderStatus.isJudgeReviewable(a.getOrderStatus()))
                    .map(a -> JudgeApproval.builder()
                        .title("Draft Order")
                        .hearingInfo(hearingInfo)
                        .document(a.getDraftOrderDocument())
                        .attachments(a.getAttachments())
                        .sortKey(new SortKey(draftOrdersReview.getHearingTime(),
                            draftOrdersReview.getHearingDate(),
                            a.getSubmittedDate(), ofNullable(a.getDraftOrderDocument()).orElse(CaseDocument.builder().documentFilename("").build())
                            .getDocumentFilename()))
                        .build());

                // Process PSAs
                Stream<JudgeApproval> psaStream = draftOrdersReview.getPsaDocReviewCollection().stream()
                    .map(PsaDocReviewCollection::getValue)
                    .filter(a -> OrderStatus.isJudgeReviewable(a.getOrderStatus()))
                    .map(a -> JudgeApproval.builder()
                        .title("PSA")
                        .hearingInfo(hearingInfo)
                        .document(a.getPsaDocument())
                        .sortKey(new SortKey(draftOrdersReview.getHearingTime(),
                            draftOrdersReview.getHearingDate(),
                            a.getSubmittedDate(), ofNullable(a.getPsaDocument()).orElse(CaseDocument.builder().documentFilename("").build())
                            .getDocumentFilename()))
                        .build());

                // Combine the two streams
                return Stream.concat(draftOrderStream, psaStream);
            })
            .sorted(Comparator.comparing(JudgeApproval::getSortKey, Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();
    }

    private JudgeApproval createReviewableItems(List<DraftOrdersReviewCollection> outstanding, int index) {
        List<JudgeApproval> collection = getReviewableItems(outstanding);
        if (collection.size() < index) {
            return null;
        }
        return collection.get(index - 1);
    }

}
