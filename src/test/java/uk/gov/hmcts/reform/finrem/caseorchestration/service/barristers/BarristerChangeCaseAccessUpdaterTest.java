package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.APP_BARRISTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerServiceTest.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerTestUtils.TEST_ORGANISATION_ID;

@ExtendWith(MockitoExtension.class)
class BarristerChangeCaseAccessUpdaterTest {

    @InjectMocks
    private BarristerChangeCaseAccessUpdater barristerChangeCaseAccessUpdater;
    @Mock
    private ManageBarristerService manageBarristerService;
    @Mock
    private PrdOrganisationService prdOrganisationService;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private AssignCaseAccessService assignCaseAccessService;
    @Mock
    private BarristerRepresentationUpdateBuilder barristerRepresentationUpdateBuilder;
    @Captor
    private final ArgumentCaptor<BarristerRepresentationUpdateBuilder.BarristerUpdateParams> captorAdded =
        ArgumentCaptor.forClass(BarristerRepresentationUpdateBuilder.BarristerUpdateParams.class);
    private final ArgumentCaptor<BarristerRepresentationUpdateBuilder.BarristerUpdateParams> captorRemoved =
        ArgumentCaptor.forClass(BarristerRepresentationUpdateBuilder.BarristerUpdateParams.class);

    @BeforeEach
    void setUp() {
        when(manageBarristerService.getBarristerCaseRole(any(BarristerParty.class)))
            .thenReturn(APP_BARRISTER);
    }

    @Test
    void givenOnlyBarristersAdded_whenUpdate_thenUpdateCaseAccess() {
        Set<Barrister> barristers = BarristerTestUtils.createBarristers();
        BarristerChange barristerChange = BarristerTestUtils.createBarristerChange(barristers, null);
        FinremCaseDetails caseDetails = createCaseDetails();

        barristerChangeCaseAccessUpdater.update(caseDetails, AUTH_TOKEN, barristerChange);

        barristers.forEach(barrister -> {
            verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, barrister.getUserId(),
                APP_BARRISTER.getCcdCode(), barrister.getOrganisation().getOrganisationID());
        });

        verify(barristerRepresentationUpdateBuilder, times(barristers.size()))
            .buildBarristerAdded(captorAdded.capture());
        List<BarristerRepresentationUpdateBuilder.BarristerUpdateParams> capturedParams = captorAdded.getAllValues();

        capturedParams.forEach(p -> verifyBarristerUpdateParams(p, caseDetails.getData(), barristers));

        verifyNoInteractions(prdOrganisationService, systemUserService);

        assertThat(caseDetails.getData().getRepresentationUpdateHistory()).hasSize(barristers.size());
    }

    @Test
    void givenOnlyBarristersRemoved_whenUpdate_thenUpdateCaseAccess() {
        Set<Barrister> barristers = BarristerTestUtils.createBarristers();
        BarristerChange barristerChange = BarristerTestUtils.createBarristerChange(null, barristers);
        FinremCaseDetails caseDetails = createCaseDetails();

        barristerChangeCaseAccessUpdater.update(caseDetails, AUTH_TOKEN, barristerChange);

        barristers.forEach(barrister -> {
            verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, barrister.getUserId(),
                APP_BARRISTER.getCcdCode(), barrister.getOrganisation().getOrganisationID());
        });

        verify(barristerRepresentationUpdateBuilder, times(barristers.size()))
            .buildBarristerRemoved(captorRemoved.capture());
        List<BarristerRepresentationUpdateBuilder.BarristerUpdateParams> capturedParams = captorRemoved.getAllValues();

        capturedParams.forEach(p -> verifyBarristerUpdateParams(p, caseDetails.getData(), barristers));

        verifyNoInteractions(prdOrganisationService, systemUserService);

        assertThat(caseDetails.getData().getRepresentationUpdateHistory()).hasSize(barristers.size());
    }

    @Test
    void givenBothBarristersAddedAndRemoved_whenUpdate_thenUpdateCaseAccess() {
        Set<Barrister> barristersAdded = BarristerTestUtils.createBarristers();
        Set<Barrister> barristersRemoved = BarristerTestUtils.createBarristers();
        BarristerChange barristerChange = BarristerTestUtils.createBarristerChange(barristersAdded, barristersRemoved);
        FinremCaseDetails caseDetails = createCaseDetails();

        barristerChangeCaseAccessUpdater.update(caseDetails, AUTH_TOKEN, barristerChange);

        barristersAdded.forEach(barrister -> {
            verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, barrister.getUserId(),
                APP_BARRISTER.getCcdCode(), barrister.getOrganisation().getOrganisationID());
        });

        verify(barristerRepresentationUpdateBuilder, times(barristersAdded.size()))
            .buildBarristerAdded(captorAdded.capture());
        List<BarristerRepresentationUpdateBuilder.BarristerUpdateParams> capturedParams = captorAdded.getAllValues();

        capturedParams.forEach(p -> verifyBarristerUpdateParams(p, caseDetails.getData(),barristersAdded));

        barristersRemoved.forEach(barrister -> {
            verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, barrister.getUserId(),
                APP_BARRISTER.getCcdCode(), barrister.getOrganisation().getOrganisationID());
        });

        verify(barristerRepresentationUpdateBuilder, times(barristersRemoved.size()))
            .buildBarristerRemoved(captorRemoved.capture());
        capturedParams = captorRemoved.getAllValues();

        capturedParams.forEach(p -> verifyBarristerUpdateParams(p, caseDetails.getData(), barristersRemoved));

        verifyNoInteractions(prdOrganisationService, systemUserService);

        int expectedRepresentationUpdates = barristersAdded.size() + barristersRemoved.size();
        assertThat(caseDetails.getData().getRepresentationUpdateHistory()).hasSize(expectedRepresentationUpdates);
    }

    @Test
    void givenBarristersRemovedAndUserIdMissing_whenUpdate_thenUpdateCaseAccess() {
        Set<Barrister> barristers = BarristerTestUtils.createBarristers();
        barristers.forEach(barrister -> barrister.setUserId(null));

        String systemAuthToken = "system-user-auth-token";
        when(systemUserService.getSysUserToken()).thenReturn(systemAuthToken);

        mockOrganisationServiceToReturnUserId("barrister1@test.com", systemAuthToken, "1");
        mockOrganisationServiceToReturnUserId("barrister2@test.com", systemAuthToken, "2");
        mockOrganisationServiceToReturnUserId("barrister3@test.com", systemAuthToken, "3");

        BarristerChange barristerChange = BarristerTestUtils.createBarristerChange(null, barristers);
        FinremCaseDetails caseDetails = createCaseDetails();

        barristerChangeCaseAccessUpdater.update(caseDetails, AUTH_TOKEN, barristerChange);

        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, "1", APP_BARRISTER.getCcdCode(), TEST_ORGANISATION_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, "2", APP_BARRISTER.getCcdCode(), TEST_ORGANISATION_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, "3", APP_BARRISTER.getCcdCode(), TEST_ORGANISATION_ID);
        verify(barristerRepresentationUpdateBuilder, times(barristers.size()))
            .buildBarristerRemoved(captorRemoved.capture());
        List<BarristerRepresentationUpdateBuilder.BarristerUpdateParams> capturedParams = captorRemoved.getAllValues();

        capturedParams.forEach(p -> verifyBarristerUpdateParams(p, caseDetails.getData(), barristers));

        assertThat(capturedParams).hasSize(barristers.size());
        assertThat(caseDetails.getData().getRepresentationUpdateHistory()).hasSize(barristers.size());
    }

    /**
     * Test case for scenario where barristers are removed from the case data but their account is suspended.
     * which means their userId cannot be retrieved from the organisation service and so they cannot be removed from case access.
     */
    @Test
    void givenBarristersRemovedAndUserSuspended_whenUpdate_thenNoUpdateCaseAccess() {
        Set<Barrister> barristers = BarristerTestUtils.createBarristers();
        barristers.forEach(barrister -> barrister.setUserId(null));

        String systemAuthToken = "system-user-auth-token";
        when(systemUserService.getSysUserToken()).thenReturn(systemAuthToken);

        // Mock organisation service to return empty (simulating suspended user)
        when(prdOrganisationService.findUserByEmail(anyString(), anyString())).thenReturn(Optional.empty());

        BarristerChange barristerChange = BarristerTestUtils.createBarristerChange(null, barristers);
        FinremCaseDetails caseDetails = createCaseDetails();

        barristerChangeCaseAccessUpdater.update(caseDetails, AUTH_TOKEN, barristerChange);

        verifyNoInteractions(assignCaseAccessService);

        verify(barristerRepresentationUpdateBuilder, times(barristers.size()))
            .buildBarristerRemoved(captorRemoved.capture());
        List<BarristerRepresentationUpdateBuilder.BarristerUpdateParams> capturedParams = captorRemoved.getAllValues();

        capturedParams.forEach(p -> verifyBarristerUpdateParams(p, caseDetails.getData(), barristers));

        assertThat(caseDetails.getData().getRepresentationUpdateHistory()).hasSize(barristers.size());
    }

    private void mockOrganisationServiceToReturnUserId(String email, String authToken, String userId) {
        when(prdOrganisationService.findUserByEmail(email, authToken)).thenReturn(java.util.Optional.of(userId));
    }

    private FinremCaseDetails createCaseDetails() {
        return FinremCaseDetails.builder()
            .id(CASE_ID)
            .data(FinremCaseData.builder()
                .barristerParty(BarristerParty.APPLICANT)
                .build())
            .build();
    }

    private void verifyBarristerUpdateParams(BarristerRepresentationUpdateBuilder.BarristerUpdateParams params,
                                             FinremCaseData caseData, Set<Barrister> barristers) {
        assertThat(params.caseData()).isEqualTo(caseData);
        assertThat(params.authToken()).isEqualTo(AUTH_TOKEN);
        assertThat(params.barristerParty()).isEqualTo(BarristerParty.APPLICANT);
        assertThat(params.barrister()).isIn(barristers);
    }
}
