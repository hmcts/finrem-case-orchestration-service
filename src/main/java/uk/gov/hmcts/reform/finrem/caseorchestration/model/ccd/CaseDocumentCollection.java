package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

public interface CaseDocumentCollection<T extends HasDocumentLink> {
    T getValue();
}
