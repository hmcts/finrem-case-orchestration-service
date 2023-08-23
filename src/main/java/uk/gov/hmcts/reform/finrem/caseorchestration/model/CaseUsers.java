package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CaseUsers {
    @JsonProperty("case_id")
    private String caseId;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("case_role")
    private String caseRole;
}
