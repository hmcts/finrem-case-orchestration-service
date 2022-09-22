package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.BarristerUpdateDifferenceCalculator;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ChangeOfRepresentationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_BARRISTER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_BARRISTER_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REPRESENTATION_UPDATE_HISTORY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_BARRISTER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element.element;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdServiceTest.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService.MANAGE_BARRISTERS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService.MANAGE_BARRISTER_PARTY;

@RunWith(MockitoJUnitRunner.class)
public class ManageBarristerServiceTest {

    private static final long CASE_ID = 1583841721773828L;
    private static final String CASEWORKER_ROLE = "[CASEWORKER]";
    private static final String APP_BARRISTER_EMAIL_ONE = "applicantbarristerone@gmail.com";
    private static final String APP_BARRISTER_EMAIL_TWO = "applicantbarristertwo@gmail.com";
    private static final String BARRISTER_USER_ID = "barristerUserId";
    private static final String APP_SOLICITOR = "App Solicitor";
    private static final Barrister DEFAULT_BARRISTER = Barrister.builder()
        .email("someEmail")
        .build();
    private static final String CLIENT_NAME = "Client Name";
    private static final String SOME_ORG_ID = "someOrgId";

    @Mock
    private BarristerUpdateDifferenceCalculator barristerUpdateDifferenceCalculator;
    @Mock
    private ChangeOfRepresentationService changeOfRepresentationService;
    @Mock
    private AssignCaseAccessService assignCaseAccessService;
    @Mock
    private PrdOrganisationService organisationService;
    @Mock
    private IdamService idamService;
    @Mock
    private CaseDataService caseDataService;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    ManageBarristerService manageBarristerService;

    @Captor
    ArgumentCaptor<ChangeOfRepresentationRequest> changeOfRepresentationRequestCaptor;

    private CaseDetails caseDetails;

    @Before
    public void setUp() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseDetails = CaseDetails.builder().id(CASE_ID).data(caseData).build();
    }

    @Test
    public void givenValidData_whenGetBarristersForPartyApplicant_thenReturnApplicantBarristerData() {
        List<BarristerData> applicantBarristers = applicantBarristerCollection();
        caseDetails.getData().put(CASE_ROLE, APP_SOLICITOR_POLICY);
        caseDetails.getData().put(APPLICANT_BARRISTER_COLLECTION, applicantBarristers);

        List<BarristerData> barristerData = manageBarristerService.getBarristersForParty(caseDetails);

        assertThat(barristerData, is(applicantBarristers));
    }

    @Test
    public void givenNoCurrentBarristers_whenGetBarristersForPartyApplicant_thenReturnEmptyList() {
        caseDetails.getData().put(CASE_ROLE, APP_SOLICITOR_POLICY);
        caseDetails.getData().put(RESPONDENT_BARRISTER_COLLECTION, respondentBarristerCollection());

        List<BarristerData> barristerData = manageBarristerService.getBarristersForParty(caseDetails);

        assertThat(barristerData, is(empty()));
    }

    @Test
    public void givenValidData_whenGetBarristersForPartyRespondent_thenReturnRespondentBarristerData() {
        List<BarristerData> respondentBarristers = respondentBarristerCollection();
        caseDetails.getData().put(CASE_ROLE, RESP_SOLICITOR_POLICY);
        caseDetails.getData().put(RESPONDENT_BARRISTER_COLLECTION, respondentBarristers);

        List<BarristerData> barristerData = manageBarristerService.getBarristersForParty(caseDetails);

        assertThat(barristerData, is(respondentBarristers));
    }

    @Test
    public void givenCaseworkerUsers_whenGetBarristersForPartyApplicant_thenReturnApplicantBarristerData() {
        List<BarristerData> applicantBarristers = applicantBarristerCollection();
        caseDetails.getData().put(CASE_ROLE, CASEWORKER_ROLE);
        caseDetails.getData().put(MANAGE_BARRISTER_PARTY, APPLICANT);
        caseDetails.getData().put(APPLICANT_BARRISTER_COLLECTION, applicantBarristers);

        List<BarristerData> barristerData = manageBarristerService.getBarristersForParty(caseDetails);

        assertThat(barristerData, is(applicantBarristers));
    }

    @Test
    public void givenValidData_whenUpdateBarristerAccess_thenGrantAccessAndGenerateRepresentationUpdateData() {
        caseDetails.getData().put(CASE_ROLE, APP_SOLICITOR_POLICY);
        when(barristerUpdateDifferenceCalculator.calculate(any(), any())).thenReturn(buildAddedBarristerChange());
        when(organisationService.findUserByEmail(APP_BARRISTER_EMAIL_ONE, AUTH_TOKEN)).thenReturn(Optional.of(BARRISTER_USER_ID));
        when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn(APP_SOLICITOR);
        when(caseDataService.buildFullApplicantName(any())).thenReturn(CLIENT_NAME);
        when(changeOfRepresentationService.generateRepresentationUpdateHistory(any()))
            .thenReturn(buildRepresentationUpdateHistory());

        Map<String, Object> caseData = manageBarristerService.updateBarristerAccess(caseDetails,
            List.of(DEFAULT_BARRISTER),
            List.of(DEFAULT_BARRISTER), AUTH_TOKEN);

        List<Element<RepresentationUpdate>> representationUpdateHistory =
            objectMapper.convertValue(caseData.get(REPRESENTATION_UPDATE_HISTORY), new TypeReference<>() {});
        RepresentationUpdate update = representationUpdateHistory.get(0).getValue();
        assertThat(update.getBy(), is(APP_SOLICITOR));
        assertThat(update.getAdded().getEmail(), is(APP_BARRISTER_EMAIL_ONE));
        assertThat(update.getParty(), is(APPLICANT));

        verify(assignCaseAccessService).grantCaseRoleToUser(caseDetails.getId(), BARRISTER_USER_ID,
            APPLICANT_BARRISTER_ROLE, SOME_ORG_ID);
        verify(changeOfRepresentationService).generateRepresentationUpdateHistory(changeOfRepresentationRequestCaptor.capture());

        ChangeOfRepresentationRequest representationRequest = changeOfRepresentationRequestCaptor.getValue();

        assertChangeOfRepresentationRequest(representationRequest,
            APP_BARRISTER_EMAIL_ONE,
            APP_SOLICITOR,
            APPLICANT,
            CLIENT_NAME);
    }

    @Test
    public void givenNoUserFound_whenUpdateBarristerAccess_thenThrowError() {
        caseDetails.getData().put(CASE_ROLE, APP_SOLICITOR_POLICY);
        when(barristerUpdateDifferenceCalculator.calculate(any(), any())).thenReturn(buildAddedBarristerChange());
        when(organisationService.findUserByEmail(APP_BARRISTER_EMAIL_ONE, AUTH_TOKEN)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            manageBarristerService.updateBarristerAccess(caseDetails,
                List.of(DEFAULT_BARRISTER),
                List.of(DEFAULT_BARRISTER), AUTH_TOKEN));

        String expectedMessage = "Could not find the user with email " + APP_BARRISTER_EMAIL_ONE;
        String actual = exception.getMessage();
        assertEquals(expectedMessage, actual);
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

    private BarristerChange buildAddedBarristerChange() {
        return BarristerChange.builder()
            .added(Set.of(Barrister.builder()
                    .email(APP_BARRISTER_EMAIL_ONE)
                    .organisation(Organisation.builder().organisationID(SOME_ORG_ID).build())
                .build()))
            .build();
    }

    private RepresentationUpdateHistory buildRepresentationUpdateHistory() {
        return RepresentationUpdateHistory.builder()
            .representationUpdateHistory(List.of(element(UUID.randomUUID(),
                RepresentationUpdate.builder()
                    .added(ChangedRepresentative.builder()
                        .email(APP_BARRISTER_EMAIL_ONE)
                        .build())
                    .by(APP_SOLICITOR)
                    .via(MANAGE_BARRISTERS)
                    .party(APPLICANT)
                    .clientName("The Applicant")
                    .build()
            )))
            .build();
    }

    private void assertChangeOfRepresentationRequest(ChangeOfRepresentationRequest request,
                                                     String email,
                                                     String by,
                                                     String party,
                                                     String client) {
        assertThat(request.getAddedRepresentative().getEmail(), is(email));
        assertThat(request.getBy(), is(by));
        assertThat(request.getParty(), is(party));
        assertThat(request.getClientName(), is(client));
    }
}