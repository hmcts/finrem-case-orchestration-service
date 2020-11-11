package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.AssignCaseAccessServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.AssignCaseAccessRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.AssignCaseAccessRequest;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_USER_ID;

public class AssignCaseAccessServiceTest extends BaseServiceTest {

    @Autowired private AssignCaseAccessService assignCaseAccessService;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AssignCaseAccessServiceConfiguration assignCaseAccessServiceConfiguration;
    @MockBean private AssignCaseAccessRequestMapper assignCaseAccessRequestMapper;
    @MockBean private IdamService idamService;
    @MockBean private RestService restService;

    @Captor private ArgumentCaptor<Map> assignCaseAccessRequestMapCaptor;

    @Before
    public void setUp() {
        AssignCaseAccessRequest assignCaseAccessRequest = AssignCaseAccessRequest
            .builder()
            .caseId(TEST_CASE_ID)
            .caseTypeId(CASE_TYPE_ID_CONTESTED)
            .assigneeId(TEST_USER_ID)
            .build();

        when(idamService.getIdamUserId(AUTH_TOKEN)).thenReturn(TEST_USER_ID);
        when(assignCaseAccessRequestMapper.mapToAssignCaseAccessRequest(any(CaseDetails.class), eq(TEST_USER_ID)))
            .thenReturn(assignCaseAccessRequest);
        when(assignCaseAccessServiceConfiguration.getCaseAssignmentsUrl()).thenReturn(TEST_URL);
    }

    @Test
    public void assignCaseAccess() {
        CaseDetails caseDetails = buildCaseDetails();

        assignCaseAccessService.assignCaseAccess(caseDetails, AUTH_TOKEN);

        verify(idamService, times(1)).getIdamUserId(AUTH_TOKEN);
        verify(assignCaseAccessRequestMapper, times(1)).mapToAssignCaseAccessRequest(caseDetails, TEST_USER_ID);
        verify(assignCaseAccessServiceConfiguration, times(1)).getCaseAssignmentsUrl();
        verify(restService, times(1)).restApiPostCall(eq(AUTH_TOKEN), eq(TEST_URL), assignCaseAccessRequestMapCaptor.capture());

        Assert.assertEquals(assignCaseAccessRequestMapCaptor.getValue().get("case_id"), TEST_CASE_ID);
        Assert.assertEquals(assignCaseAccessRequestMapCaptor.getValue().get("assignee_id"), TEST_USER_ID);
        Assert.assertEquals(assignCaseAccessRequestMapCaptor.getValue().get("case_type_id"), CASE_TYPE_ID_CONTESTED);
    }
}