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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.JUDGE_NEEDS_TO_MAKE_CHANGES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.REFUSED;

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
        moveRefusedDraftOrdersAndPsaToRefusedOrders(finremCaseDetails, draftOrdersWrapper, judgeApproval, userAuthorisation);
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
            approvable.setRefusedDate(LocalDateTime.now());
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
                documentConfiguration.getContestedDraftOrderNotApprovedFileName(),
                finremCaseDetails.getId().toString());
        } finally {
            // Clear the temp values as they are for report generation purpose.
            draftOrdersWrapper.setGeneratedOrderReason(null);
            draftOrdersWrapper.setGeneratedOrderRefusedDate(null);
            draftOrdersWrapper.setGeneratedOrderJudgeType(null);
            draftOrdersWrapper.setGeneratedOrderJudgeName(null);
        }
    }

    private void moveRefusedDraftOrdersAndPsaToRefusedOrders(FinremCaseDetails finremCaseDetails, DraftOrdersWrapper draftOrdersWrapper,
                                                             JudgeApproval judgeApproval, String userAuthorisation) {
        List<DraftOrderDocReviewCollection> removedItems = new ArrayList<>();
        List<PsaDocReviewCollection> removedPsaItems = new ArrayList<>();
        draftOrdersWrapper.setDraftOrdersReviewCollection(filterAndCollectRemovedItemsFromDraftOrderDocReviewCollection(removedItems,
            draftOrdersWrapper.getDraftOrdersReviewCollection(), REFUSED));
        draftOrdersWrapper.setDraftOrdersReviewCollection(filterAndCollectRemovedItemsFromPsaDocReviewCollection(removedPsaItems,
            draftOrdersWrapper.getDraftOrdersReviewCollection(), REFUSED));

        draftOrdersWrapper.setRefusedOrdersCollection(
            Stream.concat(
                Stream.concat(
                    ofNullable(draftOrdersWrapper.getRefusedOrdersCollection()).orElseGet(ArrayList::new).stream(),
                    removedItems.stream()
                        .filter(a -> a.getValue() != null)
                        .map(a -> RefusedOrderCollection.builder()
                            .value(RefusedOrder.builder()
                                .draftOrderOrPsa(a.getValue().getDraftOrderDocument())
                                .refusalOrder(generateRefuseOrder(finremCaseDetails, judgeApproval.getChangesRequestedByJudge(),
                                    a.getValue().getRefusedDate(), a.getValue().getApprovalJudge(), null, userAuthorisation))
                                .refusedDate(a.getValue().getRefusedDate())
                                .submittedDate(a.getValue().getSubmittedDate())
                                .submittedBy(a.getValue().getSubmittedBy())
                                .attachments(a.getValue().getAttachments())
                                .refusalJudge(a.getValue().getApprovalJudge())
                                .build())
                            .build())),
                removedPsaItems.stream()
                    .filter(a -> a.getValue() != null)
                    .map(a -> RefusedOrderCollection.builder()
                        .value(RefusedOrder.builder()
                            .draftOrderOrPsa(a.getValue().getPsaDocument())
                            .refusalOrder(generateRefuseOrder(finremCaseDetails, judgeApproval.getChangesRequestedByJudge(),
                                a.getValue().getRefusedDate(), a.getValue().getApprovalJudge(), null, userAuthorisation))
                            .refusedDate(a.getValue().getRefusedDate())
                            .submittedDate(a.getValue().getSubmittedDate())
                            .submittedBy(a.getValue().getSubmittedBy())
                            .refusalJudge(a.getValue().getApprovalJudge())
                            .build())
                        .build())
            ).toList()
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
        List<T> removedItems,
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
                removedItems.addAll(partitioned.get(true));

                // Create a new DraftOrdersReviewCollection
                DraftOrdersReviewCollection updatedCollection = new DraftOrdersReviewCollection();
                updatedCollection.setValue(updatedReviewBuilder.build());
                return updatedCollection;
            })
            .toList();
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
