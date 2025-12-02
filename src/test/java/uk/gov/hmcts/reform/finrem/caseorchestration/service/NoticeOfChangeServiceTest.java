package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistoryCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.NoticeOfChangeService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.AddedSolicitorService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.RemovedSolicitorService;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
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

    @Autowired
    private NoticeOfChangeService noticeOfChangeService;

    @MockitoBean
    private CaseDataService mockCaseDataService;

    @MockitoBean
    private IdamService mockIdamService;

    @MockitoBean
    private AddedSolicitorService addedSolicitorService;

    @MockitoBean
    private RemovedSolicitorService removedSolicitorService;

    private final Function<Map<String, Object>, List<Element<RepresentationUpdate>>> getRepresentationUpdateHistory =
        this::convertToUpdateHistory;

    private final Function<FinremCaseData, List<RepresentationUpdateHistoryCollection>> getRepresentationUpdateHistoryFinrem =
        this::convertToUpdateHistoryFinrem;

    private static MockedStatic<LocalDateTime> localDateTimeMock;

    @BeforeClass
    public static void setUpClass() {
        LocalDateTime fixed = LocalDateTime.of(2024, 1, 1, 12, 0);

        localDateTimeMock = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS);

        localDateTimeMock.when(LocalDateTime::now)
            .thenReturn(fixed);

        localDateTimeMock.when(() -> LocalDateTime.now(ZoneId.systemDefault()))
            .thenReturn(fixed);
    }

    @AfterClass
    public static void tearDownClass() {
        localDateTimeMock.close();
    }

    @Before
    public void setUp() {
        mapper.registerModule(new JavaTimeModule());
        when(mockIdamService.getIdamFullName(AUTH_TOKEN)).thenReturn("Claire Mumford");
    }

    @Test
    public void shouldUpdateRepresentationAndGenerateRepresentationUpdateHistory_whenCaseDataIsValid() throws Exception {
        setUpHelper();
        when(addedSolicitorService.getAddedSolicitorAsCaseworker(any(CaseDetails.class))).thenReturn(ChangedRepresentative.builder()
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
                AUTH_TOKEN,
                originalDetails);

            RepresentationUpdate actualChange = getRepresentationUpdateHistory.apply(caseData).getFirst().getValue();
            RepresentationUpdate expectedChange = RepresentationUpdate.builder()
                .party("applicant")
                .clientName("John Smith")
                .by("Claire Mumford")
                .via("Notice of Change")
                .date(LocalDateTime.of(2020, 6, 1, 15, 0, 0))
                .added(ChangedRepresentative.builder()
                    .email("sirsolicitor1@gmail.com")
                    .name("Sir Solicitor")
                    .organisation(Organisation.builder()
                        .organisationID("A31PTVA")
                        .organisationName("FRApplicantSolicitorFirm")
                        .build())
                    .build())
                .build();

            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty().toLowerCase()).isEqualTo(expectedChange.getParty().toLowerCase());
            assertThat(actualChange.getAdded()).isEqualTo(expectedChange.getAdded());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());
        }
    }

    @Test
    public void shouldUpdateRepresentationAndGenerateRepresentationUpdateHistory_whenCaseDataIsValid_finrem() {
        // Arrange
        FinremCaseData finremCaseData = readFinremCaseData("change-of-representatives-before.json");
        FinremCaseData originalFinremCaseData = readFinremCaseData("change-of-representatives-original-data.json");
        final ChangedRepresentative addingChangedRepresentative = mock(ChangedRepresentative.class);
        when(addedSolicitorService.getAddedSolicitorAsCaseworker(finremCaseData))
            .thenReturn(addingChangedRepresentative);

        // Act
        noticeOfChangeService.updateRepresentation(finremCaseData, originalFinremCaseData, AUTH_TOKEN);

        // Verify
        // finremCaseData is updated. The following verification is performed on finremCaseData
        List<RepresentationUpdateHistoryCollection> actual = getRepresentationUpdateHistoryFinrem.apply(finremCaseData);
        RepresentationUpdate actualChange = actual.getLast().getValue();
        RepresentationUpdate expectedChange = RepresentationUpdate.builder()
            .party("Applicant")
            .clientName("John Smith")
            .by("Claire Mumford")
            .via("Notice of Change")
            .date(LocalDateTime.of(2020, 6, 1, 15, 0, 0))
            .added(addingChangedRepresentative)
            .build();

        assertRepresentationUpdate(actualChange, expectedChange);
        assertChangeOrganisationField(finremCaseData, true,"A31PTVA", null);
    }

    @Test
    public void shouldUpdateRepresentationAndUpdateRepresentationUpdateHistory_whenChangeAlreadyPopulated() throws Exception {
        setUpHelper();

        when(addedSolicitorService.getAddedSolicitorAsCaseworker(any(CaseDetails.class))).thenReturn(ChangedRepresentative.builder()
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
                AUTH_TOKEN,
                originalDetails);
            List<Element<RepresentationUpdate>> actual = getRepresentationUpdateHistory.apply(caseData);
            RepresentationUpdate actualChange = actual.get(1).getValue();
            RepresentationUpdate expectedChange = RepresentationUpdate.builder()
                .party("applicant")
                .clientName("John Smith")
                .by("Claire Mumford")
                .via("Notice of Change")
                .date(LocalDateTime.of(2020, 6, 1, 15, 0, 0))
                .added(ChangedRepresentative.builder()
                    .email("sirsolicitor1@gmail.com")
                    .name("Sir Solicitor")
                    .organisation(Organisation.builder()
                        .organisationID("A31PTVA")
                        .organisationName("FRApplicantSolicitorFirm")
                        .build())
                    .build())
                .build();

            assertThat(actual).hasSize(2);
            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty().toLowerCase()).isEqualTo(expectedChange.getParty().toLowerCase());
            assertThat(actualChange.getAdded()).isEqualTo(expectedChange.getAdded());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());
        }
    }

    @Test
    public void shouldUpdateRepresentationAndUpdateRepresentationUpdateHistory_whenChangeAlreadyPopulated_finrem() {
        // Arrange
        FinremCaseData finremCaseData = readFinremCaseData("change-of-representatives.json");
        FinremCaseData originalFinremCaseData = readFinremCaseData("change-of-reps-populated-original.json");
        final ChangedRepresentative addingChangedRepresentative = mock(ChangedRepresentative.class);
        when(addedSolicitorService.getAddedSolicitorAsCaseworker(finremCaseData))
            .thenReturn(addingChangedRepresentative);

        // Act
        noticeOfChangeService.updateRepresentation(finremCaseData, originalFinremCaseData, AUTH_TOKEN);

        // Verify
        // finremCaseData is updated. The following verification is performed on finremCaseData
        List<RepresentationUpdateHistoryCollection> actual = getRepresentationUpdateHistoryFinrem.apply(finremCaseData);

        assertThat(actual).hasSize(2);

        // Verify the original data to be kept
        RepresentationUpdate firstElement = actual.getFirst().getValue();
        RepresentationUpdate historyDataToBeKept = getRepresentationUpdateHistory.apply(
            readCaseDetailsData("change-of-representatives.json")
        ).getFirst().getValue();
        assertThat(firstElement)
            .usingRecursiveComparison()
            .withComparatorForFields(String.CASE_INSENSITIVE_ORDER, "party")
            .isEqualTo(historyDataToBeKept);
        assertRepresentationUpdate(firstElement, historyDataToBeKept);

        // Verify the new history appended
        RepresentationUpdate lastElement = actual.getLast().getValue();
        RepresentationUpdate expectedNewElement = RepresentationUpdate.builder()
            .added(addingChangedRepresentative)
            .party("Applicant")
            .date(LocalDateTime.of(2020, 6, 1, 15, 0, 0))
            .clientName("John Smith")
            .by("Claire Mumford")
            .via("Notice of Change")
            .build();
        assertRepresentationUpdate(lastElement, expectedNewElement);
        assertThat(firstElement.getDate()).isBefore(lastElement.getDate());
        assertChangeOrganisationField(finremCaseData, true,"A31PTVA", null);
    }

    @Test
    public void inConsented_shouldUpdateRepresentationUpdateHistory_whenChangeCurrentlyUnpopulated() throws Exception {
        when(addedSolicitorService.getAddedSolicitorAsCaseworker(any(CaseDetails.class))).thenReturn(ChangedRepresentative.builder()
            .name("Sir Solicitor")
            .email("sirsolicitor1@gmail.com")
            .organisation(Organisation.builder()
                .organisationID("A31PTVA")
                .organisationName("FRApplicantSolicitorFirm")
                .build())
            .build());

        setUpHelper();
        when(mockCaseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(true);
        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH
            + "consented-change-of-reps-before.json")) {

            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
            InputStream is = getClass().getResourceAsStream(PATH + "consented-change-of-reps-original.json");
            CaseDetails originalDetails = mapper.readValue(is, CallbackRequest.class).getCaseDetails();
            Map<String, Object> caseData = noticeOfChangeService.updateRepresentation(actualRequest.getCaseDetails(),
                AUTH_TOKEN, originalDetails);
            RepresentationUpdate actualChange = getRepresentationUpdateHistory.apply(caseData).getFirst().getValue();
            RepresentationUpdate expectedChange = RepresentationUpdate.builder()
                .party("applicant")
                .clientName("John Smith")
                .by("Claire Mumford")
                .via("Notice of Change")
                .added(ChangedRepresentative.builder()
                    .email("sirsolicitor1@gmail.com")
                    .name("Sir Solicitor")
                    .organisation(Organisation.builder()
                        .organisationID("A31PTVA")
                        .organisationName("FRApplicantSolicitorFirm")
                        .build())
                    .build())
                .build();

            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty().toLowerCase()).isEqualTo(expectedChange.getParty().toLowerCase());
            assertThat(actualChange.getAdded()).isEqualTo(expectedChange.getAdded());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());
        }
    }

    @Test
    public void inConsented_shouldUpdateRepresentationUpdateHistory_whenChangeCurrentlyUnpopulated_finrem() {
        // Arrange
        FinremCaseData finremCaseData = readFinremCaseData("consented-change-of-reps-before.json");
        FinremCaseData originalFinremCaseData = readFinremCaseData("consented-change-of-reps-original.json");
        final ChangedRepresentative addingChangedRepresentative = mock(ChangedRepresentative.class);
        when(addedSolicitorService.getAddedSolicitorAsCaseworker(finremCaseData))
            .thenReturn(addingChangedRepresentative);

        // Act
        noticeOfChangeService.updateRepresentation(finremCaseData, originalFinremCaseData, AUTH_TOKEN);

        // Verify
        // finremCaseData is updated. The following verification is performed on finremCaseData
        List<RepresentationUpdateHistoryCollection> actual = getRepresentationUpdateHistoryFinrem.apply(finremCaseData);

        RepresentationUpdate expectedNewElement = RepresentationUpdate.builder()
            .party("Applicant")
            .date(LocalDateTime.of(2020, 6, 1, 15, 0, 0))
            .clientName("John Smith")
            .by("Claire Mumford")
            .via("Notice of Change")
            .added(addingChangedRepresentative)
            .build();

        // Verify expected RepresentationUpdate appended
        RepresentationUpdate firstElement = actual.getLast().getValue();
        assertRepresentationUpdate(firstElement, expectedNewElement);
        assertChangeOrganisationField(finremCaseData, true,"A31PTVA", null);
    }

    @Test
    public void inConsented_shouldUpdateRepresentationUpdateHistory_whenChangeCurrentlyPopulated() throws Exception {
        setUpHelper();

        when(addedSolicitorService.getAddedSolicitorAsCaseworker(any(CaseDetails.class))).thenReturn(ChangedRepresentative.builder()
            .name("Sir Solicitor")
            .email("sirsolicitor1@gmail.com")
            .organisation(Organisation.builder()
                .organisationID("A31PTVA")
                .organisationName("FRApplicantSolicitorFirm")
                .build())
            .build());

        when(mockCaseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(true);
        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH + "consented-change-of-reps.json")) {
            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
            InputStream is = getClass().getResourceAsStream(PATH + "consented-change-of-reps-original.json");
            CaseDetails originalDetails = mapper.readValue(is, CallbackRequest.class).getCaseDetails();
            Map<String, Object> caseData = noticeOfChangeService.updateRepresentation(actualRequest.getCaseDetails(),
                AUTH_TOKEN, originalDetails);
            List<Element<RepresentationUpdate>> actual = getRepresentationUpdateHistory.apply(caseData);
            RepresentationUpdate actualChange = actual.get(1).getValue();
            RepresentationUpdate expectedChange = RepresentationUpdate.builder()
                .date(LocalDateTime.of(2020, 6, 1, 15, 0, 0))
                .party("Applicant")
                .clientName("John Smith")
                .by("Claire Mumford")
                .via("Notice of Change")
                .added(ChangedRepresentative.builder()
                    .email("sirsolicitor1@gmail.com")
                    .name("Sir Solicitor")
                    .organisation(Organisation.builder()
                        .organisationID("A31PTVA")
                        .organisationName("FRApplicantSolicitorFirm")
                        .build())
                    .build())
                .build();

            assertThat(actual).hasSize(2);
            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty().toLowerCase()).isEqualTo(expectedChange.getParty().toLowerCase());
            assertThat(actualChange.getAdded()).isEqualTo(expectedChange.getAdded());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());
        }
    }

    @Test
    public void inConsented_shouldUpdateRepresentationUpdateHistory_whenChangeCurrentlyPopulated_finrem() {
        // Arrange
        FinremCaseData finremCaseData = readFinremCaseData("consented-change-of-reps.json");
        FinremCaseData originalFinremCaseData = readFinremCaseData("consented-change-of-reps-original.json");
        final ChangedRepresentative addingChangedRepresentative = mock(ChangedRepresentative.class);
        when(addedSolicitorService.getAddedSolicitorAsCaseworker(finremCaseData)).thenReturn(addingChangedRepresentative);

        // Act
        noticeOfChangeService.updateRepresentation(finremCaseData, originalFinremCaseData, AUTH_TOKEN);

        // Verify
        // finremCaseData is updated. The following verification is performed on finremCaseData
        List<RepresentationUpdateHistoryCollection> actual = getRepresentationUpdateHistoryFinrem.apply(finremCaseData);
        assertThat(actual).hasSize(2);

        // Verify the original data to be kept
        RepresentationUpdate firstElement = actual.getFirst().getValue();
        RepresentationUpdate historyDataToBeKept = getRepresentationUpdateHistory.apply(
            readCaseDetailsData("consented-change-of-reps.json")
        ).getFirst().getValue();
        assertRepresentationUpdate(firstElement, historyDataToBeKept);

        // Verify the new history appended
        RepresentationUpdate lastElement = actual.getLast().getValue();
        RepresentationUpdate expectedNewElement = RepresentationUpdate.builder()
            .date(LocalDateTime.of(2020, 6, 1, 15, 0, 0))
            .party("Applicant")
            .clientName("John Smith")
            .by("Claire Mumford")
            .via("Notice of Change")
            .added(addingChangedRepresentative)
            .build();
        assertRepresentationUpdate(lastElement, expectedNewElement);
        assertThat(firstElement.getDate()).isBefore(lastElement.getDate());
        assertChangeOrganisationField(finremCaseData, true,"A31PTVA", null);
    }

    @Test
    public void shouldUpdateRepresentationUpdateHistory_whenNatureIsRemoving() throws Exception {
        setUpHelper();
        when(mockCaseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(true);

        when(removedSolicitorService.getRemovedSolicitorAsCaseworker(any(CaseDetails.class), eq(true))).thenReturn(
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
                AUTH_TOKEN,
                originalDetails);
            RepresentationUpdate actualChange = getRepresentationUpdateHistory.apply(caseData).getFirst().getValue();
            RepresentationUpdate expectedChange = RepresentationUpdate.builder()
                .party("applicant")
                .clientName("John Smith")
                .by("Claire Mumford")
                .via("Notice of Change")
                .removed(ChangedRepresentative.builder()
                    .email("sirsolicitor1@gmail.com")
                    .name("Sir Solicitor")
                    .organisation(Organisation.builder()
                        .organisationID("A31PTVA")
                        .organisationName("FRApplicantSolicitorFirm")
                        .build())
                    .build())
                .build();

            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty().toLowerCase()).isEqualTo(expectedChange.getParty().toLowerCase());
            assertThat(actualChange.getRemoved()).isEqualTo(expectedChange.getRemoved());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());
        }
    }

    @Test
    public void shouldUpdateRepresentationUpdateHistory_whenNatureIsRemoving_finrem() {
        // Arrange
        FinremCaseData finremCaseData = readFinremCaseData("change-of-reps-removing-before.json");
        FinremCaseData originalFinremCaseData = readFinremCaseData("change-of-reps-removing-original.json");
        final ChangedRepresentative mockedChangedRepresentative = mock(ChangedRepresentative.class);

        when(removedSolicitorService.getRemovedSolicitorAsCaseworker(originalFinremCaseData, true))
            .thenReturn(mockedChangedRepresentative);

        // Act
        noticeOfChangeService.updateRepresentation(finremCaseData, originalFinremCaseData, AUTH_TOKEN);

        // Verify
        List<RepresentationUpdateHistoryCollection> actual = getRepresentationUpdateHistoryFinrem.apply(finremCaseData);
        RepresentationUpdate actualChange = actual.getLast().getValue();
        RepresentationUpdate expectedChange = RepresentationUpdate.builder()
            .date(LocalDateTime.of(2020, 6, 1, 15, 0, 0))
            .party("Applicant")
            .clientName("John Smith")
            .by("Claire Mumford")
            .via("Notice of Change")
            .removed(mockedChangedRepresentative)
            .build();
        assertRepresentationUpdate(actualChange, expectedChange);
        assertChangeOrganisationField(finremCaseData, true,"A31PTVA", "A31PTVA");
    }

    @Test
    public void shouldUpdateRepresentationUpdateHistory_whenNatureIsReplacing() throws Exception {
        setUpHelper();

        when(addedSolicitorService.getAddedSolicitorAsCaseworker(any(CaseDetails.class))).thenReturn(
            ChangedRepresentative.builder()
                .name("TestAppSolName")
                .email("testappsol123@gmail.com")
                .organisation(Organisation.builder()
                    .organisationID("A31PTVU")
                    .organisationName("FRApplicantNewSolFirm")
                    .build())
                .build());
        when(removedSolicitorService.getRemovedSolicitorAsCaseworker(any(CaseDetails.class), eq(true))).thenReturn(
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
                AUTH_TOKEN,
                originalDetails);

            RepresentationUpdate actualChange = getRepresentationUpdateHistory.apply(caseData).getFirst().getValue();
            RepresentationUpdate expected = RepresentationUpdate.builder()
                .party("applicant")
                .clientName("John Smith")
                .by("Claire Mumford")
                .via("Notice of Change")
                .added(ChangedRepresentative.builder()
                    .email("testappsol123@gmail.com")
                    .name("TestAppSolName")
                    .organisation(Organisation.builder()
                        .organisationID("A31PTVU")
                        .organisationName("FRApplicantNewSolFirm")
                        .build())
                    .build())
                .removed(ChangedRepresentative.builder()
                    .email("sirsolicitor1@gmail.com")
                    .name("Sir Solicitor")
                    .organisation(Organisation.builder()
                        .organisationID("A31PTVA")
                        .organisationName("FRApplicantSolicitorFirm")
                        .build())
                    .build())
                .build();

            assertThat(actualChange.getClientName()).isEqualTo(expected.getClientName());
            assertThat(actualChange.getParty().toLowerCase()).isEqualTo(expected.getParty().toLowerCase());
            assertThat(actualChange.getAdded()).isEqualTo(expected.getAdded()); //added = old sol
            assertThat(actualChange.getRemoved().getOrganisation()).isEqualTo(expected.getRemoved().getOrganisation());
            assertThat(actualChange.getBy()).isEqualTo(expected.getBy());
        }
    }

    @Test
    public void shouldUpdateRepresentationUpdateHistory_whenNatureIsReplacing_finrem() {
        // Arrange
        FinremCaseData finremCaseData = readFinremCaseData("change-of-reps-replacing-before.json");
        FinremCaseData originalFinremCaseData = readFinremCaseData("change-of-reps-replacing-original.json");
        final ChangedRepresentative mockedAddedChangedRepresentative = mock(ChangedRepresentative.class);
        final ChangedRepresentative mockedRemovedChangedRepresentative = mock(ChangedRepresentative.class);

        when(addedSolicitorService.getAddedSolicitorAsCaseworker(finremCaseData))
            .thenReturn(mockedAddedChangedRepresentative);
        when(removedSolicitorService.getRemovedSolicitorAsCaseworker(originalFinremCaseData, true))
            .thenReturn(mockedRemovedChangedRepresentative);

        // Act
        noticeOfChangeService.updateRepresentation(finremCaseData, originalFinremCaseData, AUTH_TOKEN);

        // Verify
        List<RepresentationUpdateHistoryCollection> actual = getRepresentationUpdateHistoryFinrem.apply(finremCaseData);
        RepresentationUpdate actualChange = actual.getLast().getValue();
        RepresentationUpdate expected = RepresentationUpdate.builder()
            .party("Applicant")
            .date(LocalDateTime.of(2020, 6, 1, 15, 0, 0))
            .clientName("John Smith")
            .by("Claire Mumford")
            .via("Notice of Change")
            .added(mockedAddedChangedRepresentative)
            .removed(mockedRemovedChangedRepresentative)
            .build();

        assertRepresentationUpdate(actualChange, expected);
        assertChangeOrganisationField(finremCaseData, true,"A31PTVU", "A31PTVA");
    }

    @Test
    public void shouldUpdateRepresentationUpdateHistoryRespondent() throws Exception {
        when(mockIdamService.getIdamFullName(any())).thenReturn("Claire Mumford");
        when(mockCaseDataService.isApplicantRepresentedByASolicitor(anyMap())).thenReturn(true);
        when(mockCaseDataService.isRespondentRepresentedByASolicitor(anyMap())).thenReturn(true);
        when(mockCaseDataService.buildFullRespondentName(any(CaseDetails.class))).thenReturn("Jane Smith");
        when(addedSolicitorService.getAddedSolicitorAsCaseworker(any(CaseDetails.class))).thenReturn(
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
                AUTH_TOKEN,
                originalDetails);
            RepresentationUpdate actualChange = getRepresentationUpdateHistory.apply(caseData).getFirst().getValue();
            RepresentationUpdate expected = RepresentationUpdate.builder()
                .party("respondent")
                .clientName("Jane Smith")
                .by("Claire Mumford")
                .via("Notice of Change")
                .added(ChangedRepresentative.builder()
                    .email("padmaja.ramisetti@gmail.com")
                    .name("Test respondent Solicitor")
                    .organisation(Organisation.builder()
                        .organisationID("A31PTVU")
                        .organisationName("FRRespondentSolicitorFirm")
                        .build())
                    .build())
                .build();

            assertThat(actualChange.getClientName()).isEqualTo(expected.getClientName());
            assertThat(actualChange.getParty().toLowerCase()).isEqualTo(expected.getParty().toLowerCase());
            assertThat(actualChange.getAdded()).isEqualTo(expected.getAdded());
            assertThat(actualChange.getBy()).isEqualTo(expected.getBy());
        }
    }

    @Test
    public void shouldUpdateRepresentationUpdateHistoryRespondent_finrem() {
        // Arrange
        FinremCaseData finremCaseData = readFinremCaseData("change-of-representatives-respondent-before.json");
        FinremCaseData originalFinremCaseData = readFinremCaseData("change-of-representatives-respondent-original.json");
        final ChangedRepresentative mockedAddedChangedRepresentative = mock(ChangedRepresentative.class);

        when(addedSolicitorService.getAddedSolicitorAsCaseworker(finremCaseData))
            .thenReturn(mockedAddedChangedRepresentative);

        // Act
        noticeOfChangeService.updateRepresentation(finremCaseData, originalFinremCaseData, AUTH_TOKEN);

        List<RepresentationUpdateHistoryCollection> actual = getRepresentationUpdateHistoryFinrem.apply(finremCaseData);
        RepresentationUpdate actualChange = actual.getLast().getValue();
        RepresentationUpdate expected = RepresentationUpdate.builder()
            .party("respondent")
            .clientName("Jane Smith")
            .by("Claire Mumford")
            .via("Notice of Change")
            .added(mockedAddedChangedRepresentative)
            .build();

        assertRepresentationUpdate(actualChange, expected);
        assertChangeOrganisationField(finremCaseData, false,"A31PTVU", null);
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
            new TypeReference<>() {
            });
    }

    private List<RepresentationUpdateHistoryCollection> convertToUpdateHistoryFinrem(FinremCaseData caseData) {
        return caseData.getRepresentationUpdateHistory();
    }

    private void setUpHelper() {
        when(mockCaseDataService.buildFullApplicantName(any(CaseDetails.class))).thenReturn("John Smith");
        when(mockCaseDataService.buildFullRespondentName(any(CaseDetails.class))).thenReturn("Jane Smith");
        when(mockCaseDataService.isApplicantRepresentedByASolicitor(anyMap())).thenReturn(true);
        when(mockCaseDataService.isRespondentRepresentedByASolicitor(anyMap())).thenReturn(true);
        when(mockCaseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(false);
    }

    private FinremCaseData readFinremCaseData(String fileName) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH + fileName)) {
            FinremCallbackRequest actualRequest = mapper.readValue(resourceAsStream, FinremCallbackRequest.class);
            FinremCaseData ret = actualRequest.getCaseDetails().getData();
            ret.setCcdCaseType(actualRequest.getCaseDetails().getCaseType());
            return ret;
        } catch (Exception exception) {
            throw new IllegalStateException("Fail to read FinremCaseData from the given JSON file", exception);
        }
    }

    private Map<String, Object> readCaseDetailsData(String fileName) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH + fileName)) {
            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
            return actualRequest.getCaseDetails().getData();
        } catch (Exception exception) {
            throw new IllegalStateException("Fail to read CaseDetails.data from the given JSON file", exception);
        }
    }

    private void assertRepresentationUpdate(RepresentationUpdate newElement, RepresentationUpdate expectedNewElement) {
        assertThat(newElement)
            .usingRecursiveComparison()
            .ignoringFields("date")
            .withComparatorForFields(String.CASE_INSENSITIVE_ORDER, "party")
            .isEqualTo(expectedNewElement);
    }

    private void assertChangeOrganisationField(FinremCaseData finremCaseData, boolean isApplicant,
                                               String organisationIdToAdd, String organisationIdToRemove) {

        String role = isApplicant ? CaseRole.APP_SOLICITOR.getCcdCode() : CaseRole.RESP_SOLICITOR.getCcdCode();

        assertThat(finremCaseData.getChangeOrganisationRequestField())
            .extracting(ChangeOrganisationRequest::getCaseRoleId,
                ChangeOrganisationRequest::getApprovalStatus,
                ChangeOrganisationRequest::getRequestTimestamp,
                ChangeOrganisationRequest::getApprovalRejectionTimestamp
            )
            .contains(
                DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .code(role)
                        .label(role)
                        .build())
                    .listItems(List.of(DynamicListElement.builder()
                        .code(role)
                        .label(role)
                        .build()))
                    .build(),
                ChangeOrganisationApprovalStatus.APPROVED,
                LocalDateTime.of(2024, 1, 1, 12, 0),
                LocalDateTime.of(2024, 1, 1, 12, 0));

        if (organisationIdToAdd != null) {
            assertThat(finremCaseData.getChangeOrganisationRequestField().getOrganisationToAdd())
                .extracting(Organisation::getOrganisationID)
                .isEqualTo(organisationIdToAdd);
        } else {
            assertThat(finremCaseData.getChangeOrganisationRequestField().getOrganisationToAdd()).isNull();
        }

        assertThat(Optional.ofNullable(finremCaseData.getChangeOrganisationRequestField().getOrganisationToAdd())
            .map(Organisation::getOrganisationID)
            .orElse(null))
            .isEqualTo(organisationIdToAdd);

        assertThat(Optional.ofNullable(finremCaseData.getChangeOrganisationRequestField().getOrganisationToRemove())
            .map(Organisation::getOrganisationID)
            .orElse(null))
            .isEqualTo(organisationIdToRemove);
    }
}
