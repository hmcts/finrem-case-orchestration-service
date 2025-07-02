package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.Approvable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.RefusalOrderConvertible;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.HearingInstruction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.JUDGE_NEEDS_TO_MAKE_CHANGES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.APPROVED_BY_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.REFUSED;

@Component
@RequiredArgsConstructor
class JudgeApprovalResolver {

    private final IdamService idamService;

    private final HearingProcessor hearingProcessor;

    private final RefusedOrderProcessor refusedOrderProcessor;

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
        // Process objects under Draft Order Tab
        processDraftOrderDocReviewCollection(draftOrdersWrapper, targetDoc, judgeApproval, userAuthorisation);
        processPsaDocReviewCollection(draftOrdersWrapper, targetDoc, judgeApproval, userAuthorisation);

        // Process objects under Case Documents Tab
        processAgreedDraftOrderCollection(draftOrdersWrapper, targetDoc, judgeApproval, userAuthorisation);

        if (isJudgeApproved(judgeApproval)) {
            processHearingInstruction(draftOrdersWrapper);

        }
        refusedOrderProcessor.processRefusedOrders(finremCaseDetails, draftOrdersWrapper, judgeApproval, userAuthorisation);
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
        boolean approved = isJudgeApproved(judgeApproval);
        boolean refused = isJudgeRefused(judgeApproval);
        if (approved || refused) {
            approvable.setApprovalJudge(idamService.getIdamFullName(userAuthorisation));
            if (approved) {
                approvable.setFinalOrder(YesOrNo.forValue(isFinalOrderSelected(judgeApproval)));
                if (judgeApproval.getJudgeDecision() == JUDGE_NEEDS_TO_MAKE_CHANGES) {
                    approvable.replaceDocument(judgeApproval.getAmendedDocument());
                }
                approvable.setOrderStatus(APPROVED_BY_JUDGE);
                //TODO: Set Court order date from judge approval input - revert below.
                // approvable.setCourtOrderDate(court order date);
                approvable.setApprovalDate(LocalDateTime.now());
            }
            if (refused) {
                approvable.setOrderStatus(REFUSED);
                if (approvable instanceof RefusalOrderConvertible refusalOrderConvertible) {
                    refusalOrderConvertible.setRefusedDate(LocalDateTime.now());
                }
            }

            //TODO: Set cover letter for both approved and refused orders
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

    /**
     * Checks whether the judge has refused the draft order.
     *
     * @param judgeApproval the judge's approval information
     * @return true if the judge's decision is to refuse, false otherwise
     */
    private boolean isJudgeRefused(JudgeApproval judgeApproval) {
        return ofNullable(judgeApproval).map(JudgeApproval::getJudgeDecision).map(JudgeDecision::isRefused).orElse(false);
    }

    private void processDraftOrderDocReviewCollection(DraftOrdersWrapper draftOrdersWrapper, CaseDocument targetDoc, JudgeApproval judgeApproval,
                                                      String userAuthorisation) {
        ofNullable(draftOrdersWrapper.getDraftOrdersReviewCollection())
            .ifPresent(collection -> processApprovableCollection(collection.stream()
                .flatMap(c -> c.getValue().getDraftOrderDocReviewCollection().stream().map(DraftOrderDocReviewCollection::getValue))
                .toList(), targetDoc, judgeApproval, userAuthorisation));
    }

    private void processPsaDocReviewCollection(DraftOrdersWrapper draftOrdersWrapper, CaseDocument targetDoc, JudgeApproval judgeApproval,
                                               String userAuthorisation) {
        ofNullable(draftOrdersWrapper.getDraftOrdersReviewCollection())
            .ifPresent(collection -> processApprovableCollection(collection.stream()
                .flatMap(c -> c.getValue().getPsaDocReviewCollection().stream().map(PsaDocReviewCollection::getValue))
                .toList(), targetDoc, judgeApproval, userAuthorisation));
    }

    private void processAgreedDraftOrderCollection(DraftOrdersWrapper draftOrdersWrapper, CaseDocument targetDoc, JudgeApproval judgeApproval,
                                                   String userAuthorisation) {
        ofNullable(draftOrdersWrapper.getAgreedDraftOrderCollection())
            .ifPresent(agreedDraftOrderCollections ->
                processApprovableCollection(agreedDraftOrderCollections.stream().map(AgreedDraftOrderCollection::getValue).toList(), targetDoc,
                    judgeApproval, userAuthorisation));
        ofNullable(draftOrdersWrapper.getIntvAgreedDraftOrderCollection())
            .ifPresent(agreedDraftOrderCollections ->
                processApprovableCollection(agreedDraftOrderCollections.stream().map(AgreedDraftOrderCollection::getValue).toList(), targetDoc,
                    judgeApproval, userAuthorisation));
    }

    private void processHearingInstruction(DraftOrdersWrapper draftOrdersWrapper) {
        ofNullable(draftOrdersWrapper.getHearingInstruction())
            .filter(hearingInstruction -> hearingInstruction.getRequireAnotherHearing() == YesOrNo.YES)
            .map(HearingInstruction::getAnotherHearingRequestCollection)
            .ifPresent(collection -> collection.forEach(a -> hearingProcessor.processHearingInstruction(draftOrdersWrapper, a.getValue())));
    }

    private boolean isFinalOrderSelected(JudgeApproval judgeApproval) {
        return ofNullable(judgeApproval.getIsFinalOrder())
            .map(DynamicMultiSelectList::getValue).stream().flatMap(Collection::stream)
            .anyMatch(d -> YesOrNo.isYes(d.getCode()));
    }
}
