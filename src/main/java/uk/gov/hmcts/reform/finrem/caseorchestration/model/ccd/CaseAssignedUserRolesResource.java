package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class CaseAssignedUserRolesResource {

    @JsonProperty("case_users")
    private List<CaseAssignedUserRole> caseAssignedUserRoles;
}