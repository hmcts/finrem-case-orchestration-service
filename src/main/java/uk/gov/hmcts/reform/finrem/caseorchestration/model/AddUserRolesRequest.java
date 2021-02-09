package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class AddUserRolesRequest {
    private List<CaseAssignedUserRoleWithOrganisation> case_users;
}