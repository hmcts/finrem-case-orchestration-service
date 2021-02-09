package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CaseAssignedUserRoleWithOrganisation {
    private String organisation_id;
    private String case_id;
    private String user_id;
    private String case_role;
}
