package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@ToString
@Getter
@Builder(toBuilder = true)
@Jacksonized
public class CaseAssignmentUserRole {

    @JsonProperty("case_id")
    private String caseDataId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("case_role")
    private String caseRole;
}
