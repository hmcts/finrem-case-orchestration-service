package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseUsers;

import java.util.Arrays;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaseAssignedUserRolesRequestMapper {
    public CaseAssignedUserRolesRequest mapToCaseAssignedUserRolesRequest(
        CaseDetails caseDetails, String userId, String caseRole, String organisationId) {
        return CaseAssignedUserRolesRequest
            .builder()
            .case_users(
                Arrays.asList(CaseUsers.builder()
                    .case_id(caseDetails.getId().toString())
                    .user_id(userId)
                    .case_role(caseRole)
                    .organisation_id(organisationId)
                    .build()))
            .build();
    }
}
