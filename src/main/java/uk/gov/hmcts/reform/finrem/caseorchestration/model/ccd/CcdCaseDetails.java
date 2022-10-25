package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

public interface CcdCaseDetails<D> {

    CaseType getCaseType();

    Long getId();

    D getData();
}
