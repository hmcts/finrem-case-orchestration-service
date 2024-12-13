package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.contestordernotapproved.ContestedDraftOrderNotApprovedDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Approvable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasApprovable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RefusalOrderConvertible;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UuidCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.HearingInstruction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.RefusedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.RefusedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.JUDGE_NEEDS_TO_MAKE_CHANGES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.REFUSED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.FileUtils.insertTimestamp;

@Component
@RequiredArgsConstructor
class JudgeApprovalResolver {

    private final IdamService idamService;

    private final HearingProcessor hearingProcessor;

    private final GenericDocumentService genericDocumentService;

    private final DocumentConfiguration documentConfiguration;

    private final ContestedDraftOrderNotApprovedDetailsMapper contestedDraftOrderNotApprovedDetailsMapper;

    /**
     * Populates the judge's decision for the given draft orders and updates the status of approvable documents.
     * This method processes the draft order review collection, PSA review collection, and agreed draft orders,
     * and if the judge has approved, it processes the hearing instructions as well.
     *
     * @param finremCaseDetails the finrem case details
     * @param draftOrdersWrapper the wrapper containing the draft orders to be processed
     * @param targetDoc the target document to match against the approvable items
     * @param judgeApproval the judge's approval information containing the decision
     * @param userAuthorisation the user authorization string used to fetch the judge's full name
     */
    void populateJudgeDecision(FinremCaseDetails finremCaseDetails, DraftOrdersWrapper draftOrdersWrapper, CaseDocument targetDoc,
                               JudgeApproval judgeApproval, String userAuthorisation) {
        ofNullable(draftOrdersWrapper.getDraftOrdersReviewCollection())
            .ifPresent(collection -> processApprovableCollection(collection.stream()
                .flatMap(c -> c.getValue().getDraftOrderDocReviewCollection().stream().map(DraftOrderDocReviewCollection::getValue))
                .toList(), targetDoc, judgeApproval, userAuthorisation));

        ofNullable(draftOrdersWrapper.getDraftOrdersReviewCollection())
            .ifPresent(collection -> processApprovableCollection(collection.stream()
                .flatMap(c -> c.getValue().getPsaDocReviewCollection().stream().map(PsaDocReviewCollection::getValue))
                .toList(), targetDoc, judgeApproval, userAuthorisation));

        ofNullable(draftOrdersWrapper.getAgreedDraftOrderCollection())
            .ifPresent(agreedDraftOrderCollections ->
                processApprovableCollection(agreedDraftOrderCollections.stream().map(AgreedDraftOrderCollection::getValue).toList(), targetDoc,
                    judgeApproval, userAuthorisation));

        if (isJudgeApproved(judgeApproval)) {
            ofNullable(draftOrdersWrapper.getHearingInstruction())
                .map(HearingInstruction::getAnotherHearingRequestCollection)
                .ifPresent(collection -> collection.forEach(a -> hearingProcessor.processHearingInstruction(draftOrdersWrapper, a.getValue())));
        }
        processRefusedOrders(finremCaseDetails, draftOrdersWrapper, judgeApproval, userAuthorisation);
    }

    /**
     * Processes a collection of approvable documents by matching them with the target document and handling
     * the approval process based on the judge's decision.
     *
     * @param approvables a list of approvable items to be processed
     * @param targetDoc the target document to match the approvable items against
     * @param judgeApproval the judge's approval information containing the decision
     * @param userAuthorisation the user authorization string to get the judge's full name
     */
    void processApprovableCollection(List<? extends Approvable> approvables, CaseDocument targetDoc, JudgeApproval judgeApproval,
                                     String userAuthorisation) {
        ofNullable(approvables)
            .ifPresent(list ->
                list.forEach(el -> ofNullable(el)
                    .filter(approvable -> approvable.match(targetDoc))
                    .ifPresent(approvable -> handleApprovable(approvable, judgeApproval, userAuthorisation))
                )
            );
    }

    /**
     * Handles the approval of an approvable item based on the judge's decision. If the judge has approved and
     * requested changes, the document is replaced with the amended one. The status of the approvable item is updated
     * and the approval date and judge's name are recorded.
     *
     * @param approvable the approvable item to be handled
     * @param judgeApproval the judge's approval information containing the decision
     * @param userAuthorisation the user authorization string to get the judge's full name
     */
    void handleApprovable(Approvable approvable, JudgeApproval judgeApproval, String userAuthorisation) {
        approvable.setApprovalJudge(idamService.getIdamFullName(userAuthorisation));
        if (isJudgeApproved(judgeApproval)) {
            if (judgeApproval.getJudgeDecision() == JUDGE_NEEDS_TO_MAKE_CHANGES) {
                approvable.replaceDocument(judgeApproval.getAmendedDocument());
            }
            approvable.setOrderStatus(OrderStatus.APPROVED_BY_JUDGE);
            approvable.setApprovalDate(LocalDateTime.now());
        }
        if (isJudgeRefused(judgeApproval)) {
            approvable.setOrderStatus(REFUSED);
            if (approvable instanceof RefusalOrderConvertible refusalOrderConvertible) {
                refusalOrderConvertible.setRefusedDate(LocalDateTime.now());
            }
        }
    }

    /**
     * Checks whether the judge has approved the draft order or if changes are required by the judge.
     *
     * @param judgeApproval the judge's approval information
     * @return true if the judge's decision is to approve or request changes, false otherwise
     */
    private boolean isJudgeApproved(JudgeApproval judgeApproval) {
        return ofNullable(judgeApproval).map(JudgeApproval::getJudgeDecision).map(JudgeDecision::isApproved).orElse(false);
    }

    private boolean isJudgeRefused(JudgeApproval judgeApproval) {
        return ofNullable(judgeApproval).map(JudgeApproval::getJudgeDecision).map(JudgeDecision::isRefused).orElse(false);
    }

    private CaseDocument generateRefuseOrder(FinremCaseDetails finremCaseDetails, String refusalReason, LocalDateTime refusedDate,
                                             String judgeName, JudgeType judgeType, String authorisationToken) {
        DraftOrdersWrapper draftOrdersWrapper = finremCaseDetails.getData().getDraftOrdersWrapper();
        draftOrdersWrapper.setGeneratedOrderReason(refusalReason);
        draftOrdersWrapper.setGeneratedOrderRefusedDate(refusedDate);
        draftOrdersWrapper.setGeneratedOrderJudgeName(judgeName);
        draftOrdersWrapper.setGeneratedOrderJudgeType(judgeType);

        try {
            return genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken,
                contestedDraftOrderNotApprovedDetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails,
                    finremCaseDetails.getData().getRegionWrapper().getDefaultCourtList()
                ),
                documentConfiguration.getContestedDraftOrderNotApprovedTemplate(finremCaseDetails),
                insertTimestamp(documentConfiguration.getContestedDraftOrderNotApprovedFileName()),
                finremCaseDetails.getId().toString());
        } finally {
            // Clear the temp values as they are for report generation purpose.
            draftOrdersWrapper.setGeneratedOrderReason(null);
            draftOrdersWrapper.setGeneratedOrderRefusedDate(null);
            draftOrdersWrapper.setGeneratedOrderJudgeType(null);
            draftOrdersWrapper.setGeneratedOrderJudgeName(null);
        }
    }

    private void processRefusedOrders(FinremCaseDetails finremCaseDetails, DraftOrdersWrapper draftOrdersWrapper,
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
        String judgeFeedback = judgeApproval.getChangesRequestedByJudge();
        LocalDate hearingDate = judgeApproval.getHearingDate();

        List<RefusedOrderCollection> existingRefusedOrders =
            ofNullable(draftOrdersWrapper.getRefusedOrdersCollection()).orElseGet(ArrayList::new);

        List<UUID> refusalOrderIds = new ArrayList<>();

        Function<HasApprovable, RefusedOrderCollection> toRefusedOrderCollection = item -> {
            if (item.getValue() instanceof RefusalOrderConvertible refusalOrderConvertible) {
                UUID uuid = UUID.randomUUID();
                refusalOrderIds.add(uuid);

                RefusedOrder.RefusedOrderBuilder orderBuilder = RefusedOrder.builder()
                    .refusedDocument(refusalOrderConvertible.getRefusedDocument())
                    .refusalOrder(generateRefuseOrder(finremCaseDetails, judgeFeedback, refusalOrderConvertible.getRefusedDate(),
                        refusalOrderConvertible.getApprovalJudge(), null, userAuthorisation))
                    .refusedDate(refusalOrderConvertible.getRefusedDate())
                    .submittedDate(refusalOrderConvertible.getSubmittedDate())
                    .submittedBy(refusalOrderConvertible.getSubmittedBy())
                    .submittedByEmail(refusalOrderConvertible.getSubmittedByEmail())
                    .refusalJudge(refusalOrderConvertible.getApprovalJudge())
                    .judgeFeedback(judgeFeedback)
                    .hearingDate(hearingDate);

                return RefusedOrderCollection.builder().id(uuid).value(orderBuilder.build()).build();
            } else {
                return null;
            }
        };

        List<RefusedOrderCollection> newRefusedOrders = Stream.concat(
            removedItems.stream().filter(a -> a.getValue() != null).map(toRefusedOrderCollection).filter(Objects::nonNull),
            removedPsaItems.stream().filter(a -> a.getValue() != null).map(toRefusedOrderCollection).filter(Objects::nonNull)
        ).toList();

        draftOrdersWrapper.setRefusedOrdersCollection(
            Stream.concat(existingRefusedOrders.stream(), newRefusedOrders.stream()).toList()
        );
        draftOrdersWrapper.setRefusalOrderIdsToBeSent(refusalOrderIds.stream().map(UuidCollection::new).toList());
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
