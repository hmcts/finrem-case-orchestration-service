package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_CASE_ROLE;
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

    @Nested
    class GrantApplicantSolicitorTests {

        @Test
        void givenUnrepresentedApplicant_thenIgnoreTheRequest() {
            FinremCaseData caseData = mock(FinremCaseData.class);
            when(caseData.isApplicantRepresentedByASolicitor()).thenReturn(false);

            // Act
            assignPartiesAccessService.grantApplicantSolicitor(caseData);

            verifyNotGrantingCaseRoleToUser();
        }

        @Test
        void givenMissingOrgId_thenIgnoreTheRequest() {
            FinremCaseData caseData = mock(FinremCaseData.class);
            when(caseData.isApplicantRepresentedByASolicitor()).thenReturn(true);
            when(caseData.getApplicantOrganisationPolicy()).thenReturn(organisationPolicy(null));

            // Act
            assignPartiesAccessService.grantApplicantSolicitor(caseData);

            verifyNotGrantingCaseRoleToUser();
        }

        @Test
        void givenEmailRegistered_thenAssignAppSolicitorToCase() {
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
        void givenMissingEmail_thenIgnoreTheRequest() {
            FinremCaseData caseData = mock(FinremCaseData.class);
            when(caseData.getAppSolicitorEmail()).thenReturn(null);
            when(caseData.getCcdCaseId()).thenReturn(CASE_ID);
            when(caseData.isApplicantRepresentedByASolicitor()).thenReturn(true);
            when(caseData.getApplicantOrganisationPolicy()).thenReturn(organisationPolicy(TEST_ORG_ID));

            // Act
            assignPartiesAccessService.grantApplicantSolicitor(caseData);

            verifyNotGrantingCaseRoleToUser();
        }

        @Test
        void givenEmailNotFound_thenShouldNotAssignAppSolicitorToCase() {
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

            verifyNotGrantingCaseRoleToUser();
        }
    }

    @Nested
    class GrantRespondentSolicitorTests {

        @Test
        void givenUnrepresentedRespondent_thenIgnoreTheRequest() {
            FinremCaseData caseData = mock(FinremCaseData.class);
            when(caseData.isRespondentRepresentedByASolicitor()).thenReturn(false);

            // Act
            assignPartiesAccessService.grantRespondentSolicitor(caseData);

            verifyNotGrantingCaseRoleToUser();
        }

        @Test
        void givenMissingOrgId_thenIgnoreTheRequest() {
            FinremCaseData caseData = mock(FinremCaseData.class);
            when(caseData.isRespondentRepresentedByASolicitor()).thenReturn(true);
            when(caseData.getRespondentOrganisationPolicy()).thenReturn(organisationPolicy(null));

            // Act
            assignPartiesAccessService.grantRespondentSolicitor(caseData);

            verifyNotGrantingCaseRoleToUser();
        }

        @Test
        void givenEmailRegistered_thenAssignAppSolicitorToCase() {
            FinremCaseData caseData = mock(FinremCaseData.class);
            when(caseData.getRespondentSolicitorEmail()).thenReturn(TEST_SOLICITOR_EMAIL);
            when(caseData.getCcdCaseId()).thenReturn(CASE_ID);
            when(caseData.isRespondentRepresentedByASolicitor()).thenReturn(true);
            when(caseData.getRespondentOrganisationPolicy()).thenReturn(organisationPolicy(TEST_ORG_ID));

            when(prdOrganisationService.findUserByEmail(TEST_SOLICITOR_EMAIL, TEST_SYSTEM_TOKEN)).thenReturn(
                Optional.of(TEST_USER_ID)
            );

            // Act
            assignPartiesAccessService.grantRespondentSolicitor(caseData);

            verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID_IN_LONG, TEST_USER_ID, CaseRole.RESP_SOLICITOR.getCcdCode(),
                TEST_ORG_ID);
            verifyNoMoreInteractions(assignCaseAccessService);
        }

        @Test
        void givenMissingEmail_thenIgnoreTheRequest() {
            FinremCaseData caseData = mock(FinremCaseData.class);
            when(caseData.getRespondentSolicitorEmail()).thenReturn(null);
            when(caseData.getCcdCaseId()).thenReturn(CASE_ID);
            when(caseData.isRespondentRepresentedByASolicitor()).thenReturn(true);
            when(caseData.getRespondentOrganisationPolicy()).thenReturn(organisationPolicy(TEST_ORG_ID));

            // Act
            assignPartiesAccessService.grantRespondentSolicitor(caseData);

            verifyNotGrantingCaseRoleToUser();
        }

        @Test
        void givenEmailNotFound_thenShouldNotAssignAppSolicitorToCase() {
            FinremCaseData caseData = mock(FinremCaseData.class);
            when(caseData.getRespondentSolicitorEmail()).thenReturn(TEST_SOLICITOR_EMAIL);
            when(caseData.getCcdCaseId()).thenReturn(CASE_ID);
            when(caseData.isRespondentRepresentedByASolicitor()).thenReturn(true);
            when(caseData.getRespondentOrganisationPolicy()).thenReturn(organisationPolicy(TEST_ORG_ID));

            when(prdOrganisationService.findUserByEmail(TEST_SOLICITOR_EMAIL, TEST_SYSTEM_TOKEN)).thenReturn(
                Optional.empty()
            );

            // Act
            assignPartiesAccessService.grantRespondentSolicitor(caseData);

            verifyNotGrantingCaseRoleToUser();
        }
    }

    @Nested
    class GrantIntervenerSolicitorTests {

        @Test
        void givenUnrepresentedIntervener_thenIgnoreTheRequest() throws UserNotFoundInOrganisationApiException {
            IntervenerWrapper intervenerWrapper = mock(IntervenerWrapper.class);
            when(intervenerWrapper.getIntervenerRepresented()).thenReturn(YesOrNo.NO);

            // Act
            assignPartiesAccessService.grantIntervenerSolicitor(CASE_ID_IN_LONG, intervenerWrapper);

            verifyNotGrantingCaseRoleToUser();
        }

        @Test
        void givenMissingOrgId_thenIgnoreTheRequest() throws UserNotFoundInOrganisationApiException {
            IntervenerWrapper intervenerWrapper = mock(IntervenerWrapper.class);
            when(intervenerWrapper.getIntervenerRepresented()).thenReturn(YesOrNo.YES);
            when(intervenerWrapper.getIntervenerOrganisation()).thenReturn(organisationPolicy(null));

            // Act
            assignPartiesAccessService.grantIntervenerSolicitor(CASE_ID_IN_LONG, intervenerWrapper);

            verifyNotGrantingCaseRoleToUser();
        }

        @Test
        void givenMissingCaseRole_thenIgnoreTheRequest() throws UserNotFoundInOrganisationApiException {
            IntervenerWrapper intervenerWrapper = mock(IntervenerWrapper.class);
            when(intervenerWrapper.getIntervenerRepresented()).thenReturn(YesOrNo.YES);
            when(intervenerWrapper.getIntervenerOrganisation()).thenReturn(organisationPolicy(TEST_ORG_ID));

            // Act
            assignPartiesAccessService.grantIntervenerSolicitor(CASE_ID_IN_LONG, intervenerWrapper);

            verifyNotGrantingCaseRoleToUser();
        }

        @Test
        void givenMissingEmail_thenIgnoreTheRequest() {
            IntervenerWrapper intervenerWrapper = mock(IntervenerWrapper.class);
            when(intervenerWrapper.getIntervenerSolEmail()).thenReturn(null);
            when(intervenerWrapper.getIntervenerRepresented()).thenReturn(YesOrNo.YES);
            when(intervenerWrapper.getIntervenerOrganisation()).thenReturn(organisationPolicy(TEST_ORG_ID));
            CaseRole caseRole = mock(CaseRole.class);
            when(caseRole.getCcdCode()).thenReturn(TEST_CASE_ROLE);
            when(intervenerWrapper.getIntervenerSolicitorCaseRole()).thenReturn(caseRole);

            // Act
            assertThrows(UserNotFoundInOrganisationApiException.class, ()
                -> assignPartiesAccessService.grantIntervenerSolicitor(CASE_ID_IN_LONG, intervenerWrapper));
            verifyNotGrantingCaseRoleToUser();
        }

        @Test
        void givenEmailRegistered_thenAssignAppSolicitorToCase() throws UserNotFoundInOrganisationApiException {
            IntervenerWrapper intervenerWrapper = mock(IntervenerWrapper.class);
            when(intervenerWrapper.getIntervenerSolEmail()).thenReturn(TEST_SOLICITOR_EMAIL);
            when(intervenerWrapper.getIntervenerRepresented()).thenReturn(YesOrNo.YES);
            when(intervenerWrapper.getIntervenerOrganisation()).thenReturn(organisationPolicy(TEST_ORG_ID));
            CaseRole caseRole = mock(CaseRole.class);
            when(caseRole.getCcdCode()).thenReturn(TEST_CASE_ROLE);
            when(intervenerWrapper.getIntervenerSolicitorCaseRole()).thenReturn(caseRole);

            when(prdOrganisationService.findUserByEmail(TEST_SOLICITOR_EMAIL, TEST_SYSTEM_TOKEN)).thenReturn(
                Optional.of(TEST_USER_ID)
            );

            // Act
            assignPartiesAccessService.grantIntervenerSolicitor(CASE_ID_IN_LONG, intervenerWrapper);

            verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID_IN_LONG, TEST_USER_ID, TEST_CASE_ROLE,
                TEST_ORG_ID);
            verifyNoMoreInteractions(assignCaseAccessService);
        }
    }

    private void verifyNotGrantingCaseRoleToUser() {
        verify(assignCaseAccessService, never()).grantCaseRoleToUser(anyLong(), anyString(), anyString(), anyString());
    }
}
