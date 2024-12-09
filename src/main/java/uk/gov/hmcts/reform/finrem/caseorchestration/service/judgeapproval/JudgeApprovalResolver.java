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

    private boolean isJudgeApproved(JudgeApproval judgeApproval) {
        return ofNullable(judgeApproval).map(JudgeApproval::getJudgeDecision).map(JudgeDecision::isApproved).orElse(false);
    }

    private boolean isJudgeRefused(JudgeApproval judgeApproval) {
        return ofNullable(judgeApproval).map(JudgeApproval::getJudgeDecision).map(JudgeDecision::isRefused).orElse(false);
    }

    private final GenericDocumentService genericDocumentService;

    private final DocumentConfiguration documentConfiguration;

    private final ContestedDraftOrderNotApprovedDetailsMapper contestedDraftOrderNotApprovedDetailsMapper;

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
        filterAgreedDraftOrderCollections(draftOrdersWrapper, REFUSED);

        // remove refused draft orders/PSAs from PsaDocReviewCollection and DraftOrderDocReviewCollection and collect them
        draftOrdersWrapper.setDraftOrdersReviewCollection(filterAndCollectRemovedItemsFromDraftOrderDocReviewCollection(removedItems,
            draftOrdersWrapper.getDraftOrdersReviewCollection(), REFUSED));
        draftOrdersWrapper.setDraftOrdersReviewCollection(filterAndCollectRemovedItemsFromPsaDocReviewCollection(removedPsaItems,
            draftOrdersWrapper.getDraftOrdersReviewCollection(), REFUSED));

        // create RefusedOrder from collected items.
        String judgeFeedback = judgeApproval.getChangesRequestedByJudge();
        LocalDate hearingDate = judgeApproval.getHearingDate();

        List<RefusedOrderCollection> existingRefusedOrders =
            ofNullable(draftOrdersWrapper.getRefusedOrdersCollection()).orElseGet(ArrayList::new);

        Function<HasApprovable, RefusedOrderCollection> toRefusedOrderCollection = item -> {
            if (item.getValue() instanceof RefusalOrderConvertible refusalOrderConvertible) {
                RefusedOrder.RefusedOrderBuilder orderBuilder = RefusedOrder.builder()
                    .draftOrderOrPsa(refusalOrderConvertible.getDraftOrderOrPsa())
                    .refusalOrder(generateRefuseOrder(finremCaseDetails, judgeFeedback, refusalOrderConvertible.getRefusedDate(),
                        refusalOrderConvertible.getApprovalJudge(), null, userAuthorisation))
                    .refusedDate(refusalOrderConvertible.getRefusedDate())
                    .submittedDate(refusalOrderConvertible.getSubmittedDate())
                    .submittedBy(refusalOrderConvertible.getSubmittedBy())
                    .submittedByEmail(refusalOrderConvertible.getSubmittedByEmail())
                    .refusalJudge(refusalOrderConvertible.getApprovalJudge())
                    .judgeFeedback(judgeFeedback)
                    .hearingDate(hearingDate);

                return RefusedOrderCollection.builder().value(orderBuilder.build()).build();
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
    }

    private List<DraftOrdersReviewCollection> filterAndCollectRemovedItemsFromDraftOrderDocReviewCollection(
        List<DraftOrderDocReviewCollection> removedItems,
        List<DraftOrdersReviewCollection> draftOrdersReviewCollection,
        OrderStatus statusToRemove) {
        return filterAndCollectRemovedItemsFromReviewCollection(
            removedItems,
            draftOrdersReviewCollection,
            statusToRemove,
            DraftOrdersReview::getDraftOrderDocReviewCollection,
            DraftOrdersReview.DraftOrdersReviewBuilder::draftOrderDocReviewCollection
        );
    }

    private List<DraftOrdersReviewCollection> filterAndCollectRemovedItemsFromPsaDocReviewCollection(
        List<PsaDocReviewCollection> removedItems,
        List<DraftOrdersReviewCollection> draftOrdersReviewCollection,
        OrderStatus statusToRemove) {
        return filterAndCollectRemovedItemsFromReviewCollection(
            removedItems,
            draftOrdersReviewCollection,
            statusToRemove,
            DraftOrdersReview::getPsaDocReviewCollection,
            DraftOrdersReview.DraftOrdersReviewBuilder::psaDocReviewCollection
        );
    }

    private <T extends HasApprovable> List<DraftOrdersReviewCollection> filterAndCollectRemovedItemsFromReviewCollection(
        List<T> removedItemsCollector,
        List<DraftOrdersReviewCollection> draftOrdersReviewCollection,
        OrderStatus statusToRemove,
        Function<DraftOrdersReview, List<T>> getReviewCollection,
        BiConsumer<DraftOrdersReview.DraftOrdersReviewBuilder, List<T>> setReviewCollection) {

        return draftOrdersReviewCollection.stream()
            .map(draftOrdersReview -> {
                DraftOrdersReview.DraftOrdersReviewBuilder updatedReviewBuilder = draftOrdersReview.getValue().toBuilder();

                // Partition items into kept and removed
                Map<Boolean, List<T>> partitioned =
                    partitionDraftOrderDocReviewCollection(
                        getReviewCollection.apply(draftOrdersReview.getValue()),
                        statusToRemove
                    );

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

    private void filterAgreedDraftOrderCollections(DraftOrdersWrapper draftOrdersWrapper,
        OrderStatus statusToRemove) {
        Map<Boolean, List<AgreedDraftOrderCollection>> partitioned =
            partitionDraftOrderDocReviewCollection(
                draftOrdersWrapper.getAgreedDraftOrderCollection(),
                statusToRemove
            );
        draftOrdersWrapper.setAgreedDraftOrderCollection(partitioned.get(false));
    }

    private <T extends HasApprovable> Map<Boolean, List<T>> partitionDraftOrderDocReviewCollection(
        List<T> draftOrderDocReviewCollection,
        OrderStatus statusToRemove) {
        return draftOrderDocReviewCollection.stream()
            .collect(Collectors.partitioningBy(
                docReview -> ofNullable(docReview)
                    .map(HasApprovable::getValue)
                    .map(Approvable::getOrderStatus)
                    .filter(statusToRemove::equals)
                    .isPresent()
            ));
    }

}
