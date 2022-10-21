package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

public interface CcdCaseDetails<D> {

    CaseType getCaseType();

    Long getId();

    D getData();
}
