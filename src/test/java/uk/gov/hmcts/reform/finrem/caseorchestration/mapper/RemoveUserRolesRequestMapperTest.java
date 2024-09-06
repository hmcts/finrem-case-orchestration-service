package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.RemoveUserRolesRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_CASE_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_USER_ID;

public class RemoveUserRolesRequestMapperTest extends BaseServiceTest {

    @Autowired
    private RemoveUserRolesRequestMapper removeUserRolesRequestMapper;

    @Test
    public void mapToAssignCaseAccessRequest() {
        CaseDetails caseDetails = buildCaseDetails();

        RemoveUserRolesRequest assignCaseAccessRequest =
            removeUserRolesRequestMapper.mapToRemoveUserRolesRequest(caseDetails, TEST_USER_ID, TEST_CASE_ROLE);

        assertEquals(TEST_USER_ID, assignCaseAccessRequest.getCase_users().get(0).getUserId());
        assertEquals(TEST_CASE_ROLE, assignCaseAccessRequest.getCase_users().get(0).getCaseRole());
        assertEquals(assignCaseAccessRequest.getCase_users().get(0).getCaseId(), caseDetails.getId().toString());
    }

    @Test
    public void mapToFinremCaseDetailsAssignCaseAccessRequest() {
        FinremCaseDetails caseDetails = buildFinremCaseDetails();

        RemoveUserRolesRequest assignCaseAccessRequest =
            removeUserRolesRequestMapper.mapToRemoveUserRolesRequest(caseDetails, TEST_USER_ID, TEST_CASE_ROLE);

        assertEquals(TEST_USER_ID, assignCaseAccessRequest.getCase_users().get(0).getUserId());
        assertEquals(TEST_CASE_ROLE, assignCaseAccessRequest.getCase_users().get(0).getCaseRole());
        assertEquals(assignCaseAccessRequest.getCase_users().get(0).getCaseId(), caseDetails.getId().toString());
    }

    @Test
    public void mapToCaseIdRequest() {
        String caseId = "123";
        RemoveUserRolesRequest assignCaseAccessRequest =
            removeUserRolesRequestMapper.mapToRemoveUserRolesRequest(caseId, TEST_USER_ID, TEST_CASE_ROLE);
        assertEquals(TEST_USER_ID, assignCaseAccessRequest.getCase_users().get(0).getUserId());
        assertEquals(TEST_CASE_ROLE, assignCaseAccessRequest.getCase_users().get(0).getCaseRole());
        assertEquals(caseId, assignCaseAccessRequest.getCase_users().get(0).getCaseId());
    }
}
