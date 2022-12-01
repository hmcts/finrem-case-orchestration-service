package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;

import java.util.List;
import java.util.Map;

public abstract class CaseDocumentManager<T> {

    public abstract void manageDocumentCollection(List<UploadCaseDocumentCollection> uploadedDocuments,
                                                  FinremCaseData caseData);
}
