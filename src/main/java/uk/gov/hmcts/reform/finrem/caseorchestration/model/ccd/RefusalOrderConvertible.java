package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import java.time.LocalDateTime;

public interface RefusalOrderConvertible extends Approvable {

    String getApprovalJudge();

    CaseDocument getDraftOrderOrPsa();

    LocalDateTime getRefusedDate();

    String getSubmittedBy();

    String getSubmittedByEmail();

    LocalDateTime getSubmittedDate();

    void setRefusedDate(LocalDateTime refusedDate);

}
