package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.searchuserrole;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SearchCaseAssignedUserRolesRequest {

    @JsonProperty("case_ids")
    private List<String> caseIds;

    @JsonProperty("user_ids")
    private List<String> userIds;

}
