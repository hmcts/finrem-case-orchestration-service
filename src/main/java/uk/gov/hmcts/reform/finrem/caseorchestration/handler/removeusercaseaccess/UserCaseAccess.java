package uk.gov.hmcts.reform.finrem.caseorchestration.handler.removeusercaseaccess;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

class UserCaseAccess {
    private final CaseAssignedUserRole caseAssignedUserRole;
    private final UserDetails userDetails;

    static UserCaseAccess of(CaseAssignedUserRole caseAssignedUserRole, UserDetails userDetails) {
        return new UserCaseAccess(caseAssignedUserRole, userDetails);
    }

    private UserCaseAccess(CaseAssignedUserRole caseAssignedUserRole, UserDetails userDetails) {
        this.caseAssignedUserRole = caseAssignedUserRole;
        this.userDetails = userDetails;
    }

    String getCaseRole() {
        return caseAssignedUserRole.getCaseRole();
    }

    String getUserFullName() {
        return userDetails.getFullName();
    }

    String getUserEmail() {
        return userDetails.getEmail();
    }

    String getUserId() {
        return userDetails.getId();
    }
}
