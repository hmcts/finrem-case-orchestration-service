package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CASE_CRON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

class BulkAddOrganisationPolicyTest {

    private BulkAddOrganisationPolicyTask bulkAddOrganisationPolicyTask;
    private CcdService ccdService;
    private SystemUserService systemUserService;
    private UpdateRepresentationWorkflowService updateRepresentationWorkflowService;

    @BeforeEach
    void setup() {
        ccdService = mock(CcdService.class);
        systemUserService = mock(SystemUserService.class);
        updateRepresentationWorkflowService = mock(UpdateRepresentationWorkflowService.class);
        doCallRealMethod().when(updateRepresentationWorkflowService).persistDefaultOrganisationPolicy(
            any(FinremCaseData.class));
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(
            new ObjectMapper().registerModule(new JavaTimeModule()));

        bulkAddOrganisationPolicyTask = new BulkAddOrganisationPolicyTask(ccdService, systemUserService,
            finremCaseDetailsMapper, updateRepresentationWorkflowService);
        bulkAddOrganisationPolicyTask.setTaskEnabled(true);
        bulkAddOrganisationPolicyTask.setCaseTypeContested();
    }

    @Test
    void givenTaskNotEnabledWhenRunThenDoesNothing() {
        bulkAddOrganisationPolicyTask.setTaskEnabled(false);
        bulkAddOrganisationPolicyTask.run();

        verifyNoInteractions(ccdService);
        verifyNoInteractions(systemUserService);
        verifyNoInteractions(updateRepresentationWorkflowService);
    }

    @Test
    void givenNoCasesNeedUpdatingWhenRunThenNoUpdatesExecuted() {
        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);

        SearchResult searchResult = createSearchResult(Collections.emptyList());
        when(ccdService.esSearchCases(any(CaseType.class), anyString(), anyString())).thenReturn(searchResult);

        bulkAddOrganisationPolicyTask.run();

        verify(ccdService, times(1)).esSearchCases(any(CaseType.class), anyString(), anyString());
        verifyNoMoreInteractions(ccdService);
        verifyNoInteractions(updateRepresentationWorkflowService);
    }

    @Test
    void givenCasesNeedUpdatingWhenRunThenCasesUpdated() {
        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);

        CaseDetails applicantMissing = createCase(1L, null,
            createOrganisationPolicy("XYZ1", "Bort", RESPONDENT_ORGANISATION_POLICY));
        CaseDetails respondentMissing = createCase(2L,
            createOrganisationPolicy("FHYG3", "Marge", APP_SOLICITOR_POLICY), null);
        CaseDetails bothMissing = createCase(3L, null, null);

        SearchResult searchResult = createSearchResult(List.of(applicantMissing, respondentMissing, bothMissing));
        when(ccdService.esSearchCases(any(CaseType.class), anyString(), anyString())).thenReturn(searchResult);

        SearchResult applicantMissingSearchResult = createSearchResult(List.of(applicantMissing));
        when(ccdService.getCaseByCaseId("1", CONTESTED, AUTH_TOKEN)).thenReturn(applicantMissingSearchResult);

        SearchResult respondentMissingSearchResult = createSearchResult(List.of(respondentMissing));
        when(ccdService.getCaseByCaseId("2", CONTESTED, AUTH_TOKEN)).thenReturn(respondentMissingSearchResult);

        SearchResult bothMissingSearchResult = createSearchResult(List.of(bothMissing));
        when(ccdService.getCaseByCaseId("3", CONTESTED, AUTH_TOKEN)).thenReturn(bothMissingSearchResult);

        when(ccdService.startEventForCaseWorker(AUTH_TOKEN, "1", CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType())).thenReturn(createStartEventResponse(applicantMissing));
        when(ccdService.startEventForCaseWorker(AUTH_TOKEN, "2", CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType())).thenReturn(createStartEventResponse(respondentMissing));
        when(ccdService.startEventForCaseWorker(AUTH_TOKEN, "3", CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType())).thenReturn(createStartEventResponse(bothMissing));

        bulkAddOrganisationPolicyTask.run();

        verifyOrganisationPolicy("1");
        verifyOrganisationPolicy("2");
        verifyOrganisationPolicy("3");
    }

    private void verifyOrganisationPolicy(String id) {
        ArgumentCaptor<StartEventResponse> argumentCaptor = ArgumentCaptor.forClass(StartEventResponse.class);
        verify(ccdService).submitEventForCaseWorker(argumentCaptor.capture(), eq(AUTH_TOKEN), eq(id),
            eq(CONTESTED.getCcdType()), eq(AMEND_CASE_CRON.getCcdType()), eq("DFR-3261"), eq("DFR-3261"));
        CaseDetails caseDetails = argumentCaptor.getValue().getCaseDetails();
        assertThat(caseDetails.getData().get(APPLICANT_ORGANISATION_POLICY)).isNotNull();
        assertThat(caseDetails.getData().get(RESPONDENT_ORGANISATION_POLICY)).isNotNull();
    }

    private CaseDetails createCase(Long id, OrganisationPolicy applicantOrganisationPolicy,
                                   OrganisationPolicy respondentOrganisationPolicy) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(APPLICANT_ORGANISATION_POLICY, applicantOrganisationPolicy);
        caseData.put(RESPONDENT_ORGANISATION_POLICY, respondentOrganisationPolicy);

        return CaseDetails.builder()
            .id(id)
            .caseTypeId(CONTESTED.getCcdType())
            .state(State.APPLICATION_ISSUED.getStateId())
            .data(caseData)
            .build();
    }

    private OrganisationPolicy createOrganisationPolicy(String id, String name, String role) {
        return OrganisationPolicy.builder()
            .organisation(Organisation.builder()
                .organisationID(id)
                .organisationName(name)
                .build())
            .orgPolicyCaseAssignedRole(role)
            .build();
    }

    private SearchResult createSearchResult(List<CaseDetails> cases) {
        return SearchResult.builder()
            .cases(cases)
            .total(cases.size())
            .build();
    }

    private StartEventResponse createStartEventResponse(CaseDetails caseDetails) {
        return StartEventResponse.builder()
            .caseDetails(caseDetails)
            .build();
    }
}
