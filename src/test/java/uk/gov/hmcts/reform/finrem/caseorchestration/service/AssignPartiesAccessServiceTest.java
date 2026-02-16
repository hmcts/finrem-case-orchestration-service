package uk.gov.hmcts.reform.finrem.caseorchestration.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SYSTEM_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.organisationPolicy;

@ExtendWith(MockitoExtension.class)
class AssignPartiesAccessServiceTest {

    @Mock
    private AssignCaseAccessService assignCaseAccessService;

    @Mock
    private PrdOrganisationService prdOrganisationService;

    @Mock
    private SystemUserService systemUserService;

    @InjectMocks
    private AssignPartiesAccessService assignPartiesAccessService;

    @BeforeEach
    void setUp() {
        lenient().when(systemUserService.getSysUserToken()).thenReturn(TEST_SYSTEM_TOKEN);
    }

    @Test
    void givenUnrepresentedApplicant_whenGrantApplicantSolicitorInvoked_thenIgnoreTheRequest() {
        FinremCaseData caseData = mock(FinremCaseData.class);
        when(caseData.isApplicantRepresentedByASolicitor()).thenReturn(false);

        // Act
        assignPartiesAccessService.grantApplicantSolicitor(caseData);

        verify(assignCaseAccessService, never()).grantCaseRoleToUser(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void givenMissingOrgId_whenGrantApplicantSolicitorInvoked_thenIgnoreTheRequest() {
        FinremCaseData caseData = mock(FinremCaseData.class);
        when(caseData.isApplicantRepresentedByASolicitor()).thenReturn(true);
        when(caseData.getApplicantOrganisationPolicy()).thenReturn(organisationPolicy(null));

        // Act
        assignPartiesAccessService.grantApplicantSolicitor(caseData);

        verify(assignCaseAccessService, never()).grantCaseRoleToUser(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void givenEmailRegistered_whenGrantApplicantSolicitorInvoked_thenAssignAppSolicitorToCase() {
        FinremCaseData caseData = mock(FinremCaseData.class);
        when(caseData.getAppSolicitorEmail()).thenReturn(TEST_SOLICITOR_EMAIL);
        when(caseData.getCcdCaseId()).thenReturn(CASE_ID);
        when(caseData.isApplicantRepresentedByASolicitor()).thenReturn(true);
        when(caseData.getApplicantOrganisationPolicy()).thenReturn(organisationPolicy(TEST_ORG_ID));

        when(prdOrganisationService.findUserByEmail(TEST_SOLICITOR_EMAIL, TEST_SYSTEM_TOKEN)).thenReturn(
            Optional.of(TEST_USER_ID)
        );

        // Act
        assignPartiesAccessService.grantApplicantSolicitor(caseData);

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID_IN_LONG, TEST_USER_ID, CaseRole.APP_SOLICITOR.getCcdCode(),
            TEST_ORG_ID);
        verifyNoMoreInteractions(assignCaseAccessService);
    }

    @Test
    void givenEmailNotFound_whenGrantApplicantSolicitorInvoked_thenShouldNotAssignAppSolicitorToCase() {
        FinremCaseData caseData = mock(FinremCaseData.class);
        when(caseData.getAppSolicitorEmail()).thenReturn(TEST_SOLICITOR_EMAIL);
        when(caseData.getCcdCaseId()).thenReturn(CASE_ID);
        when(caseData.isApplicantRepresentedByASolicitor()).thenReturn(true);
        when(caseData.getApplicantOrganisationPolicy()).thenReturn(organisationPolicy(TEST_ORG_ID));

        when(prdOrganisationService.findUserByEmail(TEST_SOLICITOR_EMAIL, TEST_SYSTEM_TOKEN)).thenReturn(
            Optional.empty()
        );

        // Act
        assignPartiesAccessService.grantApplicantSolicitor(caseData);

        verify(assignCaseAccessService, never()).grantCaseRoleToUser(anyLong(), anyString(), anyString(), anyString());
    }
}
