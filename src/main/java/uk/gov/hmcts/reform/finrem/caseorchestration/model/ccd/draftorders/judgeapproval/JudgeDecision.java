package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_judgeDecision", generate = true)
@Getter
public enum JudgeDecision {

    @CCD(label = "Review later")
    REVIEW_LATER(false, false, false, false),
    @CCD(label = "No, the legal representative needs to make changes")
    LEGAL_REP_NEEDS_TO_MAKE_CHANGE(false, false, true, true),
    @CCD(label = "No, I need to make changes")
    JUDGE_NEEDS_TO_MAKE_CHANGES(true, true, false, true),
    @CCD(label = "Yes")
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
