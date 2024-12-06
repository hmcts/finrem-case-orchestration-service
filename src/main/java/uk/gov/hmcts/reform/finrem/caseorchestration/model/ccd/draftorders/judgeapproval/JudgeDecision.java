package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval;

import lombok.Getter;

@Getter
public enum JudgeDecision {

    REVIEW_LATER(false),
    LEGAL_REP_NEEDS_TO_MAKE_CHANGE(false),
    JUDGE_NEEDS_TO_MAKE_CHANGES(true),
    READY_TO_BE_SEALED(true);
    private final boolean hearingInstructionRequired;

    JudgeDecision(boolean hearingInstructionRequired) {
        this.hearingInstructionRequired = hearingInstructionRequired;
    }

}
