package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval;

import lombok.Getter;

@Getter
public enum JudgeApprovalDocType {

    DRAFT_ORDER("Draft Order", "draft order"),

    PSA("Pension Sharing Annex", "PSA");

    private final String title;

    private final String description;

    JudgeApprovalDocType(String title, String description) {
        this.title = title;
        this.description = description;
    }

}
