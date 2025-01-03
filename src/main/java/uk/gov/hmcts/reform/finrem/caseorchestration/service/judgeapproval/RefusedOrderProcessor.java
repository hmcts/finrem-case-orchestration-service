package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UuidCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.Approvable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.HasApprovable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.RefusalOrderConvertible;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.RefusalOrderInstruction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.RefusedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.RefusedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.REFUSED;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefusedOrderProcessor {

    private final RefusedOrderGenerator refusedOrderGenerator;

    /**
     * Processes refused draft orders and pension sharing annexes (PSAs) by removing them from collections,
     * creating refused orders, and updating the {@link DraftOrdersWrapper}.
     *
     * <p>This method performs the following actions:</p>
     * <ul>
     *     <li>Removes refused draft orders and PSAs from their respective collections in the {@link DraftOrdersWrapper}.</li>
     *     <li>Collects the removed items and converts them into {@link RefusedOrderCollection} objects.</li>
     *     <li>Creates a refusal order document for each refused item, including judge feedback and hearing details.</li>
     *     <li>Updates the {@link DraftOrdersWrapper} with the new list of refused orders and the associated UUIDs.</li>
     * </ul>
     *
     * @param finremCaseDetails  the case details containing data required to generate refusal orders
     * @param draftOrdersWrapper the wrapper object containing collections of draft orders and PSAs
     * @param judgeApproval      the judge's decision details, including feedback and hearing date
     * @param userAuthorisation  the authorisation token for the current user
     */
    public void processRefusedOrders(FinremCaseDetails finremCaseDetails, DraftOrdersWrapper draftOrdersWrapper,
                                     JudgeApproval judgeApproval, String userAuthorisation) {
        List<DraftOrderDocReviewCollection> removedItems = new ArrayList<>();
        List<PsaDocReviewCollection> removedPsaItems = new ArrayList<>();

        // remove refused draft orders/PSAs from agreedDraftOrderCollection
        filterRefusedDraftOrderCollections(draftOrdersWrapper);

        // remove refused draft orders/PSAs from PsaDocReviewCollection and DraftOrderDocReviewCollection and collect them
        draftOrdersWrapper.setDraftOrdersReviewCollection(filterAndCollectRefusedItemsFromDraftOrderDocReviewCollection(removedItems,
            draftOrdersWrapper.getDraftOrdersReviewCollection()));
        draftOrdersWrapper.setDraftOrdersReviewCollection(filterAndCollectRefusedItemsFromPsaDocReviewCollection(removedPsaItems,
            draftOrdersWrapper.getDraftOrdersReviewCollection()));

        // create RefusedOrder from collected items.
        final String judgeFeedback = judgeApproval.getChangesRequestedByJudge();
        final LocalDate hearingDate = judgeApproval.getHearingDate();

        Function<HasApprovable, RefusedOrderCollection> toRefusedOrderCollection = item -> {
            if (item.getValue() instanceof RefusalOrderConvertible refusalOrderConvertible) {
                UUID uuid = UUID.randomUUID();

                JudgeType judgeType = ofNullable(draftOrdersWrapper.getRefusalOrderInstruction()).map(RefusalOrderInstruction::getJudgeType)
                    .orElse(null);
                if (judgeType == null) {
                    log.warn("{} - Judge type was not captured and an empty string will be shown in the refusal order.",
                        finremCaseDetails.getId());
                }

                RefusedOrder refusedOrder = RefusedOrder.builder()
                    .refusedDocument(refusalOrderConvertible.getRefusedDocument())
                    .refusalOrder(refusedOrderGenerator.generateRefuseOrder(finremCaseDetails, judgeFeedback,
                        refusalOrderConvertible.getRefusedDate(), refusalOrderConvertible.getApprovalJudge(), judgeType, userAuthorisation))
                    .refusedDate(refusalOrderConvertible.getRefusedDate())
                    .submittedDate(refusalOrderConvertible.getSubmittedDate())
                    .submittedBy(refusalOrderConvertible.getSubmittedBy())
                    .orderParty(refusalOrderConvertible.getOrderParty())
                    .refusalJudge(refusalOrderConvertible.getApprovalJudge())
                    .attachments(item.getValue() instanceof DraftOrderDocumentReview d ? d.getAttachments() : null)
                    .judgeFeedback(judgeFeedback)
                    .hearingDate(hearingDate)
                    .judgeType(judgeType)
                    .build();

                return RefusedOrderCollection.builder()
                    .id(uuid)
                    .value(refusedOrder)
                    .build();
            } else {
                return null;
            }
        };

        List<RefusedOrderCollection> newRefusedOrders = Stream.concat(
            removedItems.stream().filter(a -> a.getValue() != null).map(toRefusedOrderCollection).filter(Objects::nonNull),
            removedPsaItems.stream().filter(a -> a.getValue() != null).map(toRefusedOrderCollection).filter(Objects::nonNull)
        ).toList();

        List<UuidCollection> newRefusalOrderIds = newRefusedOrders.stream()
            .map(RefusedOrderCollection::getId)
            .map(UuidCollection::new)
            .toList();
        List<UuidCollection> existingRefusalOrderIds = Optional.ofNullable(draftOrdersWrapper.getRefusalOrderIdsToBeSent())
            .orElse(new ArrayList<>());
        existingRefusalOrderIds.addAll(newRefusalOrderIds);
        draftOrdersWrapper.setRefusalOrderIdsToBeSent(existingRefusalOrderIds);

        List<RefusedOrderCollection> existingRefusedOrders =
            ofNullable(draftOrdersWrapper.getRefusedOrdersCollection()).orElseGet(ArrayList::new);
        draftOrdersWrapper.setRefusedOrdersCollection(
            Stream.concat(existingRefusedOrders.stream(), newRefusedOrders.stream()).toList()
        );
    }

    private List<DraftOrdersReviewCollection> filterAndCollectRefusedItemsFromDraftOrderDocReviewCollection(
        List<DraftOrderDocReviewCollection> removedItems,
        List<DraftOrdersReviewCollection> draftOrdersReviewCollection) {
        return filterAndCollectRefusedItemsFromReviewCollection(
            removedItems,
            draftOrdersReviewCollection,
            DraftOrdersReview::getDraftOrderDocReviewCollection,
            DraftOrdersReview.DraftOrdersReviewBuilder::draftOrderDocReviewCollection
        );
    }

    private List<DraftOrdersReviewCollection> filterAndCollectRefusedItemsFromPsaDocReviewCollection(
        List<PsaDocReviewCollection> removedItems,
        List<DraftOrdersReviewCollection> draftOrdersReviewCollection) {
        return filterAndCollectRefusedItemsFromReviewCollection(
            removedItems,
            draftOrdersReviewCollection,
            DraftOrdersReview::getPsaDocReviewCollection,
            DraftOrdersReview.DraftOrdersReviewBuilder::psaDocReviewCollection
        );
    }

    private <T extends HasApprovable> List<DraftOrdersReviewCollection> filterAndCollectRefusedItemsFromReviewCollection(
        List<T> removedItemsCollector,
        List<DraftOrdersReviewCollection> draftOrdersReviewCollection,
        Function<DraftOrdersReview, List<T>> getReviewCollection,
        BiConsumer<DraftOrdersReview.DraftOrdersReviewBuilder, List<T>> setReviewCollection) {

        return ofNullable(draftOrdersReviewCollection).orElse(List.of()).stream()
            .map(draftOrdersReview -> {
                DraftOrdersReview.DraftOrdersReviewBuilder updatedReviewBuilder = draftOrdersReview.getValue().toBuilder();

                // Partition items into kept and removed
                Map<Boolean, List<T>> partitioned =
                    partitionRefusedDraftOrderDocReviewCollection(getReviewCollection.apply(draftOrdersReview.getValue()));

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

    private void filterRefusedDraftOrderCollections(DraftOrdersWrapper draftOrdersWrapper) {
        Map<Boolean, List<AgreedDraftOrderCollection>> partitioned =
            partitionRefusedDraftOrderDocReviewCollection(draftOrdersWrapper.getAgreedDraftOrderCollection());
        draftOrdersWrapper.setAgreedDraftOrderCollection(partitioned.get(false));
    }

    private <T extends HasApprovable> Map<Boolean, List<T>> partitionRefusedDraftOrderDocReviewCollection(List<T> draftOrderDocReviewCollection) {
        return ofNullable(draftOrderDocReviewCollection).orElse(List.of()).stream()
            .collect(Collectors.partitioningBy(
                docReview -> ofNullable(docReview)
                    .map(HasApprovable::getValue)
                    .map(Approvable::getOrderStatus)
                    .filter(REFUSED::equals)
                    .isPresent()
            ));
    }
}
