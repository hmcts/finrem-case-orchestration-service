package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

public interface JudgeReviewable {

    JudgeDecision getJudgeDecision();

    CaseDocument getDocument();

    CaseDocument getAmendedDocument();
}
