package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContestedCourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Approvable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.HearingInstruction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
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
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_JUDGE_TYPE;
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
    private final DocumentHelper documentHelper;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;
    private final BiFunction<FinremCaseDetails, String, CaseDetails> addExtraFields = this::applyAddExtraFields;

    private CaseDocument generateRefuseOrder(FinremCaseDetails finremCaseDetails, String reason, String authorisationToken) {
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        return genericDocumentService.generateDocument(authorisationToken, addExtraFields.apply(finremCaseDetails, reason),
            documentConfiguration.getContestedDraftOrderNotApprovedTemplate(caseDetails),
            documentConfiguration.getContestedDraftOrderNotApprovedFileName());
    }

    private CaseDetails applyAddExtraFields(FinremCaseDetails finremCaseDetails, String refusalReason) {
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);

        caseDetails.getData().put("ApplicantName", documentHelper.getApplicantFullName(caseDetails));
        caseDetails.getData().put("RespondentName", documentHelper.getRespondentFullNameContested(caseDetails));
        caseDetails.getData().put("Court", ContestedCourtHelper.getSelectedCourt(caseDetails));
        caseDetails.getData().put("JudgeDetails",
            StringUtils.joinWith(" ",
                caseDetails.getData().get(CONTESTED_APPLICATION_NOT_APPROVED_JUDGE_TYPE),
                caseDetails.getData().get(CONTESTED_APPLICATION_NOT_APPROVED_JUDGE_NAME)));
        caseDetails.getData().put("ContestOrderNotApprovedRefusalReasonsFormatted", refusalReason);

        return caseDetails;
    }

    void moveRefusedDraftOrdersAndPsaToRefusedOrders(FinremCaseDetails finremCaseDetails, DraftOrdersWrapper draftOrdersWrapper,
                                                     JudgeApproval judgeApproval, String userAuthorisation) {
        List<DraftOrderDocReviewCollection> removedItems = new ArrayList<>();
        draftOrdersWrapper.setDraftOrdersReviewCollection(filterDraftOrdersReviewCollectionWithRemovedItems(removedItems,
            draftOrdersWrapper.getDraftOrdersReviewCollection(), REFUSED));

        List<RefusedOrderCollection> modifiedRefusedOrdersCollection = ofNullable(draftOrdersWrapper.getRefusedOrdersCollection())
            .orElse(new ArrayList<>());
        modifiedRefusedOrdersCollection.addAll(removedItems.stream()
            .map(a -> RefusedOrderCollection.builder()
                .value(RefusedOrder.builder()
                    .draftOrderOrPsa(a.getValue().getDraftOrderDocument())
                    .refusalOrder(generateRefuseOrder(finremCaseDetails, judgeApproval.getChangesRequestedByJudge(), userAuthorisation))
                    .refusedDate(a.getValue().getRefusedDate())
                    .submittedBy(a.getValue().getSubmittedBy())
                    .attachments(a.getValue().getAttachments())
                    .refusalJudge(a.getValue().getApprovalJudge())
                    .build())
                .build())
            .toList());
        draftOrdersWrapper.setRefusedOrdersCollection(modifiedRefusedOrdersCollection);
    }

    public List<DraftOrdersReviewCollection> filterDraftOrdersReviewCollectionWithRemovedItems(
        List<DraftOrderDocReviewCollection> removedItems,
        List<DraftOrdersReviewCollection> draftOrdersReviewCollection,
        OrderStatus statusToRemove) {

        return draftOrdersReviewCollection.stream()
            .map(draftOrdersReview -> {
                DraftOrdersReview.DraftOrdersReviewBuilder updatedReviewBuilder = draftOrdersReview.getValue().toBuilder();

                // Partition items into kept and removed
                Map<Boolean, List<DraftOrderDocReviewCollection>> partitioned =
                    partitionDraftOrderDocReviewCollection(
                        draftOrdersReview.getValue().getDraftOrderDocReviewCollection(),
                        statusToRemove
                    );

                // Keep the items not matching the status
                updatedReviewBuilder.draftOrderDocReviewCollection(partitioned.get(false));

                // Collect the removed items
                removedItems.addAll(partitioned.get(true));

                // Create a new DraftOrdersReviewCollection
                DraftOrdersReviewCollection updatedCollection = new DraftOrdersReviewCollection();
                updatedCollection.setValue(updatedReviewBuilder.build());
                return updatedCollection;
            })
            .toList();
    }

    private Map<Boolean, List<DraftOrderDocReviewCollection>> partitionDraftOrderDocReviewCollection(
        List<DraftOrderDocReviewCollection> draftOrderDocReviewCollection,
        OrderStatus statusToRemove) {
        return draftOrderDocReviewCollection.stream()
            .collect(Collectors.partitioningBy(
                docReview -> ofNullable(docReview)
                    .map(DraftOrderDocReviewCollection::getValue)
                    .map(DraftOrderDocumentReview::getOrderStatus)
                    .filter(statusToRemove::equals)
                    .isPresent()
            ));
    }

}
