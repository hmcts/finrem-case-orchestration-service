package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

public interface UploadingDocumentsHolder<T extends HasUploadingDocuments> {
    T getValue();
}
