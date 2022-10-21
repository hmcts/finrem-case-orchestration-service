package uk.gov.hmcts.reform.finrem.caseorchestration.error;

import lombok.EqualsAndHashCode;

import java.util.Set;

@EqualsAndHashCode(callSuper = false)
public class GrantCaseAccessException extends RuntimeException {

    private final Long caseId;
    private final Set<String> userIds;
    private final String caseRole;

    public GrantCaseAccessException(Long caseId, Set<String> userIds, String caseRole) {
        super(String.format("User(s) %s not granted %s to case %s", userIds, caseRole, caseId));
        this.caseId = caseId;
        this.userIds = userIds;
        this.caseRole = caseRole;
    }
}
