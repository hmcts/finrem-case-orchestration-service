package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentatives;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;

public class NoticeOfChangeServiceTest extends BaseServiceTest {

    private static final String PATH = "/fixtures/noticeOfChange/";
    private static final String CHANGE_OF_REPRESENTATIVES = "changeOfRepresentatives";

    @Autowired private NoticeOfChangeService noticeOfChangeService;

    @MockBean private CaseDataService mockCaseDataService;

    @MockBean private IdamService mockIdamService;

    CallbackRequest callbackRequest;
    ChangeOfRepresentation newRepresentationChange;
    ChangeOfRepresentatives expected;


    @Before
    public void setUp() {
        mapper.registerModule(new JavaTimeModule());

        ChangedRepresentative newRep = ChangedRepresentative.builder()
            .name("Sir Solicitor")
            .email("sirsolicitor1@gmail.com")
            .organisation(Organisation.builder().organisationID("A31PTVA").organisationName("FRApplicantSolicitorFirm").build())
            .build();

        newRepresentationChange = ChangeOfRepresentation.builder()
            .party("applicant")
            .clientName("John Smith")
            .date(LocalDate.of(2020, 06, 01))
            .by("Case Worker")
            .via("Notice of Change")
            .added(newRep)
            .build();

        expected = ChangeOfRepresentatives.builder()
            .changeOfRepresentation(List.of(newRepresentationChange))
            .build();
    }

    private void setUpCaseDetails(String fileName) throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream(PATH + fileName)) {
            callbackRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
        }
    }

    @Test
    public void shouldUpdateRepresentationAndGenerateChangeOfRepresentatives_whenCaseDataIsValid() throws Exception {
        setUpCaseDetails("change-of-representatives.json");

        setUpHelper();

        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH + "change-of-representatives-before.json")) {
            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);

            Map<String, Object> caseData = noticeOfChangeService.updateRepresentation(actualRequest.getCaseDetails(), authTokenGenerator.generate());
            ChangeOfRepresentation actualChange = convertToChangeOfRepresentatives(caseData.get(CHANGE_OF_REPRESENTATIVES))
                .getChangeOfRepresentation().get(0);
            ChangeOfRepresentation expectedChange = convertToChangeOfRepresentatives(callbackRequest.getCaseDetails()
                .getData().get(CHANGE_OF_REPRESENTATIVES)).getChangeOfRepresentation().get(0);

            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty()).isEqualTo(expectedChange.getParty());
            assertThat(actualChange.getAdded()).isEqualTo(expectedChange.getAdded());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());
        }
    }

    @Test
    public void shouldUpdateRepresentationAndUpdateChangeOfRepresentatives_whenChangeAlreadyPopulated() throws Exception  {
        setUpCaseDetails("change-of-representatives.json");
        setUpHelper();

        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH + "change-of-representatives.json")) {
            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);

            Map<String, Object> caseData = noticeOfChangeService.updateRepresentation(actualRequest.getCaseDetails(), authTokenGenerator.generate());
            ChangeOfRepresentatives actual = convertToChangeOfRepresentatives(caseData.get(CHANGE_OF_REPRESENTATIVES));

            ChangeOfRepresentation actualChange = actual.getChangeOfRepresentation().get(1);
            ChangeOfRepresentation expectedChange = convertToChangeOfRepresentatives(callbackRequest.getCaseDetails()
                .getData().get(CHANGE_OF_REPRESENTATIVES)).getChangeOfRepresentation().get(0);

            assertThat(actual.getChangeOfRepresentation()).hasSize(2);
            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty()).isEqualTo(expectedChange.getParty());
            assertThat(actualChange.getAdded()).isEqualTo(expectedChange.getAdded());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());
        }
    }

    @Test
    public void inConsented_shouldUpdateChangeOfRepresentatives_whenChangeCurrentlyUnpopulated() throws Exception {
        setUpCaseDetails("consented-change-of-reps.json");

        setUpHelper();

        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH + "consented-change-of-reps-before.json")) {
            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);

            Map<String, Object> caseData = noticeOfChangeService.updateRepresentation(actualRequest.getCaseDetails(),
                authTokenGenerator.generate());
            ChangeOfRepresentation actualChange = convertToChangeOfRepresentatives(caseData.get(CHANGE_OF_REPRESENTATIVES))
                .getChangeOfRepresentation().get(0);
            ChangeOfRepresentation expectedChange = convertToChangeOfRepresentatives(callbackRequest.getCaseDetails()
                .getData().get(CHANGE_OF_REPRESENTATIVES)).getChangeOfRepresentation().get(0);

            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty()).isEqualTo(expectedChange.getParty());
            assertThat(actualChange.getAdded()).isEqualTo(expectedChange.getAdded());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());
        }
    }

    @Test
    public void inConsented_shouldUpdateChangeOfRepresentatives_whenChangeCurrentlyPopulated() throws Exception  {
        setUpCaseDetails("consented-change-of-reps.json");
        setUpHelper();

        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH + "consented-change-of-reps.json")) {
            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);

            Map<String, Object> caseData = noticeOfChangeService.updateRepresentation(actualRequest.getCaseDetails(), authTokenGenerator.generate());
            ChangeOfRepresentatives actual = convertToChangeOfRepresentatives(caseData.get(CHANGE_OF_REPRESENTATIVES));

            ChangeOfRepresentation actualChange = actual.getChangeOfRepresentation().get(1);
            ChangeOfRepresentation expectedChange = convertToChangeOfRepresentatives(callbackRequest.getCaseDetails()
                .getData().get(CHANGE_OF_REPRESENTATIVES)).getChangeOfRepresentation().get(0);

            assertThat(actual.getChangeOfRepresentation()).hasSize(2);
            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty()).isEqualTo(expectedChange.getParty());
            assertThat(actualChange.getAdded()).isEqualTo(expectedChange.getAdded());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());
        }
    }

    @Test
    public void shouldUpdateChangeOfRepresentatives_whenNatureIsRemoving() throws Exception {
        setUpCaseDetails("change-of-reps-removing.json");

        setUpHelper();

        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH + "change-of-reps-removing-before.json")) {
            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);

            Map<String, Object> caseData = noticeOfChangeService.updateRepresentation(actualRequest.getCaseDetails(), authTokenGenerator.generate());
            ChangeOfRepresentatives actual = convertToChangeOfRepresentatives(caseData.get(CHANGE_OF_REPRESENTATIVES));

            ChangeOfRepresentation actualChange = actual.getChangeOfRepresentation().get(0);
            ChangeOfRepresentation expectedChange = convertToChangeOfRepresentatives(callbackRequest.getCaseDetails()
                .getData().get(CHANGE_OF_REPRESENTATIVES)).getChangeOfRepresentation().get(0);

            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty()).isEqualTo(expectedChange.getParty());
            assertThat(actualChange.getRemoved()).isEqualTo(expectedChange.getRemoved());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());
        }
    }

    @Test
    public void shouldUpdateChangeOfRepresentatives_whenNatureIsReplacing() throws Exception {
        setUpCaseDetails("change-of-reps-replacing.json");

        setUpHelper();
        when(mockCaseDataService.getApplicantSolicitorName(any())).thenReturn("TestAppSolName");
        when(mockCaseDataService.getApplicantSolicitorEmail(any())).thenReturn("testappsol123@gmail.com");

        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH + "change-of-reps-replacing-before.json")) {
            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);

            Map<String, Object> caseData = noticeOfChangeService.updateRepresentation(actualRequest.getCaseDetails(), authTokenGenerator.generate());
            ChangeOfRepresentation actualChange = convertToChangeOfRepresentatives(caseData.get(CHANGE_OF_REPRESENTATIVES))
                .getChangeOfRepresentation().get(0);
            ChangeOfRepresentation expected = convertToChangeOfRepresentatives(callbackRequest.getCaseDetails()
                .getData().get(CHANGE_OF_REPRESENTATIVES)).getChangeOfRepresentation().get(0);

            assertThat(actualChange.getClientName()).isEqualTo(expected.getClientName());
            assertThat(actualChange.getParty()).isEqualTo(expected.getParty());
            assertThat(actualChange.getAdded()).isEqualTo(expected.getAdded()); //added = old sol
            assertThat(actualChange.getRemoved()).isEqualTo(expected.getRemoved());
            assertThat(actualChange.getBy()).isEqualTo(expected.getBy());
        }
    }

    @Test
    public void whenSavePreviousOrganisation_thenShouldReturnCaseDataWithPreviousOrganisationField() throws Exception {
        setUpCaseDetails("change-of-representatives.json");
        setUpHelper();

        Map<String, Object> caseData = noticeOfChangeService.savePreviousOrganisation(callbackRequest.getCaseDetails());

        OrganisationPolicy expectedPolicy = mapper.convertValue(caseData.get(APPLICANT_ORGANISATION_POLICY), OrganisationPolicy.class);

        ChangedRepresentative actualPolicy = mapper.convertValue(caseData.get("ApplicantPreviousRepresentative"), ChangedRepresentative.class);

        assertThat(actualPolicy.getOrganisation().getOrganisationID()).isEqualTo(expectedPolicy.getOrganisation().getOrganisationID());
        assertThat(actualPolicy.getOrganisation().getOrganisationName()).isEqualTo(expectedPolicy.getOrganisation().getOrganisationName());
    }

    @Test
    public void whenSavePreviousOrganisation_thenShouldReturnCaseDataWithRespondentPreviousOrganisationField() throws Exception {
        setUpHelper();
        setUpCaseDetails("change-of-representatives-respondent.json");
        Map<String, Object> caseData = noticeOfChangeService.savePreviousOrganisation(callbackRequest.getCaseDetails());

        OrganisationPolicy expectedPolicy = mapper.convertValue(caseData.get(RESPONDENT_ORGANISATION_POLICY), OrganisationPolicy.class);

        ChangedRepresentative actualPolicy = mapper.convertValue(caseData.get("RespondentPreviousRepresentative"), ChangedRepresentative.class);

        assertThat(actualPolicy.getOrganisation().getOrganisationID()).isEqualTo(expectedPolicy.getOrganisation().getOrganisationID());
        assertThat(actualPolicy.getOrganisation().getOrganisationName()).isEqualTo(expectedPolicy.getOrganisation().getOrganisationName());
    }

    @Test
    public void inConsented_whenSavePreviousOrganisation_thenShouldReturnCaseDataWithPreviousOrganisationField() throws Exception {
        setUpCaseDetails("consented-change-of-reps.json");
        setUpHelper();

        Map<String, Object> caseData = noticeOfChangeService.savePreviousOrganisation(callbackRequest.getCaseDetails());

        OrganisationPolicy expectedPolicy = mapper.convertValue(caseData.get(APPLICANT_ORGANISATION_POLICY), OrganisationPolicy.class);

        ChangedRepresentative actualPolicy = mapper.convertValue(caseData.get("ApplicantPreviousRepresentative"), ChangedRepresentative.class);

        assertThat(actualPolicy.getOrganisation().getOrganisationID()).isEqualTo(expectedPolicy.getOrganisation().getOrganisationID());
        assertThat(actualPolicy.getOrganisation().getOrganisationName()).isEqualTo(expectedPolicy.getOrganisation().getOrganisationName());
    }

    @Test
    public void inConsented_whenSavePreviousOrganisation_thenShouldReturnCaseDataWithRespondentPreviousOrganisationField() throws Exception {
        setUpHelper();
        setUpCaseDetails("consented-change-of-reps-respondent.json");
        Map<String, Object> caseData = noticeOfChangeService.savePreviousOrganisation(callbackRequest.getCaseDetails());

        OrganisationPolicy expectedPolicy = mapper.convertValue(caseData.get(RESPONDENT_ORGANISATION_POLICY), OrganisationPolicy.class);

        ChangedRepresentative actualPolicy = mapper.convertValue(caseData.get("RespondentPreviousRepresentative"), ChangedRepresentative.class);

        assertThat(actualPolicy.getOrganisation().getOrganisationID()).isEqualTo(expectedPolicy.getOrganisation().getOrganisationID());
        assertThat(actualPolicy.getOrganisation().getOrganisationName()).isEqualTo(expectedPolicy.getOrganisation().getOrganisationName());
    }

    private void setUpHelper() {

        when(mockIdamService.getIdamFullName(any())).thenReturn("Claire Mumford");
        when(mockCaseDataService.getApplicantSolicitorName(any())).thenReturn("Sir Solicitor");
        when(mockCaseDataService.getApplicantSolicitorEmail(any())).thenReturn("sirsolicitor1@gmail.com");
        when(mockCaseDataService.buildFullApplicantName(any())).thenReturn("John Smith");
        when(mockCaseDataService.buildFullRespondentName(any())).thenReturn("Jane Smith");
        when(mockCaseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        when(mockCaseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);
    }

    private ChangeOfRepresentatives convertToChangeOfRepresentatives(Object object) {
        return mapper.convertValue(object, new TypeReference<>() {});
    }

}
