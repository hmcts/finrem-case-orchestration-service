package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import java.util.List;

public interface UploadedApprovedOrder {

    CaseDocument getApprovedOrder();

    List<DocumentCollectionItem> getAdditionalDocuments();
}
