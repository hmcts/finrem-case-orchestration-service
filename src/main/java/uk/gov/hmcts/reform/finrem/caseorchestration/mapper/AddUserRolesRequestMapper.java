package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.AddUserRolesRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseAssignedUserRoleWithOrganisation;

import java.util.Arrays;

@Service
@Slf4j
@RequiredArgsConstructor
public class AddUserRolesRequestMapper {
    public AddUserRolesRequest mapToAddUserRolesRequest(CaseDetails caseDetails, String userId, String caseRole, String orgId) {
        return AddUserRolesRequest
            .builder()
            .case_users(
                Arrays.asList(CaseAssignedUserRoleWithOrganisation.builder()
                    .case_id(caseDetails.getId().toString())
                    .user_id(userId)
                    .case_role(caseRole)
                    .organisation_id(orgId)
                    .build()))
            .build();
    }
}
