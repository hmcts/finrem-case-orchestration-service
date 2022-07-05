package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadCaseDocumentCollection;

import java.util.List;

public abstract class CaseDocumentHandler<T> {

    public CaseDocumentHandler() {
    }

    protected abstract List<T> getDocumentCollection(FinremCaseData caseData);

    public abstract void handle(List<UploadCaseDocumentCollection> uploadedDocuments, FinremCaseData caseData);
}
