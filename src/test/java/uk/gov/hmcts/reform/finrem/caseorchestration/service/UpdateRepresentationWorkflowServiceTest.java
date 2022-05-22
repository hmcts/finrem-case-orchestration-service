package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.NoticeOfChangeService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHANGE_ORGANISATION_REQUEST;

@RunWith(MockitoJUnitRunner.class)
public class UpdateRepresentationWorkflowServiceTest {

    private static final String AUTH_TOKEN = "AuthToken";

    @Mock
    NoticeOfChangeService noticeOfChangeService;

    @Mock
    AssignCaseAccessService assignCaseAccessService;

    @Mock
    SystemUserService systemUserService;

    @InjectMocks
    UpdateRepresentationWorkflowService updateRepresentationWorkflowService;

    private CaseDetails caseDetails;

    private CaseDetails defaultChangeDetails;

    private AboutToStartOrSubmitCallbackResponse response;

    @Before
    public void setUp() {
        Map<String, Object> caseData = new HashMap<>();
        caseDetails = CaseDetails.builder().data(caseData).build();
        Map<String, Object> defaultCaseData = new HashMap<>();
        defaultChangeDetails = CaseDetails.builder().data(defaultCaseData).build();
        Map<String, Object> responseCaseData = new HashMap<>();
        response = AboutToStartOrSubmitCallbackResponse.builder().data(responseCaseData).build();
    }

    @Test
    public void givenChangeRequestWithPopulatedOrg_whenHandleWorkflow_thenCallAssignCaseAccessService() {
        setPopulatedChangeOrganisationRequest();
        when(noticeOfChangeService.updateRepresentation(caseDetails, AUTH_TOKEN, caseDetails))
            .thenReturn(caseDetails.getData());
        when(noticeOfChangeService.persistOriginalOrgPoliciesWhenRevokingAccess(caseDetails, caseDetails))
            .thenReturn(caseDetails);
        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        setDefaultChangeRequest();
        response.setData(defaultChangeDetails.getData());
        when(assignCaseAccessService.applyDecision(AUTH_TOKEN, caseDetails)).thenReturn(response);

        updateRepresentationWorkflowService.handleNoticeOfChangeWorkflow(caseDetails, AUTH_TOKEN, caseDetails);

        verify(assignCaseAccessService, times(1)).applyDecision(AUTH_TOKEN, caseDetails);
        assertEquals(getChangeOrganisationRequest(response.getData()), getDefaultChangeRequest());
    }

    @Test
    public void givenChangeRequestWithUnpopulatedOrg_whenHandleWorkflow_thenNoCallToAssignCaseAccessService() {
        setNoOrgsChangeOrganisationRequest();
        when(noticeOfChangeService.updateRepresentation(caseDetails, AUTH_TOKEN, caseDetails))
            .thenReturn(caseDetails.getData());

        AboutToStartOrSubmitCallbackResponse actualResponse = updateRepresentationWorkflowService
            .handleNoticeOfChangeWorkflow(caseDetails, AUTH_TOKEN, caseDetails);

        verify(assignCaseAccessService, never()).applyDecision(AUTH_TOKEN, caseDetails);
        assertEquals(getChangeOrganisationRequest(actualResponse.getData()), getDefaultChangeRequest());
    }

    private void setNoOrgsChangeOrganisationRequest() {
        ChangeOrganisationRequest changeRequest = ChangeOrganisationRequest.builder()
            .approvalStatus(ChangeOrganisationApprovalStatus.APPROVED)
            .approvalRejectionTimestamp(LocalDateTime.now())
            .requestTimestamp(LocalDateTime.now())
            .caseRoleId(null)
            .organisationToRemove(null)
            .organisationToAdd(null)
            .reason(null)
            .build();
        caseDetails.getData().put(CHANGE_ORGANISATION_REQUEST, changeRequest);
    }

    private void setPopulatedChangeOrganisationRequest() {
        ChangeOrganisationRequest changeRequest = ChangeOrganisationRequest.builder()
            .approvalStatus(ChangeOrganisationApprovalStatus.APPROVED)
            .approvalRejectionTimestamp(LocalDateTime.now())
            .requestTimestamp(LocalDateTime.now())
            .caseRoleId(DynamicList.builder()
                .value(DynamicListElement.builder().code(APP_SOLICITOR_POLICY).label(APP_SOLICITOR_POLICY).build())
                .listItems(List.of(DynamicListElement.builder().code(APP_SOLICITOR_POLICY).label(APP_SOLICITOR_POLICY)
                    .build()))
                .build())
            .organisationToRemove(Organisation.builder().organisationName("TestOrgName").organisationID("TestId").build())
            .organisationToAdd(null)
            .reason("bad solicitor")
            .build();
        caseDetails.getData().put(CHANGE_ORGANISATION_REQUEST, changeRequest);
    }

    private ChangeOrganisationRequest getChangeOrganisationRequest(Map<String, Object> caseData) {
        return new ObjectMapper().registerModule(new JavaTimeModule()).convertValue(caseData.get(CHANGE_ORGANISATION_REQUEST),
            ChangeOrganisationRequest.class);
    }

    private ChangeOrganisationRequest getDefaultChangeRequest() {
        return ChangeOrganisationRequest.builder()
            .reason(null)
            .requestTimestamp(null)
            .approvalRejectionTimestamp(null)
            .caseRoleId(null)
            .organisationToRemove(null)
            .organisationToAdd(null)
            .approvalStatus(null)
            .build();
    }

    private void setDefaultChangeRequest() {
        ChangeOrganisationRequest defaultChangeRequest = ChangeOrganisationRequest.builder()
            .reason(null)
            .requestTimestamp(null)
            .approvalRejectionTimestamp(null)
            .caseRoleId(null)
            .organisationToRemove(null)
            .organisationToAdd(null)
            .approvalStatus(null)
            .build();
        defaultChangeDetails.getData().put(CHANGE_ORGANISATION_REQUEST, defaultChangeRequest);
    }

}
