package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import java.time.LocalDateTime;

public interface CaseDocumentTabData {
    String getElementId();

    void setUploadDateTime(LocalDateTime date);
}
