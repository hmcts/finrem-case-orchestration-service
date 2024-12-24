package uk.gov.hmcts.reform.finrem.caseorchestration.service.draftorders;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.Approvable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.HasApprovable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
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

    /**
     * Partitions a list of elements implementing {@code HasApprovable} into two groups based on their {@link OrderStatus}.
     * The partitioning is determined by the provided {@link Predicate} applied to the {@code OrderStatus}.
     *
     * @param <T>             the type of elements in the list, which must implement {@link HasApprovable}
     * @param hasApprovables  the list of elements to be partitioned; may be {@code null}, in which case an empty map is returned
     * @param statusPredicate the {@link Predicate} used to test the {@code OrderStatus} of each element
     * @return a map where the key {@code true} contains elements matching the predicate, and {@code false} contains those that do not
     */
    public <T extends HasApprovable> Map<Boolean, List<T>> partitionByOrderStatus(List<T> hasApprovables, Predicate<OrderStatus> statusPredicate) {
        return ofNullable(hasApprovables).orElse(List.of()).stream()
            .collect(partitioningBy(
                hasApprovable -> ofNullable(hasApprovable)
                    .map(HasApprovable::getValue)
                    .map(Approvable::getOrderStatus)
                    .filter(statusPredicate)
                    .isPresent()
            ));
    }

    /**
     * Filters and collects {@link DraftOrderDocReviewCollection} from a list of {@link DraftOrdersReviewCollection}
     * based on a given {@link Predicate} applied to their {@link OrderStatus}.
     * The filtered results are collected into the provided {@code collector}.
     *
     * @param draftOrdersReviewCollection the list of {@link DraftOrdersReviewCollection} to filter; may be {@code null},
     *                                    in which case an empty list is returned
     * @param collector                   the list where filtered {@link DraftOrderDocReviewCollection} will be collected
     * @param statusPredicate             the {@link Predicate} used to filter {@link DraftOrderDocReviewCollection} by their {@link OrderStatus}
     * @return a list of filtered {@link DraftOrdersReviewCollection}, each containing the filtered {@link DraftOrderDocReviewCollection}
     */
    public List<DraftOrdersReviewCollection> filterAndCollectDraftOrderDocs(
        List<DraftOrdersReviewCollection> draftOrdersReviewCollection,
        List<DraftOrderDocReviewCollection> collector,
        Predicate<OrderStatus> statusPredicate) {
        return filterAndCollect(
            draftOrdersReviewCollection, collector, statusPredicate,
            DraftOrdersReview::getDraftOrderDocReviewCollection,
            DraftOrdersReview.DraftOrdersReviewBuilder::draftOrderDocReviewCollection
        );
    }

    /**
     * Filters and collects {@link PsaDocReviewCollection} from a list of {@link DraftOrdersReviewCollection}
     * based on a given {@link Predicate} applied to their {@link OrderStatus}.
     * The filtered results are collected into the provided {@code collector}.
     *
     * @param draftOrdersReviewCollection the list of {@link DraftOrdersReviewCollection} to filter; may be {@code null},
     *                                    in which case an empty list is returned
     * @param collector                   the list where filtered {@link PsaDocReviewCollection} will be collected
     * @param statusPredicate             the {@link Predicate} used to filter {@link PsaDocReviewCollection} by their {@link OrderStatus}
     * @return a list of filtered {@link DraftOrdersReviewCollection}, each containing the filtered {@link PsaDocReviewCollection}
     */
    public List<DraftOrdersReviewCollection> filterAndCollectPsaDocs(
        List<DraftOrdersReviewCollection> draftOrdersReviewCollection,
        List<PsaDocReviewCollection> collector,
        Predicate<OrderStatus> statusPredicate) {
        return filterAndCollect(
            draftOrdersReviewCollection, collector, statusPredicate,
            DraftOrdersReview::getPsaDocReviewCollection,
            DraftOrdersReview.DraftOrdersReviewBuilder::psaDocReviewCollection
        );
    }

    /**
     * Collects agreed draft orders from the given list that match the specified status predicate
     * and adds them to the provided collector list.
     *
     * @param agreedDraftOrderCollections the list of agreed draft orders to process;
     *                                     if null, no processing is performed.
     * @param collector                   the list where matching agreed draft orders are collected;
     *                                     if null, no items are added.
     * @param statusPredicate             the predicate used to determine the order status to collect.
     *                                     Orders that satisfy this predicate are added to the collector.
     */
    public void collectAgreedDraftOrders(
        List<AgreedDraftOrderCollection> agreedDraftOrderCollections,
        List<AgreedDraftOrderCollection> collector,
        Predicate<OrderStatus> statusPredicate) {

        ofNullable(agreedDraftOrderCollections).orElse(List.of())
            .forEach(draftOrdersReview -> {
                Map<Boolean, List<AgreedDraftOrderCollection>> partitioned = partitionByOrderStatus(agreedDraftOrderCollections, statusPredicate);
                collector.addAll(partitioned.getOrDefault(true, List.of()));
            });
    }

    private <T extends HasApprovable> List<DraftOrdersReviewCollection> filterAndCollect(
        List<DraftOrdersReviewCollection> draftOrdersReviewCollection,
        List<T> collector,
        Predicate<OrderStatus> statusPredicate,
        Function<DraftOrdersReview, List<T>> getReviewCollection,
        BiConsumer<DraftOrdersReview.DraftOrdersReviewBuilder, List<T>> setReviewCollection) {

        return ofNullable(draftOrdersReviewCollection).orElse(List.of()).stream()
            .map(draftOrdersReview -> {
                DraftOrdersReview.DraftOrdersReviewBuilder updatedReviewBuilder = draftOrdersReview.getValue().toBuilder();

                // Partition items into kept and removed
                Map<Boolean, List<T>> partitioned =
                    partitionByOrderStatus(getReviewCollection.apply(draftOrdersReview.getValue()), statusPredicate);

                // Keep the items not matching the status
                setReviewCollection.accept(updatedReviewBuilder, partitioned.get(false));

                // Collect the removed items
                if (collector != null) {
                    collector.addAll(partitioned.get(true));
                }

                // Create a new DraftOrdersReviewCollection
                DraftOrdersReviewCollection updatedCollection = new DraftOrdersReviewCollection();
                updatedCollection.setValue(updatedReviewBuilder.build());
                return updatedCollection;
            })
            .toList();
    }
}
