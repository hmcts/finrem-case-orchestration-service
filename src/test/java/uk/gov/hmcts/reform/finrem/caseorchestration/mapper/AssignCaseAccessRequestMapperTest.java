package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.AssignCaseAccessRequest;

import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_USER_ID;

public class AssignCaseAccessRequestMapperTest extends BaseServiceTest {

    @Autowired private AssignCaseAccessRequestMapper assignCaseAccessRequestMapper;

    @Test
    public void mapToAssignCaseAccessRequest() {
        CaseDetails caseDetails = buildCaseDetails();

        AssignCaseAccessRequest assignCaseAccessRequest = assignCaseAccessRequestMapper.mapToAssignCaseAccessRequest(caseDetails, TEST_USER_ID);

        Assert.assertEquals(assignCaseAccessRequest.getAssignee_id(), TEST_USER_ID);
        Assert.assertEquals(assignCaseAccessRequest.getCase_id(), caseDetails.getId().toString());
        Assert.assertEquals(assignCaseAccessRequest.getCase_type_id(), caseDetails.getCaseTypeId());
    }
}