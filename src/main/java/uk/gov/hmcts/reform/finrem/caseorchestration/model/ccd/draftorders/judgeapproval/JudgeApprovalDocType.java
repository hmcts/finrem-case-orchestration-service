package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_judgeApprovalDocType", generate = true)
@Getter
public enum JudgeApprovalDocType {

    @CCD(label = "Draft Order")
    DRAFT_ORDER("Draft Order", "draft order"),

    @CCD(label = "Pension Sharing Annex")
    PSA("Pension Sharing Annex", "PSA");

    private final String title;

    private final String description;

    JudgeApprovalDocType(String title, String description) {
        this.title = title;
        this.description = description;
    }

}
