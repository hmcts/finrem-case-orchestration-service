package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval;

import lombok.Getter;

@Getter
public enum JudgeDecision {

    REVIEW_LATER(false, false, false, false),
    LEGAL_REP_NEEDS_TO_MAKE_CHANGE(false, false, true, true),
    JUDGE_NEEDS_TO_MAKE_CHANGES(true, true, false, true),
    READY_TO_BE_SEALED(true, true, false, true);
    private final boolean hearingInstructionRequired;
    private final boolean approved;
    private final boolean refused;
    private final boolean extraReportFieldsInputRequired;

    JudgeDecision(boolean hearingInstructionRequired, boolean approved, boolean refused, boolean extraReportFieldsInputRequired) {
        this.hearingInstructionRequired = hearingInstructionRequired;
        this.approved = approved;
        this.refused = refused;
        this.extraReportFieldsInputRequired = extraReportFieldsInputRequired;
    }

}
