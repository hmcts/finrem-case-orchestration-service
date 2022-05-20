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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.AddedSolicitorService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.RemovedSolicitorService;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOC_PARTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;

public class NoticeOfChangeServiceTest extends BaseServiceTest {

    private static final String PATH = "/fixtures/noticeOfChange/caseworkerNoc/";
    private static final String REPRESENTATION_UPDATE_HISTORY = "RepresentationUpdateHistory";

    @Autowired private NoticeOfChangeService noticeOfChangeService;

    @MockBean private CaseDataService mockCaseDataService;

    @MockBean private IdamService mockIdamService;

    @MockBean private AddedSolicitorService addedSolicitorService;

    @MockBean private RemovedSolicitorService removedSolicitorService;

    private CallbackRequest callbackRequest;

    private final Function<Map<String, Object>, List<Element<RepresentationUpdate>>> getFirstChangeElement =
        this::convertToUpdateHistory;


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

    @Test
    public void shouldUpdateRepresentationAndGenerateRepresentationUpdateHistory_whenCaseDataIsValid() throws Exception {
        setUpCaseDetails("change-of-representatives.json");

        setUpHelper();
        when(addedSolicitorService.getAddedSolicitorAsCaseworker(any())).thenReturn(ChangedRepresentative.builder()
            .name("Sir Solicitor")
            .email("sirsolicitor1@gmail.com")
            .organisation(Organisation.builder()
                .organisationID("A31PTVA")
                .organisationName("FRApplicantSolicitorFirm")
                .build())
            .build());

        try (InputStream resourceAsStream = getClass()
            .getResourceAsStream(PATH + "change-of-representatives-before.json")) {

            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
            InputStream is = getClass().getResourceAsStream(PATH + "change-of-representatives-original-data.json");
            CaseDetails originalDetails = mapper.readValue(is, CallbackRequest.class).getCaseDetails();

            Map<String, Object> caseData = noticeOfChangeService.updateRepresentation(actualRequest.getCaseDetails(),
                authTokenGenerator.generate(),
                originalDetails);

            RepresentationUpdate actualChange = getFirstChangeElement.apply(caseData).get(0).getValue();
            RepresentationUpdate expectedChange = getFirstChangeElement.apply(callbackRequest.getCaseDetails()
                .getData()).get(0).getValue();

            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty().toLowerCase()).isEqualTo(expectedChange.getParty().toLowerCase());
            assertThat(actualChange.getAdded()).isEqualTo(expectedChange.getAdded());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());
        }
    }

    @Test
    public void shouldUpdateRepresentationAndUpdateRepresentationUpdateHistory_whenChangeAlreadyPopulated() throws Exception  {
        setUpCaseDetails("change-of-representatives.json");
        setUpHelper();

        when(addedSolicitorService.getAddedSolicitorAsCaseworker(any())).thenReturn(ChangedRepresentative.builder()
            .name("Sir Solicitor")
            .email("sirsolicitor1@gmail.com")
            .organisation(Organisation.builder()
                .organisationID("A31PTVA")
                .organisationName("FRApplicantSolicitorFirm")
                .build())
            .build());

        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH + "change-of-representatives.json")) {
            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
            InputStream is = getClass().getResourceAsStream(PATH + "change-of-reps-populated-original.json");
            CaseDetails originalDetails = mapper.readValue(is, CallbackRequest.class).getCaseDetails();
            Map<String, Object> caseData = noticeOfChangeService.updateRepresentation(actualRequest.getCaseDetails(),
                authTokenGenerator.generate(),
                originalDetails);
            List<Element<RepresentationUpdate>> actual = getFirstChangeElement.apply(caseData);
            RepresentationUpdate actualChange = actual.get(1).getValue();
            RepresentationUpdate expectedChange = getFirstChangeElement.apply(callbackRequest.getCaseDetails()
                .getData()).get(0).getValue();

            assertThat(actual).hasSize(2);
            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty().toLowerCase()).isEqualTo(expectedChange.getParty().toLowerCase());
            assertThat(actualChange.getAdded()).isEqualTo(expectedChange.getAdded());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());
        }
    }

    @Test
    public void inConsented_shouldUpdateRepresentationUpdateHistory_whenChangeCurrentlyUnpopulated() throws Exception {
        setUpCaseDetails("consented-change-of-reps.json");

        when(addedSolicitorService.getAddedSolicitorAsCaseworker(any())).thenReturn(ChangedRepresentative.builder()
            .name("Sir Solicitor")
            .email("sirsolicitor1@gmail.com")
            .organisation(Organisation.builder()
                .organisationID("A31PTVA")
                .organisationName("FRApplicantSolicitorFirm")
                .build())
            .build());

        setUpHelper();
        when(mockCaseDataService.isConsentedApplication(any())).thenReturn(true);
        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH
            + "consented-change-of-reps-before.json")) {

            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
            InputStream is = getClass().getResourceAsStream(PATH + "consented-change-of-reps-original.json");
            CaseDetails originalDetails = mapper.readValue(is, CallbackRequest.class).getCaseDetails();
            Map<String, Object> caseData = noticeOfChangeService.updateRepresentation(actualRequest.getCaseDetails(),
                authTokenGenerator.generate(), originalDetails);
            RepresentationUpdate actualChange = getFirstChangeElement.apply(caseData).get(0).getValue();
            RepresentationUpdate expectedChange = getFirstChangeElement.apply(callbackRequest.getCaseDetails()
                .getData()).get(0).getValue();

            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty().toLowerCase()).isEqualTo(expectedChange.getParty().toLowerCase());
            assertThat(actualChange.getAdded()).isEqualTo(expectedChange.getAdded());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());
        }
    }

    @Test
    public void inConsented_shouldUpdateRepresentationUpdateHistory_whenChangeCurrentlyPopulated() throws Exception  {
        setUpCaseDetails("consented-change-of-reps.json");
        setUpHelper();

        when(addedSolicitorService.getAddedSolicitorAsCaseworker(any())).thenReturn(ChangedRepresentative.builder()
            .name("Sir Solicitor")
            .email("sirsolicitor1@gmail.com")
            .organisation(Organisation.builder()
                .organisationID("A31PTVA")
                .organisationName("FRApplicantSolicitorFirm")
                .build())
            .build());

        when(mockCaseDataService.isConsentedApplication(any())).thenReturn(true);
        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH + "consented-change-of-reps.json")) {
            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
            InputStream is = getClass().getResourceAsStream(PATH + "consented-change-of-reps-original.json");
            CaseDetails originalDetails = mapper.readValue(is, CallbackRequest.class).getCaseDetails();
            Map<String, Object> caseData = noticeOfChangeService.updateRepresentation(actualRequest.getCaseDetails(),
                authTokenGenerator.generate(), originalDetails);
            List<Element<RepresentationUpdate>> actual = getFirstChangeElement.apply(caseData);
            RepresentationUpdate actualChange = actual.get(1).getValue();
            RepresentationUpdate expectedChange = getFirstChangeElement.apply(callbackRequest.getCaseDetails()
                .getData()).get(0).getValue();

            assertThat(actual).hasSize(2);
            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty().toLowerCase()).isEqualTo(expectedChange.getParty().toLowerCase());
            assertThat(actualChange.getAdded()).isEqualTo(expectedChange.getAdded());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());
        }
    }

    @Test
    public void shouldUpdateRepresentationUpdateHistory_whenNatureIsRemoving() throws Exception {
        setUpCaseDetails("change-of-reps-removing.json");
        setUpHelper();
        when(mockCaseDataService.isConsentedApplication(any())).thenReturn(true);

        when(removedSolicitorService.getRemovedSolicitorAsCaseworker(any(), any())).thenReturn(
            ChangedRepresentative.builder()
                .name("Sir Solicitor")
                .email("sirsolicitor1@gmail.com")
                .organisation(Organisation.builder()
                    .organisationID("A31PTVA")
                    .organisationName("FRApplicantSolicitorFirm")
                    .build())
                .build());

        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH + "change-of-reps-removing-before.json")) {
            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);

            InputStream is = getClass().getResourceAsStream(PATH + "change-of-reps-removing-original.json");
            CaseDetails originalDetails = mapper.readValue(is, CallbackRequest.class).getCaseDetails();
            Map<String, Object> caseData = noticeOfChangeService.updateRepresentation(actualRequest.getCaseDetails(),
                authTokenGenerator.generate(),
                originalDetails);
            RepresentationUpdate actualChange = getFirstChangeElement.apply(caseData).get(0).getValue();
            RepresentationUpdate expectedChange = getFirstChangeElement.apply(callbackRequest.getCaseDetails()
                .getData()).get(0).getValue();

            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty().toLowerCase()).isEqualTo(expectedChange.getParty().toLowerCase());
            assertThat(actualChange.getRemoved()).isEqualTo(expectedChange.getRemoved());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());
        }
    }

    @Test
    public void shouldUpdateRepresentationUpdateHistory_whenNatureIsReplacing() throws Exception {
        setUpCaseDetails("change-of-reps-replacing.json");

        setUpHelper();

        when(addedSolicitorService.getAddedSolicitorAsCaseworker(any())).thenReturn(
            ChangedRepresentative.builder()
                .name("TestAppSolName")
                .email("testappsol123@gmail.com")
                .organisation(Organisation.builder()
                    .organisationID("A31PTVU")
                    .organisationName("FRApplicantNewSolFirm")
                    .build())
                .build());
        when(removedSolicitorService.getRemovedSolicitorAsCaseworker(any(), any())).thenReturn(
            ChangedRepresentative.builder()
                .name("Sir Solicitor")
                .email("sirsolicitor1@gmail.com")
                .organisation(Organisation.builder()
                    .organisationID("A31PTVA")
                    .organisationName("FRApplicantSolicitorFirm")
                    .build())
                .build());

        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH
            + "change-of-reps-replacing-before.json")) {
            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
            InputStream is = getClass().getResourceAsStream(PATH + "change-of-reps-replacing-original.json");
            CaseDetails originalDetails = mapper.readValue(is, CallbackRequest.class).getCaseDetails();
            Map<String, Object> caseData = noticeOfChangeService.updateRepresentation(actualRequest.getCaseDetails(),
                authTokenGenerator.generate(),
                originalDetails);

            RepresentationUpdate actualChange = getFirstChangeElement.apply(caseData).get(0).getValue();
            RepresentationUpdate expected = getFirstChangeElement.apply(callbackRequest.getCaseDetails()
                .getData()).get(0).getValue();

            assertThat(actualChange.getClientName()).isEqualTo(expected.getClientName());
            assertThat(actualChange.getParty().toLowerCase()).isEqualTo(expected.getParty().toLowerCase());
            assertThat(actualChange.getAdded()).isEqualTo(expected.getAdded()); //added = old sol
            assertThat(actualChange.getRemoved().getOrganisation()).isEqualTo(expected.getRemoved().getOrganisation());
            assertThat(actualChange.getBy()).isEqualTo(expected.getBy());
        }
    }

    @Test
    public void shouldUpdateRepresentationUpdateHistoryRespondent() throws Exception {
        setUpCaseDetails("change-of-representatives-respondent.json");
        when(mockIdamService.getIdamFullName(any())).thenReturn("Claire Mumford");
        when(mockCaseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        when(mockCaseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);
        when(mockCaseDataService.buildFullRespondentName(any())).thenReturn("Jane Smith");
        when(addedSolicitorService.getAddedSolicitorAsCaseworker(any())).thenReturn(
            ChangedRepresentative.builder()
                .name("Test respondent Solicitor")
                .email("padmaja.ramisetti@gmail.com")
                .organisation(Organisation.builder()
                    .organisationID("A31PTVU")
                    .organisationName("FRRespondentSolicitorFirm")
                    .build())
                .build());

        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH
            + "change-of-representatives-respondent-before.json")) {
            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
            InputStream is = getClass().getResourceAsStream(PATH
                + "change-of-representatives-respondent-original.json");
            CaseDetails originalDetails = mapper.readValue(is, CallbackRequest.class).getCaseDetails();

            Map<String, Object> caseData = noticeOfChangeService.updateRepresentation(actualRequest.getCaseDetails(),
                authTokenGenerator.generate(),
                originalDetails);
            RepresentationUpdate actualChange = getFirstChangeElement.apply(caseData).get(0).getValue();
            RepresentationUpdate expected = getFirstChangeElement.apply(callbackRequest.getCaseDetails()
                .getData()).get(0).getValue();

            assertThat(actualChange.getClientName()).isEqualTo(expected.getClientName());
            assertThat(actualChange.getParty().toLowerCase()).isEqualTo(expected.getParty().toLowerCase());
            assertThat(actualChange.getAdded()).isEqualTo(expected.getAdded());
            assertThat(actualChange.getBy()).isEqualTo(expected.getBy());
        }
    }

    @Test
    public void givenNoOrgPoliciesAndApplicant_whenRevokingAccess_thenRepopulateFromOriginalCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(NOC_PARTY, APPLICANT);
        caseData.put(APPLICANT_ORGANISATION_POLICY, OrganisationPolicy.builder().build());
        Map<String, Object> originalData = new HashMap<>();
        OrganisationPolicy appSolicitorPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationName("Test Firm").organisationID("testID").build())
            .orgPolicyCaseAssignedRole(APP_SOLICITOR_POLICY)
            .build();
        originalData.put(APPLICANT_ORGANISATION_POLICY, appSolicitorPolicy);

        CaseDetails currentDetails = CaseDetails.builder().data(caseData).build();
        CaseDetails originalDetails = CaseDetails.builder().data(originalData).build();

        currentDetails = noticeOfChangeService.persistOriginalOrgPoliciesWhenRevokingAccess(currentDetails,
            originalDetails);
        OrganisationPolicy actualPolicy = mapper.convertValue(currentDetails.getData().get(APPLICANT_ORGANISATION_POLICY),
            OrganisationPolicy.class);
        assertThat(actualPolicy).isEqualTo(appSolicitorPolicy);
    }

    @Test
    public void givenNoOrgPoliciesAndRespondent_whenRevokingAccess_thenRepopulateFromOriginalCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(NOC_PARTY, RESPONDENT);
        caseData.put(RESPONDENT_ORGANISATION_POLICY, OrganisationPolicy.builder().build());
        Map<String, Object> originalData = new HashMap<>();
        OrganisationPolicy appSolicitorPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationName("Test Firm").organisationID("testID").build())
            .orgPolicyCaseAssignedRole(RESP_SOLICITOR_POLICY)
            .build();
        originalData.put(RESPONDENT_ORGANISATION_POLICY, appSolicitorPolicy);

        CaseDetails currentDetails = CaseDetails.builder().data(caseData).build();
        CaseDetails originalDetails = CaseDetails.builder().data(originalData).build();

        currentDetails = noticeOfChangeService.persistOriginalOrgPoliciesWhenRevokingAccess(currentDetails,
            originalDetails);
        OrganisationPolicy actualPolicy = mapper.convertValue(currentDetails.getData()
                .get(RESPONDENT_ORGANISATION_POLICY), OrganisationPolicy.class);
        assertThat(actualPolicy).isEqualTo(appSolicitorPolicy);
    }

    private List<Element<RepresentationUpdate>> convertToUpdateHistory(Map<String, Object> data) {
        return mapper.convertValue(data.get(REPRESENTATION_UPDATE_HISTORY),
            new TypeReference<>() {});
    }

    private void setUpHelper() {

        when(mockIdamService.getIdamFullName(any())).thenReturn("Claire Mumford");
        when(mockCaseDataService.buildFullApplicantName(any())).thenReturn("John Smith");
        when(mockCaseDataService.buildFullRespondentName(any())).thenReturn("Jane Smith");
        when(mockCaseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        when(mockCaseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);
        when(mockCaseDataService.isConsentedApplication(any())).thenReturn(false);
    }

}

