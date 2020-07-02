package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Features {

    CONSENT_ORDER_NOT_APPROVED_APPLICANT_DOCUMENT_GENERATION("consent_order_not_approved_applicant_document_generation");

    private final String name;
}
