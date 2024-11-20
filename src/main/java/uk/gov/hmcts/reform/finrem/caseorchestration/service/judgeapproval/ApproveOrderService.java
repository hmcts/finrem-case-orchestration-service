package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeReviewable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.ReviewableDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.ReviewablePsa;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.JUDGE_NEEDS_TO_MAKE_CHANGES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.READY_TO_BE_SEALED;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApproveOrderService {

    public DynamicList buildWhichOrderDynamicList(JudgeApproval judgeApproval) {
        List<DynamicListElement> listItems = new ArrayList<>();

        processReviewableItems(judgeApproval, "draftOrder_", this::getReviewableDraftOrder, listItems);
        processReviewableItems(judgeApproval, "psa_", this::getReviewablePsa, listItems);

        return DynamicList.builder().listItems(listItems).build();
    }

    private <T extends JudgeReviewable> void processReviewableItems(JudgeApproval judgeApproval,
                                                                    String codePrefix,
                                                                    BiFunction<JudgeApproval, Integer, T> fetchMethod,
                                                                    List<DynamicListElement> listItems) {
        for (int i = 1; i <= 5; i++) {
            int finalI = i;
            Optional.ofNullable(fetchMethod.apply(judgeApproval, i))
                .map(this::getFilenameForDecision)
                .ifPresent(filename -> listItems.add(DynamicListElement.builder()
                    .code(codePrefix + finalI)
                    .label(filename)
                    .build()));
        }
    }

    private String getFilenameForDecision(JudgeReviewable reviewable) {
        if (READY_TO_BE_SEALED == reviewable.getJudgeDecision()) {
            return Optional.ofNullable(reviewable.getDocument())
                .map(CaseDocument::getDocumentFilename)
                .orElse(null);
        } else if (JUDGE_NEEDS_TO_MAKE_CHANGES == reviewable.getJudgeDecision()) {
            return Optional.ofNullable(reviewable.getAmendedDocument())
                .map(CaseDocument::getDocumentFilename)
                .orElse(null);
        }
        return null;
    }

    private ReviewableDraftOrder getReviewableDraftOrder(JudgeApproval judgeApproval, int index) {
        return invokeReviewable(judgeApproval, "getReviewableDraftOrder", index, ReviewableDraftOrder.class);
    }

    private ReviewablePsa getReviewablePsa(JudgeApproval judgeApproval, int index) {
        return invokeReviewable(judgeApproval, "getReviewablePsa", index, ReviewablePsa.class);
    }

    private <T> T invokeReviewable(JudgeApproval judgeApproval, String methodName, int index, Class<T> type) {
        try {
            return type.cast(judgeApproval.getClass()
                .getMethod(methodName + index)
                .invoke(judgeApproval));
        } catch (Exception e) {
            log.error("Unexpected error invoking method: " + methodName + index, e);
            return null;
        }
    }

}
