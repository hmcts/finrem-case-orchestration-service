package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;

import static java.lang.String.format;

@Service
@Slf4j
public class ApproveOrderService {

    private final JudgeApprovalResolver judgeApprovalResolver;

    public ApproveOrderService(JudgeApprovalResolver judgeApprovalResolver) {
        this.judgeApprovalResolver = judgeApprovalResolver;
    }

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
            CaseDocument targetDoc = validateJudgeApprovalDocument(judgeApproval, i);
            judgeApprovalResolver.populateJudgeDecision(draftOrdersWrapper, targetDoc, judgeApproval, userAuthorisation);
        }
    }

    private CaseDocument validateJudgeApprovalDocument(JudgeApproval judgeApproval, int index) {
        CaseDocument doc = judgeApproval.getDocument();
        if (doc == null) {
            throw new IllegalArgumentException(format("Document is null for JudgeApproval at index %d. Please check the data integrity.", index));
        }
        return doc;
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
