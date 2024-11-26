package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.utils.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApprovalDocType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApproveOrderService {

    private final IdamService idamService;

    public void populateJudgeDecisions(DraftOrdersWrapper draftOrdersWrapper, String userAuthorisation) {
        for (int i = 1; i <= 5; i++) {
            JudgeApproval judgeApproval = resolveJudgeApproval(draftOrdersWrapper, i);
            if (judgeApproval != null && judgeApproval.getJudgeDecision() == JudgeDecision.READY_TO_BE_SEALED) {
                CaseDocument doc = judgeApproval.getDocument();
                if (doc == null) {
                    throw new IllegalArgumentException(format("Document is null for JudgeApproval at index %d with decision READY_TO_BE_SEALED. "
                        + "Please check the data integrity.", i));
                }
                draftOrdersWrapper.getDraftOrdersReviewCollection().forEach(d -> {
                    d.getValue().getDraftOrderDocReviewCollection().forEach(d1 -> {
                        if (doc.equals(d1.getValue().getDraftOrderDocument())) {
                            d1.getValue().setOrderStatus(OrderStatus.APPROVED_BY_JUDGE);
                            d1.getValue().setApprovalDate(LocalDate.now());
                            d1.getValue().setApprovalJudge(idamService.getIdamFullName(userAuthorisation));
                        }
                    });
                    d.getValue().getPsaDocReviewCollection().forEach(d1 -> {
                        if (doc.equals(d1.getValue().getPsaDocument())) {
                            d1.getValue().setOrderStatus(OrderStatus.APPROVED_BY_JUDGE);
                            d1.getValue().setApprovalDate(LocalDate.now());
                            d1.getValue().setApprovalJudge(idamService.getIdamFullName(userAuthorisation));
                        }
                    });
                });
            }
        }
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
                String codePrefix = JudgeApprovalDocType.DRAFT_ORDER == judgeApproval.getDocType() ? "draftOrder" : "psa";
                String code = codePrefix + "_" + i;

                String filename = getDocumentFileName(judgeApproval);
                if (!StringUtils.isEmpty(filename)) {
                    listItems.add(DynamicListElement.builder().code(code).label(filename).build());
                }
            }
        }

        return DynamicList.builder().listItems(listItems).build();
    }

    private static String getDocumentFileName(JudgeApproval judgeApproval) {
        String filename = null;
        if (JudgeDecision.JUDGE_NEEDS_TO_MAKE_CHANGES == judgeApproval.getJudgeDecision()) {
            filename = judgeApproval.getAmendedDocument() != null
                ? judgeApproval.getAmendedDocument().getDocumentFilename()
                : "Unknown Filename";
        } else if (JudgeDecision.READY_TO_BE_SEALED == judgeApproval.getJudgeDecision()) {
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
