package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class RemoveUserRolesRequest {
    private List<CaseUsers> case_users;
}