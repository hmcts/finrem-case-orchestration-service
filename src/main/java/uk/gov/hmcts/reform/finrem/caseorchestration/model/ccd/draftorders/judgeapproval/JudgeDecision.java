package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval;

import lombok.Getter;

@Getter
public enum JudgeDecision {

    REVIEW_LATER(false, false, false),
    LEGAL_REP_NEEDS_TO_MAKE_CHANGE(false, false, true),
    JUDGE_NEEDS_TO_MAKE_CHANGES(true, true, true),
    READY_TO_BE_SEALED(true, true, true);
    private final boolean hearingInstructionRequired;
    private final boolean approved;
    private final boolean refused;

    JudgeDecision(boolean hearingInstructionRequired, boolean approved, boolean refused) {
        this.hearingInstructionRequired = hearingInstructionRequired;
        this.approved = approved;
        this.refused = refused;
    }

}
