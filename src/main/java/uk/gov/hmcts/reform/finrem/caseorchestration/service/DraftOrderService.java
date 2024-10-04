package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftOrderReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftOrderReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderToBeReviewed;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionSharingAnnexCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;

@Service
@Slf4j
@RequiredArgsConstructor
public class DraftOrderService {

    public List<DraftOrderReview> getOutstandingDraftOrderReviews(FinremCaseDetails caseDetails, int daysSinceOrderUpload) {
        DraftOrdersWrapper draftOrdersWrapper = caseDetails.getData().getDraftOrdersWrapper();
        return getDraftOrderReviewsToBeReviewed(draftOrdersWrapper, daysSinceOrderUpload);
    }

    // Method to get DraftOrderReview objects based on the specified conditions
    static List<DraftOrderReview> getDraftOrderReviewsToBeReviewed(DraftOrdersWrapper draftOrdersWrapper, int daysSinceOrderUpload) {
        return ofNullable(draftOrdersWrapper.getDraftOrderReviewCollection())
            .map(draftOrderReviewCollection -> draftOrderReviewCollection.stream()
                .map(DraftOrderReviewCollection::getValue)
                .filter(draftOrderReview -> draftOrderReview != null && draftOrderReview.getAgreedDraftOrderCollection() != null)
                .filter(draftOrderReview -> draftOrderReview.getAgreedDraftOrderCollection().stream()
                    .map(AgreedDraftOrderCollection::getValue)
                    .anyMatch(agreedDraftOrder -> agreedDraftOrder != null
                        && (checkOrderCondition(ofNullable(agreedDraftOrder.getDraftOrderCollection()).orElse(List.of()).stream()
                        .map(DraftOrderCollection::getValue)
                        .toList(), daysSinceOrderUpload)
                        || checkOrderCondition(ofNullable(agreedDraftOrder.getPensionSharingAnnexCollection()).orElse(List.of()).stream()
                        .map(PensionSharingAnnexCollection::getValue).toList(), daysSinceOrderUpload))))
                .toList())
            .orElse(new ArrayList<>());
    }

    // Generic helper method to check if any OrderToBeReviewed meets the condition (OrderStatus and submittedDate)
    static <T extends OrderToBeReviewed> boolean checkOrderCondition(List<? extends T> orderCollections, int daysSinceOrderUpload) {
        return orderCollections != null && orderCollections.stream()
            .anyMatch(order -> order != null
                && (order.getStatus() == null || order.getStatus() == OrderStatus.REVIEW_LATER)
                && order.getNotificationSentDate() == null
                && order.getSubmittedDate() != null
                && order.getSubmittedDate().isBefore(LocalDate.now().minusDays(daysSinceOrderUpload)));
    }

}
