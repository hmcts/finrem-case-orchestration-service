package uk.gov.hmcts.reform.finrem.caseorchestration.service.draftorders;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.Approvable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.HasApprovable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.partitioningBy;

@Component
public class HasApprovableCollectionReader {

    public <T extends HasApprovable> Map<Boolean, List<T>> partitionHasApprovablesByStatus(List<T> hasApprovables,
                                                                                           Predicate<OrderStatus> statusPredicate) {
        return ofNullable(hasApprovables).orElse(List.of()).stream()
            .collect(partitioningBy(
                hasApprovable -> ofNullable(hasApprovable)
                    .map(HasApprovable::getValue)
                    .map(Approvable::getOrderStatus)
                    .filter(statusPredicate)
                    .isPresent()
            ));
    }

    public List<DraftOrdersReviewCollection> filterAndCollectFromDraftOrderDocReviewCollection(
        List<DraftOrderDocReviewCollection> collector,
        List<DraftOrdersReviewCollection> draftOrdersReviewCollection,
        Predicate<OrderStatus> statusPredicate) {
        return filterAndCollectFromDraftOrdersReviewCollection(
            collector,
            draftOrdersReviewCollection,
            DraftOrdersReview::getDraftOrderDocReviewCollection,
            DraftOrdersReview.DraftOrdersReviewBuilder::draftOrderDocReviewCollection,
            statusPredicate
        );
    }

    public List<DraftOrdersReviewCollection> filterAndCollectFromPsaDocReviewCollection(
        List<PsaDocReviewCollection> collector,
        List<DraftOrdersReviewCollection> draftOrdersReviewCollection,
        Predicate<OrderStatus> statusPredicate) {
        return filterAndCollectFromDraftOrdersReviewCollection(
            collector,
            draftOrdersReviewCollection,
            DraftOrdersReview::getPsaDocReviewCollection,
            DraftOrdersReview.DraftOrdersReviewBuilder::psaDocReviewCollection,
            statusPredicate
        );
    }

    private <T extends HasApprovable> List<DraftOrdersReviewCollection> filterAndCollectFromDraftOrdersReviewCollection(
        List<T> removedItemsCollector,
        List<DraftOrdersReviewCollection> draftOrdersReviewCollection,
        Function<DraftOrdersReview, List<T>> getReviewCollection,
        BiConsumer<DraftOrdersReview.DraftOrdersReviewBuilder, List<T>> setReviewCollection,
        Predicate<OrderStatus> statusPredicate) {

        return ofNullable(draftOrdersReviewCollection).orElse(List.of()).stream()
            .map(draftOrdersReview -> {
                DraftOrdersReview.DraftOrdersReviewBuilder updatedReviewBuilder = draftOrdersReview.getValue().toBuilder();

                // Partition items into kept and removed
                Map<Boolean, List<T>> partitioned =
                    partitionHasApprovablesByStatus(getReviewCollection.apply(draftOrdersReview.getValue()), statusPredicate);

                // Keep the items not matching the status
                setReviewCollection.accept(updatedReviewBuilder, partitioned.get(false));

                // Collect the removed items
                if (removedItemsCollector != null) {
                    removedItemsCollector.addAll(partitioned.get(true));
                }

                // Create a new DraftOrdersReviewCollection
                DraftOrdersReviewCollection updatedCollection = new DraftOrdersReviewCollection();
                updatedCollection.setValue(updatedReviewBuilder.build());
                return updatedCollection;
            })
            .toList();
    }
}
