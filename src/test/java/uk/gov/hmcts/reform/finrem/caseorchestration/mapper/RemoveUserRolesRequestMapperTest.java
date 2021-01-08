package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.RemoveUserRolesRequest;

import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_CASE_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_USER_ID;

public class RemoveUserRolesRequestMapperTest extends BaseServiceTest {

    @Autowired private RemoveUserRolesRequestMapper removeUserRolesRequestMapper;

    @Test
    public void mapToAssignCaseAccessRequest() {
        CaseDetails caseDetails = buildCaseDetails();

        RemoveUserRolesRequest assignCaseAccessRequest =
            removeUserRolesRequestMapper.mapToRemoveUserRolesRequest(caseDetails, TEST_USER_ID, TEST_CASE_ROLE);

        Assert.assertEquals(assignCaseAccessRequest.getCase_users().get(0).getUser_id(), TEST_USER_ID);
        Assert.assertEquals(assignCaseAccessRequest.getCase_users().get(0).getCase_role(), TEST_CASE_ROLE);
        Assert.assertEquals(assignCaseAccessRequest.getCase_users().get(0).getCase_id(), caseDetails.getId().toString());
    }
}