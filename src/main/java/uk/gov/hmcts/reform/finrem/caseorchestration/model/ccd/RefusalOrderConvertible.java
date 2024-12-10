package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import java.time.LocalDateTime;

/**
 * Interface defining the methods required to convert
 * a refusal order from draft orders or pension sharing annexes.
 */
public interface RefusalOrderConvertible extends Approvable {

    String getApprovalJudge();

    CaseDocument getDraftOrderOrPsa();

    LocalDateTime getRefusedDate();

    String getSubmittedBy();

    String getSubmittedByEmail();

    LocalDateTime getSubmittedDate();

    void setRefusedDate(LocalDateTime refusedDate);

}
