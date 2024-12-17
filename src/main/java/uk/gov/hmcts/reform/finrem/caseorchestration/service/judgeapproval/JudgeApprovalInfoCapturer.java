package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class JudgeApprovalInfoCapturer {

    public static void fileNameCaptors(JudgeApproval judgeApproval, DraftOrdersWrapper draftOrdersWrapper){
        String fileName = judgeApproval.getDocument().getDocumentFilename();

        switch (judgeApproval.getJudgeDecision()) {
            case READY_TO_BE_SEALED -> draftOrdersWrapper.getOrdersApproved().add(fileName);
            case LEGAL_REP_NEEDS_TO_MAKE_CHANGE -> draftOrdersWrapper.getOrdersRepresentativeChanges().add(fileName);
            case JUDGE_NEEDS_TO_MAKE_CHANGES -> draftOrdersWrapper.getOrdersChanged().add(fileName);
            case REVIEW_LATER -> draftOrdersWrapper.getOrdersReviewLater().add(fileName);
            default -> throw new IllegalStateException("Unhandled judge decision for document:" + fileName);
        }
    }

}
