package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

public interface CcdCaseDetails<T extends FinremCaseData> {

    CaseType getCaseType();

    Long getId();

    T getData();
}
