package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderStatus {

    APPROVE,
    JUDGE_AMEND_DRAFT,
    JUDGE_REQUESTED_CHANGES,
    REVIEW_LATER
}

