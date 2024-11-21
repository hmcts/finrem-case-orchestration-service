package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval;

import lombok.Getter;

@Getter
public enum JudgeApprovalDocType {

    DRAFT_ORDER("Draft Order"),

    PSA("Pension Sharing Annex");
    private final String title;

    JudgeApprovalDocType(String title) {
        this.title = title;
    }

}
