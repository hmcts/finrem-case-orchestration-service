package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;

public class NoticeOfChangeServiceTest extends BaseServiceTest {
    private static final String PATH = "/fixtures/noticeOfChange/caseworkerNoc/";
    private static final String CHANGE_OF_REPRESENTATIVES = "ChangeOfRepresentatives";
    private static final String TEST_CASE_ID = "123456";
    private static final String TEST_USER_ID = "testUserId";

    @Autowired
    private NoticeOfChangeService noticeOfChangeService;

    @MockBean
    private CaseDataService mockCaseDataService;

    @MockBean private IdamService mockIdamService;

    @MockBean private AssignCaseAccessService mockAssignCaseAccessService;

    private CallbackRequest callbackRequest;

    private final Function<Map<String, Object>, List<Element<ChangeOfRepresentation>>> getFirstChangeElement =
        this::convertToChangeOfRepresentation;

    @Before
    public void setUp() {
        mapper.registerModule(new JavaTimeModule());
    }

    private void setUpCaseDetails(String fileName) throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream(PATH + fileName)) {
            callbackRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
        }
    }

    private List<Element<ChangeOfRepresentation>> convertToChangeOfRepresentation(Map<String, Object> data) {
        return mapper.convertValue(data.get(CHANGE_OF_REPRESENTATIVES),
            new TypeReference<>() {});
    }

    @Test
    public void shouldUpdateRepresentationAndGenerateChangeOfRepresentatives_whenCaseDataIsValid() throws Exception {
        setUpCaseDetails("change-of-representatives.json");

        setUpHelper();

        try (InputStream resourceAsStream = getClass()
            .getResourceAsStream(PATH + "change-of-representatives-before.json")) {

            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
            InputStream is = getClass().getResourceAsStream(PATH + "change-of-representatives-original-data.json");
            CaseDetails originalDetails = mapper.readValue(is, CallbackRequest.class).getCaseDetails();

            when(mockAssignCaseAccessService.getUsersWithAccess(APP_SOLICITOR_POLICY)).thenReturn(
                CaseAssignmentUserRolesResource.builder()
                    .caseAssignmentUserRoles(List.of(
                        CaseAssignmentUserRole.builder().caseRole(APP_SOLICITOR_POLICY)
                            .caseDataId(TEST_CASE_ID)
                            .userId(TEST_USER_ID)
                            .caseRole(APP_SOLICITOR_POLICY)
                            .build()))
                    .build());

            Map<String, Object> caseData = noticeOfChangeService.caseWorkerUpdatesRepresentation(actualRequest.getCaseDetails(),
                authTokenGenerator.generate(),
                originalDetails);

            ChangeOfRepresentation actualChange = getFirstChangeElement.apply(caseData).get(0).getValue();
            ChangeOfRepresentation expectedChange = getFirstChangeElement.apply(callbackRequest.getCaseDetails()
                .getData()).get(0).getValue();

            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty()).isEqualTo(expectedChange.getParty());
            assertThat(actualChange.getAdded()).isEqualTo(expectedChange.getAdded());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());
            OrganisationPolicy actualPolicy = getOrganisationPolicy(caseData, APPLICANT_ORGANISATION_POLICY);
            assertThat(actualPolicy.getOrganisation().getOrganisationName()).isEqualTo("FRApplicantSolicitorFirm");
            assertThat(actualPolicy.getOrganisation().getOrganisationID()).isEqualTo("A31PTVA");
        }
    }

    @Test
    public void shouldUpdateRepresentationAndUpdateChangeOfRepresentatives_whenChangeAlreadyPopulated() throws Exception  {
        setUpCaseDetails("change-of-representatives.json");
        setUpHelper();

        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH + "change-of-representatives.json")) {
            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
            InputStream is = getClass().getResourceAsStream(PATH + "change-of-reps-populated-original.json");
            CaseDetails originalDetails = mapper.readValue(is, CallbackRequest.class).getCaseDetails();

            when(mockAssignCaseAccessService.getUsersWithAccess(APP_SOLICITOR_POLICY)).thenReturn(
                CaseAssignmentUserRolesResource.builder()
                    .caseAssignmentUserRoles(List.of(
                        CaseAssignmentUserRole.builder().caseRole(APP_SOLICITOR_POLICY)
                            .caseDataId(TEST_CASE_ID)
                            .userId(TEST_USER_ID)
                            .caseRole(APP_SOLICITOR_POLICY)
                            .build()))
                    .build());

            Map<String, Object> caseData = noticeOfChangeService.caseWorkerUpdatesRepresentation(actualRequest.getCaseDetails(),
                authTokenGenerator.generate(),
                originalDetails);
            List<Element<ChangeOfRepresentation>> actual = getFirstChangeElement.apply(caseData);
            ChangeOfRepresentation actualChange = actual.get(1).getValue();
            ChangeOfRepresentation expectedChange = getFirstChangeElement.apply(callbackRequest.getCaseDetails()
                .getData()).get(0).getValue();

            assertThat(actual).hasSize(2);
            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty()).isEqualTo(expectedChange.getParty());
            assertThat(actualChange.getAdded()).isEqualTo(expectedChange.getAdded());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());

            OrganisationPolicy actualPolicy = getOrganisationPolicy(caseData, APPLICANT_ORGANISATION_POLICY);
            assertThat(actualPolicy.getOrganisation().getOrganisationName()).isEqualTo("FRApplicantSolicitorFirm");
            assertThat(actualPolicy.getOrganisation().getOrganisationID()).isEqualTo("A31PTVA");
        }
    }

    @Test
    public void inConsented_shouldUpdateChangeOfRepresentatives_whenChangeCurrentlyUnpopulated() throws Exception {
        setUpCaseDetails("consented-change-of-reps.json");

        setUpHelper();
        when(mockCaseDataService.isConsentedApplication(any())).thenReturn(true);
        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH
            + "consented-change-of-reps-before.json")) {

            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
            InputStream is = getClass().getResourceAsStream(PATH + "consented-change-of-reps-original.json");
            CaseDetails originalDetails = mapper.readValue(is, CallbackRequest.class).getCaseDetails();
            when(mockAssignCaseAccessService.getUsersWithAccess(APP_SOLICITOR_POLICY)).thenReturn(
                CaseAssignmentUserRolesResource.builder()
                    .caseAssignmentUserRoles(List.of(
                        CaseAssignmentUserRole.builder().caseRole(APP_SOLICITOR_POLICY)
                            .caseDataId(TEST_CASE_ID)
                            .userId(TEST_USER_ID)
                            .caseRole(APP_SOLICITOR_POLICY)
                            .build()))
                    .build());
            Map<String, Object> caseData = noticeOfChangeService.caseWorkerUpdatesRepresentation(actualRequest.getCaseDetails(),
                authTokenGenerator.generate(), originalDetails);
            ChangeOfRepresentation actualChange = getFirstChangeElement.apply(caseData).get(0).getValue();
            ChangeOfRepresentation expectedChange = getFirstChangeElement.apply(callbackRequest.getCaseDetails()
                .getData()).get(0).getValue();

            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty()).isEqualTo(expectedChange.getParty());
            assertThat(actualChange.getAdded()).isEqualTo(expectedChange.getAdded());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());
            OrganisationPolicy actualPolicy = getOrganisationPolicy(caseData, APPLICANT_ORGANISATION_POLICY);
            assertThat(actualPolicy.getOrganisation().getOrganisationName()).isEqualTo("FRApplicantSolicitorFirm");
            assertThat(actualPolicy.getOrganisation().getOrganisationID()).isEqualTo("A31PTVA");
        }
    }

    @Test
    public void inConsented_shouldUpdateChangeOfRepresentatives_whenChangeCurrentlyPopulated() throws Exception  {
        setUpCaseDetails("consented-change-of-reps.json");
        setUpHelper();
        when(mockCaseDataService.isConsentedApplication(any())).thenReturn(true);
        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH + "consented-change-of-reps.json")) {
            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
            InputStream is = getClass().getResourceAsStream(PATH + "consented-change-of-reps-original.json");
            CaseDetails originalDetails = mapper.readValue(is, CallbackRequest.class).getCaseDetails();

            when(mockAssignCaseAccessService.getUsersWithAccess(APP_SOLICITOR_POLICY)).thenReturn(
                CaseAssignmentUserRolesResource.builder()
                    .caseAssignmentUserRoles(List.of(
                        CaseAssignmentUserRole.builder().caseRole(APP_SOLICITOR_POLICY)
                            .caseDataId(TEST_CASE_ID)
                            .userId(TEST_USER_ID)
                            .caseRole(APP_SOLICITOR_POLICY)
                            .build()))
                    .build());

            Map<String, Object> caseData = noticeOfChangeService.caseWorkerUpdatesRepresentation(actualRequest.getCaseDetails(),
                authTokenGenerator.generate(), originalDetails);
            List<Element<ChangeOfRepresentation>> actual = getFirstChangeElement.apply(caseData);
            ChangeOfRepresentation actualChange = actual.get(1).getValue();
            ChangeOfRepresentation expectedChange = getFirstChangeElement.apply(callbackRequest.getCaseDetails()
                .getData()).get(0).getValue();

            assertThat(actual).hasSize(2);
            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty()).isEqualTo(expectedChange.getParty());
            assertThat(actualChange.getAdded()).isEqualTo(expectedChange.getAdded());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());
            OrganisationPolicy actualPolicy = getOrganisationPolicy(caseData, APPLICANT_ORGANISATION_POLICY);
            assertThat(actualPolicy.getOrganisation().getOrganisationName()).isEqualTo("FRApplicantSolicitorFirm");
            assertThat(actualPolicy.getOrganisation().getOrganisationID()).isEqualTo("A31PTVA");
        }
    }

    @Test
    public void changeOfRepresentativesRespondent() throws Exception {
        setUpCaseDetails("change-of-representatives-respondent.json");
        when(mockIdamService.getIdamFullName(any())).thenReturn("Claire Mumford");
        when(mockCaseDataService.buildFullApplicantName(any())).thenReturn("John Smith");
        when(mockCaseDataService.buildFullRespondentName(any())).thenReturn("Jane Smith");
        when(mockCaseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        when(mockCaseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);
        when(mockCaseDataService.isConsentedApplication(any())).thenReturn(false);
        when(mockAssignCaseAccessService.revokeUserAccess(any())).thenReturn(CaseAssignmentUserRolesResponse
            .builder()
            .build());

        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH
            + "change-of-representatives-respondent-before.json")) {

            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
            InputStream is = getClass().getResourceAsStream(PATH + "change-of-representatives-respondent-original.json");
            CaseDetails originalDetails = mapper.readValue(is, CallbackRequest.class).getCaseDetails();

            when(mockAssignCaseAccessService.getUsersWithAccess(APP_SOLICITOR_POLICY)).thenReturn(
                CaseAssignmentUserRolesResource.builder()
                    .caseAssignmentUserRoles(List.of(
                        CaseAssignmentUserRole.builder().caseRole(APP_SOLICITOR_POLICY)
                            .caseDataId(TEST_CASE_ID)
                            .userId(TEST_USER_ID)
                            .caseRole(APP_SOLICITOR_POLICY)
                            .build()))
                    .build());

            Map<String, Object> caseData = noticeOfChangeService.caseWorkerUpdatesRepresentation(actualRequest.getCaseDetails(),
                authTokenGenerator.generate(), originalDetails);
            List<Element<ChangeOfRepresentation>> actual = getFirstChangeElement.apply(caseData);
            ChangeOfRepresentation actualChange = actual.get(0).getValue();
            ChangeOfRepresentation expectedChange = getFirstChangeElement.apply(callbackRequest.getCaseDetails()
                .getData()).get(0).getValue();

            assertThat(actual).hasSize(1);
            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty()).isEqualTo(expectedChange.getParty());
            assertThat(actualChange.getAdded()).isEqualTo(expectedChange.getAdded());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());
            OrganisationPolicy actualPolicy = getOrganisationPolicy(caseData, RESPONDENT_ORGANISATION_POLICY);
            assertThat(actualPolicy.getOrganisation().getOrganisationName()).isEqualTo("FRRespondentSolicitorFirm");
            assertThat(actualPolicy.getOrganisation().getOrganisationID()).isEqualTo("A31PTVU");
        }
    }

    @Test
    public void changeOfRepsRemoving() throws Exception {
        setUpCaseDetails("change-of-reps-removing.json");
        when(mockIdamService.getIdamFullName(any())).thenReturn("Claire Mumford");
        when(mockCaseDataService.buildFullApplicantName(any())).thenReturn("John Smith");
        when(mockCaseDataService.buildFullRespondentName(any())).thenReturn("Jane Smith");
        when(mockCaseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        when(mockCaseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);
        when(mockCaseDataService.isConsentedApplication(any())).thenReturn(false);
        when(mockAssignCaseAccessService.revokeUserAccess(any())).thenReturn(CaseAssignmentUserRolesResponse
            .builder()
            .build());

        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH
            + "change-of-reps-removing-before.json")) {

            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
            InputStream is = getClass().getResourceAsStream(PATH + "change-of-reps-removing-original.json");
            CaseDetails originalDetails = mapper.readValue(is, CallbackRequest.class).getCaseDetails();

            when(mockAssignCaseAccessService.getUsersWithAccess(APP_SOLICITOR_POLICY)).thenReturn(
                CaseAssignmentUserRolesResource.builder()
                    .caseAssignmentUserRoles(List.of(
                        CaseAssignmentUserRole.builder().caseRole(APP_SOLICITOR_POLICY)
                            .caseDataId(TEST_CASE_ID)
                            .userId(TEST_USER_ID)
                            .caseRole(APP_SOLICITOR_POLICY)
                            .build()))
                    .build());

            Map<String, Object> caseData = noticeOfChangeService.caseWorkerUpdatesRepresentation(actualRequest.getCaseDetails(),
                authTokenGenerator.generate(), originalDetails);
            List<Element<ChangeOfRepresentation>> actual = getFirstChangeElement.apply(caseData);
            ChangeOfRepresentation actualChange = actual.get(0).getValue();
            ChangeOfRepresentation expectedChange = getFirstChangeElement.apply(callbackRequest.getCaseDetails()
                .getData()).get(0).getValue();

            assertThat(actual).hasSize(1);
            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty()).isEqualTo(expectedChange.getParty());
            assertThat(actualChange.getAdded()).isEqualTo(expectedChange.getAdded());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());
            OrganisationPolicy actualPolicy = getOrganisationPolicy(caseData, APPLICANT_ORGANISATION_POLICY);
            assertThat(actualPolicy.getOrganisation()).isEqualTo(null);
        }
    }

    private OrganisationPolicy getOrganisationPolicy(Map<String, Object> caseData, String policy) {
        return mapper.convertValue(caseData.get(policy), OrganisationPolicy.class);
    }

    private void setUpHelper() {

        when(mockIdamService.getIdamFullName(any())).thenReturn("Claire Mumford");
        when(mockCaseDataService.buildFullApplicantName(any())).thenReturn("John Smith");
        when(mockCaseDataService.buildFullRespondentName(any())).thenReturn("Jane Smith");
        when(mockCaseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        when(mockCaseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);
        when(mockCaseDataService.isConsentedApplication(any())).thenReturn(false);
        when(mockAssignCaseAccessService.revokeUserAccess(any())).thenReturn(CaseAssignmentUserRolesResponse
            .builder()
            .build());

    }
}
