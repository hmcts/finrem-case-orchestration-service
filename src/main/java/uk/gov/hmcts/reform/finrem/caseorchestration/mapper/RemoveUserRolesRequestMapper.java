package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseUsers;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.RemoveUserRolesRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RemoveUserRolesRequestMapper {
    public RemoveUserRolesRequest mapToRemoveUserRolesRequest(CaseDetails caseDetails, String userId, String caseRole) {
        return mapToRemoveUserRolesRequest(String.valueOf(caseDetails.getId()), userId, caseRole);
    }

    public RemoveUserRolesRequest mapToRemoveUserRolesRequest(FinremCaseDetails caseDetails, String userId, String caseRole) {
        return mapToRemoveUserRolesRequest(String.valueOf(caseDetails.getId()), userId, caseRole);
    }

    public RemoveUserRolesRequest mapToRemoveUserRolesRequest(String caseId, String userId, String caseRole) {
        return RemoveUserRolesRequest
            .builder()
            .case_users(
                List.of(CaseUsers.builder()
                    .caseId(caseId)
                    .userId(userId)
                    .caseRole(caseRole)
                    .build()))
            .build();
    }
}
