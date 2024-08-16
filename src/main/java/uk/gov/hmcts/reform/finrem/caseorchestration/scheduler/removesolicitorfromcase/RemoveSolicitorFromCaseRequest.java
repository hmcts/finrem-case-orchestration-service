package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler.removesolicitorfromcase;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"caseReference", "caseType", "userId", "role", "updateReference"})
record RemoveSolicitorFromCaseRequest(String caseReference, String caseType, String userId, String role,
                                      String updateReference) {
}
