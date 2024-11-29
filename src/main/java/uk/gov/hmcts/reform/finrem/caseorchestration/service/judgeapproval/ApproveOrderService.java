package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.utils.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Approvable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingInstructionProcessable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.AnotherHearingRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.HearingInstruction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApprovalDocType.DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApprovalDocType.PSA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.JUDGE_NEEDS_TO_MAKE_CHANGES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.READY_TO_BE_SEALED;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApproveOrderService {

    protected static final String SEPARATOR = "#";

    private final IdamService idamService;

    /**
     * Populates judge decisions for draft orders by iterating through a predefined range of indexes (1 to 5),
     * resolving judge approvals, and updating the corresponding draft orders and PSA documents statuses and hearing instructions.
     *
     * <p>For each index, the method:
     * <ol>
     *   <li>Resolves the {@link JudgeApproval} object for the specified index.</li>
     *   <li>Validates the judge approval and ensures it contains a valid document.</li>
     *   <li>Updates the draft orders and PSA documents in the provided {@link DraftOrdersWrapper} based on the judge's decision.</li>
     * </ol>
     *
     * @param draftOrdersWrapper the wrapper object containing draft orders and PSA document collections to be updated
     * @param userAuthorisation  the authorisation token of the user, used to fetch the approving judge's details
     * @throws IllegalArgumentException if a judge approval is found with a null document at a given index
     */
    public void populateJudgeDecisions(DraftOrdersWrapper draftOrdersWrapper, String userAuthorisation) {
        for (int i = 1; i <= 5; i++) {
            JudgeApproval judgeApproval = resolveJudgeApproval(draftOrdersWrapper, i);
            if (isJudgeApproved(judgeApproval)) {
                CaseDocument targetDoc = validateJudgeApprovalDocument(judgeApproval, i);
                populateJudgeDecision(draftOrdersWrapper, targetDoc, judgeApproval, userAuthorisation);
            }
        }
    }

    protected void populateJudgeDecision(DraftOrdersWrapper draftOrdersWrapper, CaseDocument targetDoc, JudgeApproval judgeApproval,
                                         String userAuthorisation) {
        ofNullable(draftOrdersWrapper.getDraftOrdersReviewCollection())
            .ifPresent(collection -> collection.forEach(el -> {
                if (el.getValue() != null) {
                    ofNullable(el.getValue().getDraftOrderDocReviewCollection())
                        .ifPresent(draftOrderDocReviewCollection ->
                            processApprovableCollection(draftOrderDocReviewCollection.stream().map(DraftOrderDocReviewCollection::getValue).toList(),
                                targetDoc, judgeApproval, userAuthorisation));

                    ofNullable(el.getValue().getPsaDocReviewCollection())
                        .ifPresent(psaDocReviewCollection ->
                            processApprovableCollection(psaDocReviewCollection.stream().map(PsaDocReviewCollection::getValue).toList(), targetDoc,
                                judgeApproval, userAuthorisation));

                }
            }));

        ofNullable(draftOrdersWrapper.getAgreedDraftOrderCollection())
            .ifPresent(agreedDraftOrderCollections ->
                processApprovableCollection(agreedDraftOrderCollections.stream().map(AgreedDraftOrderCollection::getValue).toList(), targetDoc,
                    judgeApproval, userAuthorisation));

        ofNullable(draftOrdersWrapper.getHearingInstruction())
            .map(HearingInstruction::getAnotherHearingRequestCollection)
            .ifPresent(collection -> collection.forEach(a -> processHearingInstruction(draftOrdersWrapper, a.getValue())));
    }

    protected boolean isJudgeApproved(JudgeApproval judgeApproval) {
        return judgeApproval != null && Arrays.asList(READY_TO_BE_SEALED, JUDGE_NEEDS_TO_MAKE_CHANGES).contains(judgeApproval.getJudgeDecision());
    }

    protected CaseDocument validateJudgeApprovalDocument(JudgeApproval judgeApproval, int index) {
        CaseDocument doc = judgeApproval.getDocument();
        if (doc == null) {
            throw new IllegalArgumentException(format("Document is null for JudgeApproval at index %d. Please check the data integrity.", index));
        }
        return doc;
    }

    protected void processApprovableCollection(List<? extends Approvable> approvables, CaseDocument targetDoc, JudgeApproval judgeApproval,
                                             String userAuthorisation) {
        ofNullable(approvables)
            .ifPresent(list ->
                list.forEach(el -> ofNullable(el)
                    .filter(approvable -> approvable.match(targetDoc))
                    .ifPresent(approvable -> handleApprovable(approvable, judgeApproval, userAuthorisation))
                )
            );
    }

    protected void handleApprovable(Approvable approvable, JudgeApproval judgeApproval, String userAuthorisation) {
        if (judgeApproval.getJudgeDecision() == JUDGE_NEEDS_TO_MAKE_CHANGES) {
            approvable.replaceDocument(judgeApproval.getAmendedDocument());
        }
        approvable.setOrderStatus(OrderStatus.APPROVED_BY_JUDGE);
        approvable.setApprovalDate(LocalDate.now());
        approvable.setApprovalJudge(idamService.getIdamFullName(userAuthorisation));
    }

    protected void processHearingInstruction(DraftOrdersWrapper draftOrdersWrapper, AnotherHearingRequest anotherHearingRequest) {
        String[] splitResult = ofNullable(anotherHearingRequest)
            .map(AnotherHearingRequest::getWhichOrder)
            .map(DynamicList::getValueCode)
            .map(valueCode -> valueCode.split(SEPARATOR))
            .orElseThrow(() -> new IllegalStateException("Missing selected value in AnotherHearingRequest.whichOrder"));
        if (splitResult.length != 2) {
            String valueCode = Optional.of(anotherHearingRequest)
                .map(AnotherHearingRequest::getWhichOrder)
                .map(DynamicList::getValueCode)
                .orElse(null);
            throw new IllegalStateException(format("Unexpected selected value in AnotherHearingRequest.whichOrder: %s", valueCode));
        }

        String orderIndex = splitResult[1];

        JudgeApproval judgeApproval = null;
        try {
            judgeApproval = (JudgeApproval) draftOrdersWrapper.getClass().getMethod("getJudgeApproval" + (orderIndex))
                .invoke(draftOrdersWrapper);
        } catch (Exception e) {
            throw new IllegalStateException(format("Unexpected method \"getJudgeApproval%s\" was invoked", orderIndex), e);
        }
        ofNullable(judgeApproval)
            .map(JudgeApproval::getDocument).ifPresent(targetDoc -> ofNullable(draftOrdersWrapper.getDraftOrdersReviewCollection())
                .ifPresent(collection -> collection.forEach(el -> {
                    if (el.getValue() != null) {
                        ofNullable(el.getValue().getDraftOrderDocReviewCollection())
                            .ifPresent(draftOrderDocReviewCollection ->
                                processHearingInstruction(draftOrderDocReviewCollection.stream()
                                        .map(DraftOrderDocReviewCollection::getValue).toList(), targetDoc, anotherHearingRequest)
                            );

                        ofNullable(el.getValue().getPsaDocReviewCollection())
                            .ifPresent(psaDocReviewCollection ->
                                processHearingInstruction(psaDocReviewCollection.stream()
                                        .map(PsaDocReviewCollection::getValue).toList(), targetDoc, anotherHearingRequest)
                            );
                    }
                })));
    }

    protected void processHearingInstruction(List<? extends HearingInstructionProcessable> hip,
                                           CaseDocument targetDoc,
                                           AnotherHearingRequest anotherHearingRequest) {
        ofNullable(hip)
            .ifPresent(list -> list.forEach(el -> {
                if (el.match(targetDoc)) {
                    el.setAnotherHearingToBeListed(YesOrNo.YES);
                    el.setHearingType(anotherHearingRequest.getTypeOfHearing().name());
                    el.setAdditionalTime(anotherHearingRequest.getAdditionalTime());
                    el.setHearingTimeEstimate(anotherHearingRequest.getTimeEstimate().getValue());
                    el.setOtherListingInstructions(anotherHearingRequest.getAnyOtherListingInstructions());
                }
            }));
    }

    /**
     * Builds a {@link DynamicList} representing the orders available for selection
     * based on the given {@link DraftOrdersWrapper}. Each order is represented as a
     * {@link DynamicListElement} containing a code and label.
     *
     * <p>The method processes up to 5 judge approvals, where each approval contributes a list item
     * if the approval is non-null and the associated document filename is not empty.</p>
     *
     * <p>For each {@link JudgeApproval}:</p>
     * <ul>
     *     <li>The code is prefixed with "draftOrder" or "psa", depending on the document type.</li>
     *     <li>The filename is used as the label of the list item.</li>
     * </ul>
     *
     * <p>Example:</p>
     * <pre>
     * Code: draftOrder_1, Label: OrderDocument1.pdf
     * Code: psa_2, Label: PensionSharingAnnex1.pdf
     * </pre>
     *
     * @param draftOrdersWrapper the {@link DraftOrdersWrapper} containing the judge approvals.
     * @return a {@link DynamicList} populated with items representing the orders.
     */
    public DynamicList buildWhichOrderDynamicList(DraftOrdersWrapper draftOrdersWrapper) {
        List<DynamicListElement> listItems = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            JudgeApproval judgeApproval = resolveJudgeApproval(draftOrdersWrapper, i);
            if (judgeApproval != null) {
                String codePrefix = DRAFT_ORDER == judgeApproval.getDocType() ? DRAFT_ORDER.name() : PSA.name();
                String code = codePrefix + SEPARATOR + i;

                String filename = getDocumentFileName(judgeApproval);
                if (!StringUtils.isEmpty(filename)) {
                    listItems.add(DynamicListElement.builder().code(code).label(filename).build());
                }
            }
        }

        return DynamicList.builder().listItems(listItems).build();
    }

    protected static String getDocumentFileName(JudgeApproval judgeApproval) {
        String filename = null;
        if (JUDGE_NEEDS_TO_MAKE_CHANGES == judgeApproval.getJudgeDecision()) {
            filename = judgeApproval.getAmendedDocument() != null
                ? judgeApproval.getAmendedDocument().getDocumentFilename()
                : "Unknown Filename";
        } else if (READY_TO_BE_SEALED == judgeApproval.getJudgeDecision()) {
            filename = judgeApproval.getDocument() != null
                ? judgeApproval.getDocument().getDocumentFilename()
                : "Unknown Filename";
        }
        return filename;
    }

    /**
     * Resolves the {@link JudgeApproval} object from the provided {@link DraftOrdersWrapper}
     * based on the specified index. Each index corresponds to a specific judge approval field
     * in the wrapper. If the index is out of the supported range (1 to 5), the method returns null.
     *
     * <p>Mapping of index to fields:</p>
     * <ul>
     *     <li>1 - {@link DraftOrdersWrapper#getJudgeApproval1()}</li>
     *     <li>2 - {@link DraftOrdersWrapper#getJudgeApproval2()}</li>
     *     <li>3 - {@link DraftOrdersWrapper#getJudgeApproval3()}</li>
     *     <li>4 - {@link DraftOrdersWrapper#getJudgeApproval4()}</li>
     *     <li>5 - {@link DraftOrdersWrapper#getJudgeApproval5()}</li>
     * </ul>
     *
     * @param draftOrdersWrapper the {@link DraftOrdersWrapper} containing judge approval fields.
     * @param index the index specifying which judge approval field to retrieve (1-5).
     * @return the corresponding {@link JudgeApproval} object, or null if the index is out of range.
     */
    public JudgeApproval resolveJudgeApproval(DraftOrdersWrapper draftOrdersWrapper, int index) {
        return switch (index) {
            case 1 -> draftOrdersWrapper.getJudgeApproval1();
            case 2 -> draftOrdersWrapper.getJudgeApproval2();
            case 3 -> draftOrdersWrapper.getJudgeApproval3();
            case 4 -> draftOrdersWrapper.getJudgeApproval4();
            case 5 -> draftOrdersWrapper.getJudgeApproval5();
            default -> null;
        };
    }

}
