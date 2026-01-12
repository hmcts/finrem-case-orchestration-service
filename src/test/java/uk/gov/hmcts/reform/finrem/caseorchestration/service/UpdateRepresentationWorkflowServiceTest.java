package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.NoticeOfChangeService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHANGE_ORGANISATION_REQUEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOC_PARTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class UpdateRepresentationWorkflowServiceTest {

    @Mock
    private NoticeOfChangeService noticeOfChangeService;

    @Mock
    private AssignCaseAccessService assignCaseAccessService;

    @Mock
    private SystemUserService systemUserService;

    @InjectMocks
    private UpdateRepresentationWorkflowService updateRepresentationWorkflowService;

    private CaseDetails caseDetails;

    private CaseDetails defaultChangeDetails;

    private AboutToStartOrSubmitCallbackResponse response;

    @BeforeEach
    void setUp() {
        Map<String, Object> caseData = new HashMap<>();
        caseDetails = CaseDetails.builder().data(caseData).build();
        Map<String, Object> defaultCaseData = new HashMap<>();
        defaultChangeDetails = CaseDetails.builder().data(defaultCaseData).build();
        Map<String, Object> responseCaseData = new HashMap<>();
        response = AboutToStartOrSubmitCallbackResponse.builder().data(responseCaseData).build();
    }

    @Test
    void givenChangeRequestWithPopulatedOrg_whenHandleWorkflowApplicant_thenCallAssignCaseAccessService() {
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
    void givenChangeRequestWithPopulatedOrg_whenHandleWorkflowRespondent_thenCallAssignCaseAccessService() {
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
    void givenChangeRequestWithUnpopulatedOrg_whenHandleWorkflowApplicant_thenNoCallToAssignCaseAccessService() {
        setNoOrgsChangeOrganisationRequest();
        caseDetails.getData().put(NOC_PARTY, APPLICANT);
        when(noticeOfChangeService.updateRepresentation(caseDetails, AUTH_TOKEN, caseDetails))
            .thenReturn(caseDetails.getData());
        when(noticeOfChangeService.hasInvalidOrgPolicy(caseDetails, true)).thenReturn(true);
        when(noticeOfChangeService.hasInvalidOrgPolicy(caseDetails, false)).thenReturn(false);

        AboutToStartOrSubmitCallbackResponse actualResponse = updateRepresentationWorkflowService
            .handleNoticeOfChangeWorkflow(caseDetails, AUTH_TOKEN, caseDetails);

        verify(assignCaseAccessService, never()).applyDecision(AUTH_TOKEN, caseDetails);
        assertEquals(getChangeOrganisationRequest(actualResponse.getData()), getDefaultChangeRequest());
    }

    @Test
    void givenChangeRequestWithUnpopulatedOrg_whenHandleWorkflowRespondent_thenNoCallToAssignCaseAccessService() {
        setNoOrgsChangeOrganisationRequest();
        caseDetails.getData().put(NOC_PARTY, RESPONDENT);
        when(noticeOfChangeService.updateRepresentation(caseDetails, AUTH_TOKEN, caseDetails))
            .thenReturn(caseDetails.getData());
        when(noticeOfChangeService.hasInvalidOrgPolicy(caseDetails, true)).thenReturn(false);
        when(noticeOfChangeService.hasInvalidOrgPolicy(caseDetails, false)).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse actualResponse = updateRepresentationWorkflowService
            .handleNoticeOfChangeWorkflow(caseDetails, AUTH_TOKEN, caseDetails);

        verify(assignCaseAccessService, never()).applyDecision(AUTH_TOKEN, caseDetails);
        assertEquals(getChangeOrganisationRequest(actualResponse.getData()), getDefaultChangeRequest());
    }

    @Test
    void givenContestedCase_whenBothPartyUnRepresented_thenAddDefaultRole() {
        FinremCaseDetails finremCaseDetails = getFinremCaseDataDetails();
        FinremCaseData data = finremCaseDetails.getData();
        updateRepresentationWorkflowService.persistDefaultOrganisationPolicy(data);

        assertEquals(CaseRole.APP_SOLICITOR.getCcdCode(),
            data.getApplicantOrganisationPolicy().getOrgPolicyCaseAssignedRole());
        assertEquals(CaseRole.RESP_SOLICITOR.getCcdCode(),
            data.getRespondentOrganisationPolicy().getOrgPolicyCaseAssignedRole());
    }

    @Test
    void givenContestedCase_whenRespondentUnRepresented_thenAddDefaultRole() {
        FinremCaseDetails finremCaseDetails = getFinremCaseDataDetails();
        FinremCaseData data = finremCaseDetails.getData();
        data.setApplicantOrganisationPolicy(getOrganisationPolicy(CaseRole.APP_SOLICITOR));
        updateRepresentationWorkflowService.persistDefaultOrganisationPolicy(data);
        assertEquals(CaseRole.APP_SOLICITOR.getCcdCode(),
            data.getApplicantOrganisationPolicy().getOrgPolicyCaseAssignedRole());
        assertEquals("XX9191910", data.getApplicantOrganisationPolicy().getOrgPolicyReference());
        assertNull(data.getRespondentOrganisationPolicy().getOrgPolicyReference());
        assertEquals(CaseRole.RESP_SOLICITOR.getCcdCode(),
            data.getRespondentOrganisationPolicy().getOrgPolicyCaseAssignedRole());
    }

    @Test
    void givenContestedCase_whenApplicantUnRepresented_thenAddDefaultRole() {
        FinremCaseDetails finremCaseDetails = getFinremCaseDataDetails();
        FinremCaseData data = finremCaseDetails.getData();
        data.setRespondentOrganisationPolicy(getOrganisationPolicy(CaseRole.RESP_SOLICITOR));
        updateRepresentationWorkflowService.persistDefaultOrganisationPolicy(data);
        assertEquals(CaseRole.APP_SOLICITOR.getCcdCode(),
            data.getApplicantOrganisationPolicy().getOrgPolicyCaseAssignedRole());
        assertNull(data.getApplicantOrganisationPolicy().getOrgPolicyReference());
        assertEquals(CaseRole.RESP_SOLICITOR.getCcdCode(),
            data.getRespondentOrganisationPolicy().getOrgPolicyCaseAssignedRole());
        assertEquals("XX9191910", data.getRespondentOrganisationPolicy().getOrgPolicyReference());
    }

    @Test
    void givenContestedCase_whenApplicantOrgPolicyEmpty_thenAddDefaultRole() {
        FinremCaseDetails finremCaseDetails = getFinremCaseDataDetails();
        FinremCaseData data = finremCaseDetails.getData();
        data.setApplicantOrganisationPolicy(new OrganisationPolicy());
        data.setRespondentOrganisationPolicy(getOrganisationPolicy(CaseRole.RESP_SOLICITOR));

        updateRepresentationWorkflowService.persistDefaultOrganisationPolicy(data);

        assertEquals(CaseRole.APP_SOLICITOR.getCcdCode(),
            data.getApplicantOrganisationPolicy().getOrgPolicyCaseAssignedRole());
        assertNull(data.getApplicantOrganisationPolicy().getOrgPolicyReference());
        assertEquals("XX9191910", data.getRespondentOrganisationPolicy().getOrgPolicyReference());
        assertEquals(CaseRole.RESP_SOLICITOR.getCcdCode(),
            data.getRespondentOrganisationPolicy().getOrgPolicyCaseAssignedRole());
    }

    @Test
    void givenContestedCase_whenRespondentOrgPolicyEmpty_thenAddDefaultRole() {
        FinremCaseDetails finremCaseDetails = getFinremCaseDataDetails();
        FinremCaseData data = finremCaseDetails.getData();
        data.setApplicantOrganisationPolicy(getOrganisationPolicy(CaseRole.APP_SOLICITOR));
        data.setRespondentOrganisationPolicy(new OrganisationPolicy());

        updateRepresentationWorkflowService.persistDefaultOrganisationPolicy(data);

        assertEquals(CaseRole.APP_SOLICITOR.getCcdCode(),
            data.getApplicantOrganisationPolicy().getOrgPolicyCaseAssignedRole());
        assertEquals("XX9191910", data.getApplicantOrganisationPolicy().getOrgPolicyReference());
        assertEquals(CaseRole.RESP_SOLICITOR.getCcdCode(),
            data.getRespondentOrganisationPolicy().getOrgPolicyCaseAssignedRole());
        assertNull(data.getRespondentOrganisationPolicy().getOrgPolicyReference());
    }

    @Test
    void givenNoOrganisationsToAddOrRemove_whenPrepareChangeOrganisationRequest_thenAddDefaultRole() {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        ChangeOrganisationRequest cor = mock(ChangeOrganisationRequest.class);
        when(cor.isNoOrganisationsToAddOrRemove()).thenReturn(false);
        finremCaseData.setChangeOrganisationRequestField(cor);
        FinremCaseData finremCaseDataBefore = mock(FinremCaseData.class);
        EventType eventType = mock(EventType.class);

        updateRepresentationWorkflowService.prepareNoticeOfChangeAndOrganisationPolicy(finremCaseData, finremCaseDataBefore, eventType,
            AUTH_TOKEN);

        verify(noticeOfChangeService).updateRepresentationUpdateHistory(finremCaseData, finremCaseDataBefore, eventType,
            AUTH_TOKEN);
        verify(noticeOfChangeService).populateChangeOrganisationRequestField(finremCaseData, finremCaseDataBefore);
        verify(finremCaseData, never()).setApplicantOrganisationPolicy(any(OrganisationPolicy.class));
        verify(finremCaseData, never()).setRespondentOrganisationPolicy(any(OrganisationPolicy.class));
    }

    @Test
    void givenHavingOrganisationsToAddOrRemove_whenPrepareChangeOrganisationRequest_thenAddDefaultRole() {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        ChangeOrganisationRequest cor = mock(ChangeOrganisationRequest.class);
        when(cor.isNoOrganisationsToAddOrRemove()).thenReturn(true);
        finremCaseData.setChangeOrganisationRequestField(cor);
        FinremCaseData finremCaseDataBefore = mock(FinremCaseData.class);
        EventType eventType = mock(EventType.class);

        updateRepresentationWorkflowService.prepareNoticeOfChangeAndOrganisationPolicy(finremCaseData, finremCaseDataBefore,
            eventType, AUTH_TOKEN);

        final Organisation emptyOrganisation = Organisation.builder().organisationID(null).organisationName(null).build();
        assertThat(finremCaseData.getApplicantOrganisationPolicy())
            .extracting(OrganisationPolicy::getOrgPolicyCaseAssignedRole,
                OrganisationPolicy::getOrganisation)
            .contains(CaseRole.APP_SOLICITOR.getCcdCode(), emptyOrganisation);

        assertThat(finremCaseData.getRespondentOrganisationPolicy())
            .extracting(OrganisationPolicy::getOrgPolicyCaseAssignedRole,
                OrganisationPolicy::getOrganisation)
            .contains(CaseRole.RESP_SOLICITOR.getCcdCode(), emptyOrganisation);

        verify(noticeOfChangeService).updateRepresentationUpdateHistory(finremCaseData, finremCaseDataBefore, eventType,
            AUTH_TOKEN);
        verify(noticeOfChangeService).populateChangeOrganisationRequestField(finremCaseData, finremCaseDataBefore);
        verify(finremCaseData).setApplicantOrganisationPolicy(any(OrganisationPolicy.class));
        verify(finremCaseData).setRespondentOrganisationPolicy(any(OrganisationPolicy.class));
    }

    private OrganisationPolicy getOrganisationPolicy(CaseRole role) {
        return OrganisationPolicy
            .builder()
            .organisation(Organisation.builder().organisationID("abc")
                .organisationName("abc limited").build())
            .orgPolicyReference("XX9191910")
            .orgPolicyCaseAssignedRole(role.getCcdCode())
            .build();
    }

    private FinremCaseDetails getFinremCaseDataDetails() {
        return FinremCaseDetails.builder().id(Long.valueOf(CASE_ID)).caseType(CONTESTED)
            .data(new FinremCaseData()).build();
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
