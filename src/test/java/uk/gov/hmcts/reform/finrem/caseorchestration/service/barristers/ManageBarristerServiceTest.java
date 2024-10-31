package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.NoSuchUserException;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.BarristerUpdateDifferenceCalculator;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChangeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerLetterTuple;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_BARRISTER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_BARRISTER_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASEWORKER_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER_BARRISTER_1_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER_BARRISTER_2_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER_BARRISTER_3_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER_BARRISTER_4_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTVR_SOLICITOR_1_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTVR_SOLICITOR_2_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTVR_SOLICITOR_3_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTVR_SOLICITOR_4_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANAGE_BARRISTERS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANAGE_BARRISTER_PARTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REPRESENTATION_UPDATE_HISTORY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_BARRISTER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdServiceTest.AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class ManageBarristerServiceTest {

    private static final long CASE_ID = 1583841721773828L;
    private static final String APP_BARRISTER_EMAIL_ONE = "applicantbarristerone@gmail.com";
    private static final String APP_BARRISTER_EMAIL_TWO = "applicantbarristertwo@gmail.com";
    private static final String BARRISTER_USER_ID = "barristerUserId";
    private static final String APP_SOLICITOR = "App Solicitor";
    private static final String CASEWORKER_NAME = "The Caseworker";
    private static final String SYS_USER_TOKEN = "sysUserToken";
    private static final String CLIENT_NAME = "Client Name";
    private static final String SOME_ORG_ID = "someOrgId";
    static final String CASEWORKER_POLICY = "[CASEWORKER]";
    private static final Barrister DEFAULT_BARRISTER = Barrister.builder()
        .email("someEmail")
        .build();

    @Mock
    private BarristerUpdateDifferenceCalculator barristerUpdateDifferenceCalculator;
    @Mock
    private AssignCaseAccessService assignCaseAccessService;
    @Mock
    private PrdOrganisationService organisationService;
    @Mock
    private IdamService idamService;
    @Mock
    private CaseDataService caseDataService;
    @Mock
    private CaseAssignedRoleService caseAssignedRoleService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private BarristerLetterService barristerLetterService;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    ManageBarristerService manageBarristerService;

    private CaseDetails caseDetails;

    @BeforeEach
    void setUp() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseDetails = CaseDetails.builder().id(CASE_ID).data(caseData).build();
    }

    @Test
    void givenValidData_whenGetBarristersForPartyApplicant_thenReturnApplicantBarristerData() {
        List<BarristerData> applicantBarristers = applicantBarristerCollection();
        caseDetails.getData().put(CASE_ROLE, APP_SOLICITOR_POLICY);
        caseDetails.getData().put(APPLICANT_BARRISTER_COLLECTION, applicantBarristers);
        when(caseAssignedRoleService.getCaseAssignedUserRole(caseDetails.getId().toString(), AUTH_TOKEN))
            .thenReturn(buildCaseAssignedUserRolesResource(APP_SOLICITOR_POLICY));

        List<BarristerData> barristerData = manageBarristerService.getBarristersForParty(caseDetails, AUTH_TOKEN);

        assertThat(barristerData, is(applicantBarristers));
    }

    @Test
    void givenValidData_whenGetBarristersForPartyIntervener1_thenReturnIntervener1BarristerData() {
        List<BarristerData> intervener1Barristers = intervener1BarristerCollection();
        caseDetails.getData().put(CASE_ROLE, INTVR_SOLICITOR_1_POLICY);
        caseDetails.getData().put(INTERVENER_BARRISTER_1_COLLECTION, intervener1Barristers);
        when(caseAssignedRoleService.getCaseAssignedUserRole(caseDetails.getId().toString(), AUTH_TOKEN))
            .thenReturn(buildCaseAssignedUserRolesResource(INTVR_SOLICITOR_1_POLICY));

        List<BarristerData> barristerData = manageBarristerService.getBarristersForParty(caseDetails, AUTH_TOKEN);

        assertThat(barristerData, is(intervener1Barristers));
    }

    @Test
    void givenValidData_whenGetBarristersForPartyIntervener2_thenReturnIntervener2BarristerData() {
        List<BarristerData> intervener2Barristers = intervener2BarristerCollection();
        caseDetails.getData().put(CASE_ROLE, INTVR_SOLICITOR_2_POLICY);
        caseDetails.getData().put(INTERVENER_BARRISTER_2_COLLECTION, intervener2Barristers);
        when(caseAssignedRoleService.getCaseAssignedUserRole(caseDetails.getId().toString(), AUTH_TOKEN))
            .thenReturn(buildCaseAssignedUserRolesResource(INTVR_SOLICITOR_2_POLICY));

        List<BarristerData> barristerData = manageBarristerService.getBarristersForParty(caseDetails, AUTH_TOKEN);

        assertThat(barristerData, is(intervener2Barristers));
    }

    @Test
    void givenValidData_whenGetBarristersForPartyIntervener3_thenReturnIntervener3BarristerData() {
        List<BarristerData> intervener3Barristers = intervener3BarristerCollection();
        caseDetails.getData().put(CASE_ROLE, INTVR_SOLICITOR_3_POLICY);
        caseDetails.getData().put(INTERVENER_BARRISTER_3_COLLECTION, intervener3Barristers);
        when(caseAssignedRoleService.getCaseAssignedUserRole(caseDetails.getId().toString(), AUTH_TOKEN))
            .thenReturn(buildCaseAssignedUserRolesResource(INTVR_SOLICITOR_3_POLICY));

        List<BarristerData> barristerData = manageBarristerService.getBarristersForParty(caseDetails, AUTH_TOKEN);

        assertThat(barristerData, is(intervener3Barristers));
    }

    @Test
    void givenValidData_whenGetBarristersForPartyIntervener4_thenReturnIntervener4BarristerData() {
        List<BarristerData> intervener4Barristers = intervener4BarristerCollection();
        caseDetails.getData().put(CASE_ROLE, INTVR_SOLICITOR_4_POLICY);
        caseDetails.getData().put(INTERVENER_BARRISTER_4_COLLECTION, intervener4Barristers);
        when(caseAssignedRoleService.getCaseAssignedUserRole(caseDetails.getId().toString(), AUTH_TOKEN))
            .thenReturn(buildCaseAssignedUserRolesResource(INTVR_SOLICITOR_4_POLICY));

        List<BarristerData> barristerData = manageBarristerService.getBarristersForParty(caseDetails, AUTH_TOKEN);

        assertThat(barristerData, is(intervener4Barristers));
    }


    @Test
    void givenNoCurrentBarristers_whenGetBarristersForPartyApplicant_thenReturnEmptyList() {
        caseDetails.getData().put(CASE_ROLE, APP_SOLICITOR_POLICY);
        caseDetails.getData().put(RESPONDENT_BARRISTER_COLLECTION, respondentBarristerCollection());
        when(caseAssignedRoleService.getCaseAssignedUserRole(caseDetails.getId().toString(), AUTH_TOKEN))
            .thenReturn(buildCaseAssignedUserRolesResource(APP_SOLICITOR_POLICY));

        List<BarristerData> barristerData = manageBarristerService.getBarristersForParty(caseDetails, AUTH_TOKEN);

        assertThat(barristerData, is(empty()));
    }

    @Test
    void givenNoCurrentBarristers_whenGetBarristersForPartyIntervener1_thenReturnEmptyList() {
        caseDetails.getData().put(CASE_ROLE, INTVR_SOLICITOR_1_POLICY);
        caseDetails.getData().put(RESPONDENT_BARRISTER_COLLECTION, respondentBarristerCollection());
        when(caseAssignedRoleService.getCaseAssignedUserRole(caseDetails.getId().toString(), AUTH_TOKEN))
            .thenReturn(buildCaseAssignedUserRolesResource(INTVR_SOLICITOR_1_POLICY));

        List<BarristerData> barristerData = manageBarristerService.getBarristersForParty(caseDetails, AUTH_TOKEN);

        assertThat(barristerData, is(empty()));
    }

    @Test
    void givenNoCurrentBarristers_whenGetBarristersForPartyIntervener2_thenReturnEmptyList() {
        caseDetails.getData().put(CASE_ROLE, INTVR_SOLICITOR_2_POLICY);
        caseDetails.getData().put(RESPONDENT_BARRISTER_COLLECTION, respondentBarristerCollection());
        when(caseAssignedRoleService.getCaseAssignedUserRole(caseDetails.getId().toString(), AUTH_TOKEN))
            .thenReturn(buildCaseAssignedUserRolesResource(INTVR_SOLICITOR_2_POLICY));

        List<BarristerData> barristerData = manageBarristerService.getBarristersForParty(caseDetails, AUTH_TOKEN);

        assertThat(barristerData, is(empty()));
    }

    @Test
    void givenNoCurrentBarristers_whenGetBarristersForPartyIntervener3_thenReturnEmptyList() {
        caseDetails.getData().put(CASE_ROLE, INTVR_SOLICITOR_3_POLICY);
        caseDetails.getData().put(RESPONDENT_BARRISTER_COLLECTION, respondentBarristerCollection());
        when(caseAssignedRoleService.getCaseAssignedUserRole(caseDetails.getId().toString(), AUTH_TOKEN))
            .thenReturn(buildCaseAssignedUserRolesResource(INTVR_SOLICITOR_3_POLICY));

        List<BarristerData> barristerData = manageBarristerService.getBarristersForParty(caseDetails, AUTH_TOKEN);

        assertThat(barristerData, is(empty()));
    }

    @Test
    void givenNoCurrentBarristers_whenGetBarristersForPartyIntervener4_thenReturnEmptyList() {
        caseDetails.getData().put(CASE_ROLE, INTVR_SOLICITOR_4_POLICY);
        caseDetails.getData().put(RESPONDENT_BARRISTER_COLLECTION, respondentBarristerCollection());
        when(caseAssignedRoleService.getCaseAssignedUserRole(caseDetails.getId().toString(), AUTH_TOKEN))
            .thenReturn(buildCaseAssignedUserRolesResource(INTVR_SOLICITOR_4_POLICY));

        List<BarristerData> barristerData = manageBarristerService.getBarristersForParty(caseDetails, AUTH_TOKEN);

        assertThat(barristerData, is(empty()));
    }

    @Test
    void givenValidData_whenGetBarristersForPartyRespondent_thenReturnRespondentBarristerData() {
        List<BarristerData> respondentBarristers = respondentBarristerCollection();
        caseDetails.getData().put(CASE_ROLE, RESP_SOLICITOR_POLICY);
        caseDetails.getData().put(RESPONDENT_BARRISTER_COLLECTION, respondentBarristers);
        when(caseAssignedRoleService.getCaseAssignedUserRole(caseDetails.getId().toString(), AUTH_TOKEN))
            .thenReturn(buildCaseAssignedUserRolesResource(RESP_SOLICITOR_POLICY));

        List<BarristerData> barristerData = manageBarristerService.getBarristersForParty(caseDetails, AUTH_TOKEN);

        assertThat(barristerData, is(respondentBarristers));
    }

    @Test
    void givenCaseworkerUsers_whenGetBarristersForPartyApplicant_thenReturnApplicantBarristerData() {
        List<BarristerData> applicantBarristers = applicantBarristerCollection();
        caseDetails.getData().put(CASE_ROLE, CASEWORKER_ROLE);
        caseDetails.getData().put(MANAGE_BARRISTER_PARTY, APPLICANT);
        caseDetails.getData().put(APPLICANT_BARRISTER_COLLECTION, applicantBarristers);
        when(caseAssignedRoleService.getCaseAssignedUserRole(caseDetails.getId().toString(), AUTH_TOKEN))
            .thenReturn(CaseAssignedUserRolesResource.builder().build());

        List<BarristerData> barristerData = manageBarristerService.getBarristersForParty(caseDetails, AUTH_TOKEN);

        assertThat(barristerData, is(applicantBarristers));
    }

    @Test
    void givenCaseworkerUsers_whenGetBarristersForPartyIntervener1_thenReturnIntervener1BarristerData() {
        List<BarristerData> intervener1Barristers = intervener1BarristerCollection();
        caseDetails.getData().put(CASE_ROLE, CASEWORKER_ROLE);
        caseDetails.getData().put(MANAGE_BARRISTER_PARTY, INTERVENER1);
        caseDetails.getData().put(INTERVENER_BARRISTER_1_COLLECTION, intervener1Barristers);
        when(caseAssignedRoleService.getCaseAssignedUserRole(caseDetails.getId().toString(), AUTH_TOKEN))
            .thenReturn(CaseAssignedUserRolesResource.builder().build());

        List<BarristerData> barristerData = manageBarristerService.getBarristersForParty(caseDetails, AUTH_TOKEN);

        assertThat(barristerData, is(intervener1Barristers));
    }

    @Test
    void givenCaseworkerUsers_whenGetBarristersForPartyIntervener2_thenReturnIntervener2BarristerData() {
        List<BarristerData> intervener2Barristers = intervener2BarristerCollection();
        caseDetails.getData().put(CASE_ROLE, CASEWORKER_ROLE);
        caseDetails.getData().put(MANAGE_BARRISTER_PARTY, INTERVENER2);
        caseDetails.getData().put(INTERVENER_BARRISTER_2_COLLECTION, intervener2Barristers);
        when(caseAssignedRoleService.getCaseAssignedUserRole(caseDetails.getId().toString(), AUTH_TOKEN))
            .thenReturn(CaseAssignedUserRolesResource.builder().build());

        List<BarristerData> barristerData = manageBarristerService.getBarristersForParty(caseDetails, AUTH_TOKEN);

        assertThat(barristerData, is(intervener2Barristers));
    }

    @Test
    void givenCaseworkerUsers_whenGetBarristersForPartyIntervener3_thenReturnIntervener3BarristerData() {
        List<BarristerData> intervener3Barristers = intervener3BarristerCollection();
        caseDetails.getData().put(CASE_ROLE, CASEWORKER_ROLE);
        caseDetails.getData().put(MANAGE_BARRISTER_PARTY, INTERVENER3);
        caseDetails.getData().put(INTERVENER_BARRISTER_3_COLLECTION, intervener3Barristers);
        when(caseAssignedRoleService.getCaseAssignedUserRole(caseDetails.getId().toString(), AUTH_TOKEN))
            .thenReturn(CaseAssignedUserRolesResource.builder().build());

        List<BarristerData> barristerData = manageBarristerService.getBarristersForParty(caseDetails, AUTH_TOKEN);

        assertThat(barristerData, is(intervener3Barristers));
    }

    @Test
    void givenCaseworkerUsers_whenGetBarristersForPartyIntervener4_thenReturnIntervener4BarristerData() {
        List<BarristerData> intervener4Barristers = intervener4BarristerCollection();
        caseDetails.getData().put(CASE_ROLE, CASEWORKER_ROLE);
        caseDetails.getData().put(MANAGE_BARRISTER_PARTY, INTERVENER4);
        caseDetails.getData().put(INTERVENER_BARRISTER_4_COLLECTION, intervener4Barristers);
        when(caseAssignedRoleService.getCaseAssignedUserRole(caseDetails.getId().toString(), AUTH_TOKEN))
            .thenReturn(CaseAssignedUserRolesResource.builder().build());

        List<BarristerData> barristerData = manageBarristerService.getBarristersForParty(caseDetails, AUTH_TOKEN);

        assertThat(barristerData, is(intervener4Barristers));
    }

    @Test
    void givenValidData_whenUpdateBarristerAccess_thenGrantAccessAndGenerateRepresentationUpdateData() {
        caseDetails.getData().put(CASE_ROLE, APP_SOLICITOR_POLICY);
        when(barristerUpdateDifferenceCalculator.calculate(any(), any())).thenReturn(buildBarristerChange());
        when(organisationService.findUserByEmail(APP_BARRISTER_EMAIL_ONE, AUTH_TOKEN)).thenReturn(Optional.of(BARRISTER_USER_ID));
        when(organisationService.findUserByEmail(APP_BARRISTER_EMAIL_TWO, AUTH_TOKEN)).thenReturn(Optional.of(BARRISTER_USER_ID));
        when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn(APP_SOLICITOR);
        when(caseDataService.buildFullApplicantName((CaseDetails) any())).thenReturn(CLIENT_NAME);
        when(caseAssignedRoleService.getCaseAssignedUserRole(caseDetails.getId().toString(), AUTH_TOKEN))
            .thenReturn(buildCaseAssignedUserRolesResource(APP_SOLICITOR_POLICY));

        Map<String, Object> caseData = manageBarristerService.updateBarristerAccess(caseDetails,
            List.of(DEFAULT_BARRISTER),
            Collections.emptyList(), AUTH_TOKEN);

        List<Element<RepresentationUpdate>> representationUpdateHistory =
            objectMapper.convertValue(caseData.get(REPRESENTATION_UPDATE_HISTORY), new TypeReference<>() {});
        RepresentationUpdate update = representationUpdateHistory.get(0).getValue();
        assertThat(update.getBy(), is(APP_SOLICITOR));
        assertThat(update.getAdded().getEmail(), is(APP_BARRISTER_EMAIL_ONE));
        assertThat(update.getParty(), is(APPLICANT));
        assertThat(update.getVia(), is(MANAGE_BARRISTERS));
        assertThat(update.getClientName(), is(CLIENT_NAME));

        verify(assignCaseAccessService).grantCaseRoleToUser(caseDetails.getId(), BARRISTER_USER_ID,
            APPLICANT_BARRISTER_ROLE, SOME_ORG_ID);
    }

    @Test
    void givenValidData_whenUpdateBarristerAccessAsCaseworker_thenGrantAccessAndGenerateRepresentationUpdateData() {
        caseDetails.getData().put(CASE_ROLE, CASEWORKER_ROLE);
        caseDetails.getData().put(MANAGE_BARRISTER_PARTY, APPLICANT);
        when(barristerUpdateDifferenceCalculator.calculate(any(), any())).thenReturn(buildBarristerChange());
        when(organisationService.findUserByEmail(APP_BARRISTER_EMAIL_ONE, SYS_USER_TOKEN)).thenReturn(Optional.of(BARRISTER_USER_ID));
        when(organisationService.findUserByEmail(APP_BARRISTER_EMAIL_TWO, SYS_USER_TOKEN)).thenReturn(Optional.of(BARRISTER_USER_ID));
        when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn(CASEWORKER_NAME);
        when(caseDataService.buildFullApplicantName((CaseDetails) any())).thenReturn(CLIENT_NAME);
        when(systemUserService.getSysUserToken()).thenReturn(SYS_USER_TOKEN);
        when(caseAssignedRoleService.getCaseAssignedUserRole(caseDetails.getId().toString(), AUTH_TOKEN))
            .thenReturn(buildCaseAssignedUserRolesResource(APP_SOLICITOR_POLICY));

        Map<String, Object> caseData = manageBarristerService.updateBarristerAccess(caseDetails,
            List.of(DEFAULT_BARRISTER),
            Collections.emptyList(), AUTH_TOKEN);

        List<Element<RepresentationUpdate>> representationUpdateHistory =
            objectMapper.convertValue(caseData.get(REPRESENTATION_UPDATE_HISTORY), new TypeReference<>() {});
        RepresentationUpdate update = representationUpdateHistory.get(0).getValue();
        assertThat(update.getBy(), is(CASEWORKER_NAME));
        assertThat(update.getAdded().getEmail(), is(APP_BARRISTER_EMAIL_ONE));
        assertThat(update.getParty(), is(APPLICANT));
        assertThat(update.getVia(), is(MANAGE_BARRISTERS));
        assertThat(update.getClientName(), is(CLIENT_NAME));

        verify(assignCaseAccessService).grantCaseRoleToUser(caseDetails.getId(), BARRISTER_USER_ID,
            APPLICANT_BARRISTER_ROLE, SOME_ORG_ID);
        verify(organisationService).findUserByEmail(APP_BARRISTER_EMAIL_ONE, SYS_USER_TOKEN);
    }

    @Test
    void givenNoUserFound_whenUpdateBarristerAccess_thenThrowError() {
        caseDetails.getData().put(CASE_ROLE, APP_SOLICITOR_POLICY);
        when(barristerUpdateDifferenceCalculator.calculate(any(), any())).thenReturn(buildBarristerChange());
        when(caseAssignedRoleService.getCaseAssignedUserRole(caseDetails.getId().toString(), AUTH_TOKEN)).thenReturn(
            buildCaseAssignedUserRolesResource(APP_SOLICITOR_POLICY));
        when(organisationService.findUserByEmail(APP_BARRISTER_EMAIL_ONE, AUTH_TOKEN)).thenReturn(Optional.empty());

        try {
            manageBarristerService.updateBarristerAccess(caseDetails,
                    List.of(DEFAULT_BARRISTER),
                    Collections.emptyList(), AUTH_TOKEN);
        } catch (NoSuchUserException nue) {
            String expectedMessage = "Could not find barrister with provided email";
            assertEquals(expectedMessage, nue.getMessage());
        }
    }

    @Test
    void givenValidData_whenNotifyBarristerAccess_sendBarristerNotification() {
        caseDetails.getData().put(CASE_ROLE, APP_SOLICITOR_POLICY);
        BarristerChange barristerChange = buildBarristerChange();
        when(barristerUpdateDifferenceCalculator.calculate(any(), any())).thenReturn(barristerChange);
        when(caseAssignedRoleService.getCaseAssignedUserRole(caseDetails.getId().toString(), AUTH_TOKEN))
            .thenReturn(buildCaseAssignedUserRolesResource(APP_SOLICITOR_POLICY));

        manageBarristerService.notifyBarristerAccess(caseDetails,
            List.of(DEFAULT_BARRISTER),
            List.of(DEFAULT_BARRISTER),
            AUTH_TOKEN);

        Barrister expectedAdded = barristerChange.getAdded().stream().toList().get(0);
        Barrister expectedRemoved = barristerChange.getRemoved().stream().toList().get(0);

        verify(notificationService).sendBarristerAddedEmail(caseDetails, expectedAdded);
        verify(notificationService).sendBarristerRemovedEmail(caseDetails, expectedRemoved);

        BarristerLetterTuple addedTuple = BarristerLetterTuple
            .of(DocumentHelper.PaperNotificationRecipient.APPLICANT, AUTH_TOKEN, BarristerChangeType.ADDED);
        BarristerLetterTuple removedTuple = BarristerLetterTuple
            .of(DocumentHelper.PaperNotificationRecipient.APPLICANT, AUTH_TOKEN, BarristerChangeType.REMOVED);

        verify(barristerLetterService).sendBarristerLetter(caseDetails, expectedAdded, addedTuple, AUTH_TOKEN);
        verify(barristerLetterService).sendBarristerLetter(caseDetails, expectedRemoved, removedTuple, AUTH_TOKEN);
    }

    @Test
    void givenValidData_whenUpdateBarristerAccess_thenRemoveAccessAndGenerateRepresentationUpdateData() {
        caseDetails.getData().put(CASE_ROLE, APP_SOLICITOR_POLICY);
        when(barristerUpdateDifferenceCalculator.calculate(any(), any())).thenReturn(buildRemovedBarristerChange());
        when(organisationService.findUserByEmail(APP_BARRISTER_EMAIL_ONE, AUTH_TOKEN)).thenReturn(Optional.of(BARRISTER_USER_ID));
        when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn(APP_SOLICITOR);
        when(caseDataService.buildFullApplicantName((CaseDetails) any())).thenReturn(CLIENT_NAME);
        when(caseAssignedRoleService.getCaseAssignedUserRole(caseDetails.getId().toString(), AUTH_TOKEN))
            .thenReturn(buildCaseAssignedUserRolesResource(APP_SOLICITOR_POLICY));

        Map<String, Object> caseData = manageBarristerService.updateBarristerAccess(caseDetails,
            List.of(DEFAULT_BARRISTER),
            Collections.emptyList(), AUTH_TOKEN);

        List<Element<RepresentationUpdate>> representationUpdateHistory =
            objectMapper.convertValue(caseData.get(REPRESENTATION_UPDATE_HISTORY), new TypeReference<>() {});
        RepresentationUpdate update = representationUpdateHistory.get(0).getValue();
        assertThat(update.getBy(), is(APP_SOLICITOR));
        assertThat(update.getRemoved().getEmail(), is(APP_BARRISTER_EMAIL_ONE));
        assertThat(update.getParty(), is(APPLICANT));
        assertThat(update.getVia(), is(MANAGE_BARRISTERS));
        assertThat(update.getClientName(), is(CLIENT_NAME));

        verify(assignCaseAccessService).removeCaseRoleToUser(caseDetails.getId(), BARRISTER_USER_ID,
            APPLICANT_BARRISTER_ROLE, SOME_ORG_ID);
    }

    private List<BarristerData> applicantBarristerCollection() {
        return List.of(
            BarristerData.builder()
                .id(UUID.randomUUID().toString())
                .barrister(Barrister.builder()
                    .email(APP_BARRISTER_EMAIL_ONE)
                    .build())
                .build(),
            BarristerData.builder()
                .id(UUID.randomUUID().toString())
                .barrister(Barrister.builder()
                    .email(APP_BARRISTER_EMAIL_TWO)
                    .build())
                .build()
        );
    }

    private List<BarristerData> respondentBarristerCollection() {
        return List.of(
            BarristerData.builder()
                .id(UUID.randomUUID().toString())
                .barrister(Barrister.builder()
                    .email("respbarristerone@gmail.com")
                    .build())
                .build(),
            BarristerData.builder()
                .id(UUID.randomUUID().toString())
                .barrister(Barrister.builder()
                    .email("respbarristertwo@gmail.com")
                    .build())
                .build()
        );
    }

    private List<BarristerData> intervener1BarristerCollection() {
        return List.of(
            BarristerData.builder()
                .id(UUID.randomUUID().toString())
                .barrister(Barrister.builder()
                    .email("intvr1barristerone@gmail.com")
                    .build())
                .build(),
            BarristerData.builder()
                .id(UUID.randomUUID().toString())
                .barrister(Barrister.builder()
                    .email("intvr1barristertwo@gmail.com")
                    .build())
                .build()
        );
    }

    private List<BarristerData> intervener2BarristerCollection() {
        return List.of(
            BarristerData.builder()
                .id(UUID.randomUUID().toString())
                .barrister(Barrister.builder()
                    .email("intvr2barristerone@gmail.com")
                    .build())
                .build(),
            BarristerData.builder()
                .id(UUID.randomUUID().toString())
                .barrister(Barrister.builder()
                    .email("intvr2barristertwo@gmail.com")
                    .build())
                .build()
        );
    }

    private List<BarristerData> intervener3BarristerCollection() {
        return List.of(
            BarristerData.builder()
                .id(UUID.randomUUID().toString())
                .barrister(Barrister.builder()
                    .email("intvr3barristerone@gmail.com")
                    .build())
                .build(),
            BarristerData.builder()
                .id(UUID.randomUUID().toString())
                .barrister(Barrister.builder()
                    .email("intvr3barristertwo@gmail.com")
                    .build())
                .build()
        );
    }

    private List<BarristerData> intervener4BarristerCollection() {
        return List.of(
            BarristerData.builder()
                .id(UUID.randomUUID().toString())
                .barrister(Barrister.builder()
                    .email("intvr4barristerone@gmail.com")
                    .build())
                .build(),
            BarristerData.builder()
                .id(UUID.randomUUID().toString())
                .barrister(Barrister.builder()
                    .email("intvr4barristertwo@gmail.com")
                    .build())
                .build()
        );
    }

    private BarristerChange buildBarristerChange() {
        return BarristerChange.builder()
            .added(Set.of(Barrister.builder()
                    .email(APP_BARRISTER_EMAIL_ONE)
                    .organisation(Organisation.builder().organisationID(SOME_ORG_ID).build())
                .build()))
            .removed(Set.of(Barrister.builder()
                .email(APP_BARRISTER_EMAIL_TWO)
                .organisation(Organisation.builder().organisationID(SOME_ORG_ID).build())
                .build()))
            .build();
    }

    private BarristerChange buildRemovedBarristerChange() {
        return BarristerChange.builder()
            .removed(Set.of(Barrister.builder()
                .email(APP_BARRISTER_EMAIL_ONE)
                .organisation(Organisation.builder().organisationID(SOME_ORG_ID).build())
                .build()))
            .build();
    }

    private CaseAssignedUserRolesResource buildCaseAssignedUserRolesResource(String role) {
        return CaseAssignedUserRolesResource.builder()
            .caseAssignedUserRoles(List.of(CaseAssignedUserRole.builder()
                .caseRole(role)
                .build()))
            .build();
    }
}