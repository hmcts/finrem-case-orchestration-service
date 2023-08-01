package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseUsers;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.RemoveUserRolesRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.Arrays;

@Service
@Slf4j
@RequiredArgsConstructor
public class RemoveUserRolesRequestMapper {
    public RemoveUserRolesRequest mapToRemoveUserRolesRequest(CaseDetails caseDetails, String userId, String caseRole) {
        return RemoveUserRolesRequest
            .builder()
            .case_users(
                Arrays.asList(CaseUsers.builder()
                    .caseId(caseDetails.getId().toString())
                    .userId(userId)
                    .caseRole(caseRole)
                    .build()))
            .build();
    }

    public RemoveUserRolesRequest mapToRemoveUserRolesRequest(FinremCaseDetails caseDetails, String userId, String caseRole) {
        return RemoveUserRolesRequest
            .builder()
            .case_users(
                Arrays.asList(CaseUsers.builder()
                    .caseId(caseDetails.getId().toString())
                    .userId(userId)
                    .caseRole(caseRole)
                    .build()))
            .build();
    }
}
