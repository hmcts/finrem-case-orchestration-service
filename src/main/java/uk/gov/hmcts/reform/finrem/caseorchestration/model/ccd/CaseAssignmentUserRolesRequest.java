package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter
@Data
@Builder
@Jacksonized
public class CaseAssignmentUserRolesRequest {

    @JsonProperty("case_users")
    private List<CaseAssignmentUserRoleWithOrganisation> caseAssignmentUserRolesWithOrganisation;

}
