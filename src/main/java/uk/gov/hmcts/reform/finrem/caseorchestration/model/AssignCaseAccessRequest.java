package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AssignCaseAccessRequest {
    @JsonProperty("case_id")
    private String caseId;

    @JsonProperty("assignee_id")
    private String assigneeId;

    @JsonProperty("case_type_id")
    private String caseTypeId;
}
