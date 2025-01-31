package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import java.util.List;

public interface HasUploadingDocument {

    List<CaseDocument> getUploadingDocuments();
}
