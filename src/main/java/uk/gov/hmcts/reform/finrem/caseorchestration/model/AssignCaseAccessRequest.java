package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@SuppressWarnings("java:S116")
public class AssignCaseAccessRequest {
    private String case_id;
    private String assignee_id;
    private String case_type_id;
}
