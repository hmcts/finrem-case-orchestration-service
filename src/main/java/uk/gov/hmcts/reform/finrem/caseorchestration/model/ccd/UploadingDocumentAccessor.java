package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

public interface UploadingDocumentAccessor<T extends HasUploadingDocuments> {
    T getValue();
}
