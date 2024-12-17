package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders;

import java.time.LocalDateTime;

public interface HasSubmittedInfo {
    void setSubmittedDate(LocalDateTime localDate);

    void setSubmittedBy(String submittedBy);

    void setSubmittedByEmail(String submittedByEmail);

    String getUploadedOnBehalfOf();
}
