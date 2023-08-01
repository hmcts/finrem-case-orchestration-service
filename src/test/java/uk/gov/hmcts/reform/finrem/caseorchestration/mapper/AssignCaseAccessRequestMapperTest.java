package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.AssignCaseAccessRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_USER_ID;

public class AssignCaseAccessRequestMapperTest extends BaseServiceTest {

    @Autowired
    private AssignCaseAccessRequestMapper assignCaseAccessRequestMapper;

    @Test
    public void mapToAssignCaseAccessRequest() {
        CaseDetails caseDetails = buildCaseDetails();

        AssignCaseAccessRequest assignCaseAccessRequest = assignCaseAccessRequestMapper.mapToAssignCaseAccessRequest(caseDetails, TEST_USER_ID);

        Assert.assertEquals(TEST_USER_ID, assignCaseAccessRequest.getAssignee_id());
        Assert.assertEquals(assignCaseAccessRequest.getCase_id(), caseDetails.getId().toString());
        Assert.assertEquals(assignCaseAccessRequest.getCase_type_id(), caseDetails.getCaseTypeId());
    }

    @Test
    public void mapToAssignCaseAccessRequestFinremCaseDetails() {
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder()
            .caseType(CaseType.CONTESTED)
            .id(123L)
            .build();


        AssignCaseAccessRequest assignCaseAccessRequest = assignCaseAccessRequestMapper.mapToAssignCaseAccessRequest(finremCaseDetails, TEST_USER_ID);

        Assert.assertEquals(TEST_USER_ID, assignCaseAccessRequest.getAssignee_id());
        Assert.assertEquals(assignCaseAccessRequest.getCase_id(), finremCaseDetails.getId().toString());
        Assert.assertEquals(assignCaseAccessRequest.getCase_type_id(), finremCaseDetails.getCaseType().toString());
    }
}