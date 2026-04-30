package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistory;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.events.AuditEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationContactInformation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerRepresentationChecker;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.NoticeOfChangeInvalidRequestException;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.AddedSolicitorService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.RemovedSolicitorService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHANGE_ORGANISATION_REQUEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_DX_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_DX_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.IS_NOC_REJECTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_DX_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element.element;

@ExtendWith(MockitoExtension.class)
class UpdateRepresentationServiceTest {

    private static final String PATH = "/fixtures/noticeOfChange/";
    private static final String NOC_EVENT = "nocRequest";

    public static final String ADDRESS_LINE_1 = "addressLine1";
    public static final String ADDRESS_LINE_2 = "addressLine2";
    public static final String ADDRESS_LINE_3 = "addressLine3";
    public static final String COUNTY = "county";
    public static final String COUNTRY = "country";
    public static final String TOWN_CITY = "townCity";
    public static final String POSTCODE = "postCode";

    private static final String NOTICE_OF_CHANGE = "Notice of Change";
    private static final String REPRESENTATION_UPDATE_HISTORY = "RepresentationUpdateHistory";

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private ObjectMapper mapper;

    @InjectMocks
    private UpdateRepresentationService updateRepresentationService;

    @Mock
    private AuditEventService auditEventService;

    @Mock
    private IdamAuthService idamClient;

    @Mock
    private CaseDataService caseDataService;

    @Mock
    private PrdOrganisationService organisationService;

    @Mock
    private UpdateSolicitorDetailsService updateSolicitorDetailsService;

    @Mock
    private ChangeOfRepresentationService changeOfRepresentationService;

    @Mock
    private AddedSolicitorService addedSolicitorService;

    @Mock
    private RemovedSolicitorService removedSolicitorService;

    @Mock
    private BarristerRepresentationChecker barristerRepresentationChecker;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    private UserDetails testAppSolicitor;
    private UserDetails testRespSolicitor;

    private Organisation applicantOrg;
    private Organisation respondentOrg;
    private AuditEvent testAuditEvent;
    private OrganisationsResponse orgResponse;

    private final Function<Map<String, Object>, List<Element<RepresentationUpdate>>> getFirstChangeElement =
        this::convertToChangeOfRepresentation;

    OrganisationContactInformation organisationContactInformation = OrganisationContactInformation.builder()
        .addressLine1(ADDRESS_LINE_1)
        .addressLine2(ADDRESS_LINE_2)
        .addressLine3(ADDRESS_LINE_3)
        .county(COUNTY)
        .country(COUNTRY)
        .townCity(TOWN_CITY)
        .postcode(POSTCODE)
        .build();

    CaseDetails initialDetails;
    Map<String, Object> expectedCaseData;

    @BeforeEach
    void setUp() {
        mapper = objectMapper;

        testAppSolicitor = UserDetails.builder()
            .forename("Sir")
            .surname("Solicitor")
            .email("sirsolicitor1@gmail.com")
            .build();

        testRespSolicitor = UserDetails.builder()
            .forename("Test respondent")
            .surname("Solicitor")
            .email("padmaja.ramisetti@gmail.com")
            .build();

        applicantOrg = Organisation.builder()
            .organisationName("FRApplicantSolicitorFirm")
            .organisationID("A31PTVA")
            .build();

        respondentOrg = Organisation.builder()
            .organisationName("FRRespondentSolicitorFirm")
            .organisationID("A31PTVU")
            .build();

        testAuditEvent = AuditEvent.builder()
            .userId("randomID")
            .build();

        orgResponse = OrganisationsResponse.builder()
            .contactInformation(List.of(organisationContactInformation))
            .name("FRApplicantSolicitorFirm")
            .organisationIdentifier("FRApplicantSolicitorFirm")
            .build();
    }

    private void setUpCaseDetails(String fileName) throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream(PATH + fileName)) {
            initialDetails = mapper.readValue(resourceAsStream, CallbackRequest.class)
                .getCaseDetails();
            expectedCaseData = initialDetails.getData();
        }
    }

    @Test
    void givenContestedCaseAndEmptyChangeOfRepsForApplicant_WhenUpdateRepresentation_thenReturnCorrectCaseData() throws Exception {
        setUpDefaultMockContext();
        setUpCaseDetails("contestedAppSolicitorAdding/after-update-details.json");

        InputStream resourceAsStream = getClass().getResourceAsStream(PATH
            + "contestedAppSolicitorAdding/change-of-representatives-before.json");
        initialDetails = mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();

        Map<String, Object> actualData = updateRepresentationService
            .updateRepresentationAsSolicitor(initialDetails, "bebe");

        assertEquals(actualData.get(CONTESTED_SOLICITOR_NAME), expectedCaseData.get(CONTESTED_SOLICITOR_NAME));
        assertEquals(actualData.get(CONTESTED_SOLICITOR_EMAIL), expectedCaseData.get(CONTESTED_SOLICITOR_EMAIL));
        assertNull(actualData.get(CONTESTED_SOLICITOR_DX_NUMBER));
        assertNull(actualData.get(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED));
        assertNull(actualData.get(SOLICITOR_PHONE));

        RepresentationUpdate actualChangeOfRep = getFirstChangeElement.apply(actualData).getFirst().getValue();
        RepresentationUpdate expectedChangeOfRep = getFirstChangeElement.apply(expectedCaseData).getFirst().getValue();

        assertEquals(actualChangeOfRep.getParty().toLowerCase(), expectedChangeOfRep.getParty().toLowerCase());
        assertEquals(actualChangeOfRep.getClientName(), expectedChangeOfRep.getClientName());
        assertEquals(actualChangeOfRep.getBy(), expectedChangeOfRep.getBy());
        assertEquals(actualChangeOfRep.getAdded(), expectedChangeOfRep.getAdded());
        assertEquals(actualChangeOfRep.getRemoved(), expectedChangeOfRep.getRemoved());

        Address solicitorAddress = mapper.convertValue(actualData.get(CONTESTED_SOLICITOR_ADDRESS), Address.class);
        assertEquals(ADDRESS_LINE_1, solicitorAddress.getAddressLine1());
        assertEquals(TOWN_CITY, solicitorAddress.getPostTown());
        assertEquals(COUNTY, solicitorAddress.getCounty());
        assertEquals(COUNTRY, solicitorAddress.getCountry());
        assertEquals(POSTCODE, solicitorAddress.getPostCode());
    }

    @Test
    void givenContestedCaseAndEmptyChangeOfRepsForInterveners_WhenUpdateRepresentation_thenReturnCorrectCaseData() {
        Map<String, String> intervenersJson = Map.of("contestedIntvr1SolicitorAdding/after-update-details.json",
            "contestedIntvr1SolicitorAdding/change-of-representatives-before.json",
            "contestedIntvr2SolicitorAdding/after-update-details.json",
            "contestedIntvr2SolicitorAdding/change-of-representatives-before.json",
            "contestedIntvr3SolicitorAdding/after-update-details.json",
            "contestedIntvr3SolicitorAdding/change-of-representatives-before.json",
            "contestedIntvr4SolicitorAdding/after-update-details.json",
            "contestedIntvr4SolicitorAdding/change-of-representatives-before.json");

        intervenersJson.forEach((key, value) -> {
            try {
                setUpDefaultMockContext();
                setUpCaseDetails(key);
                InputStream resourceAsStream = getClass().getResourceAsStream(PATH + value);
                initialDetails = mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();

                Map<String, Object> actualData = updateRepresentationService
                    .updateRepresentationAsSolicitor(initialDetails, "bebe");

                assertEquals(actualData.get(CONTESTED_SOLICITOR_NAME), expectedCaseData.get(CONTESTED_SOLICITOR_NAME));
                assertEquals(actualData.get(CONTESTED_SOLICITOR_EMAIL), expectedCaseData.get(CONTESTED_SOLICITOR_EMAIL));
                assertNull(actualData.get(CONTESTED_SOLICITOR_DX_NUMBER));
                assertNull(actualData.get(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED));
                assertNull(actualData.get(SOLICITOR_PHONE));

                RepresentationUpdate actualChangeOfRep = getFirstChangeElement.apply(actualData).getFirst().getValue();
                RepresentationUpdate expectedChangeOfRep = getFirstChangeElement.apply(expectedCaseData).getFirst().getValue();

                assertEquals(actualChangeOfRep.getParty().toLowerCase(), expectedChangeOfRep.getParty().toLowerCase());
                assertEquals(actualChangeOfRep.getClientName(), expectedChangeOfRep.getClientName());
                assertEquals(actualChangeOfRep.getBy(), expectedChangeOfRep.getBy());
                assertEquals(actualChangeOfRep.getAdded(), expectedChangeOfRep.getAdded());
                assertEquals(actualChangeOfRep.getRemoved(), expectedChangeOfRep.getRemoved());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

    @Test
    void addRemovedSolicitorOrganisationFieldToCaseDataTest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();

        updateRepresentationService.addRemovedSolicitorOrganisationFieldToCaseData(caseDetails);
        assertNull(caseData.get("changeOrganisationRequestField"));

        ChangeOrganisationRequest changeRequest = ChangeOrganisationRequest.builder()
            .organisationToRemove(Organisation.builder().organisationID("org1").build())
            .build();
        caseData.put("changeOrganisationRequestField", changeRequest);
        updateRepresentationService.addRemovedSolicitorOrganisationFieldToCaseData(caseDetails);
        assertEquals("org1", ((ChangeOrganisationRequest) caseData.get("changeOrganisationRequestField"))
            .getOrganisationToRemove().getOrganisationID());

        changeRequest = ChangeOrganisationRequest.builder().build();
        caseData.put("changeOrganisationRequestField", changeRequest);
        updateRepresentationService.addRemovedSolicitorOrganisationFieldToCaseData(caseDetails);
        assertNull(((ChangeOrganisationRequest) caseData.get("changeOrganisationRequestField"))
            .getOrganisationToRemove().getOrganisationID());
    }

    @Test
    void givenConsentedCaseAndEmptyChangeOfReps_WhenUpdateRepresentation_thenReturnCorrectCaseData() throws Exception {
        String fixture = "consentedAppSolicitorAdding";
        setUpMockContext(testAppSolicitor, orgResponse, this::getChangeOfRepsAppContested, fixture, true);
        when(addedSolicitorService.getAddedSolicitorAsSolicitor(any(), any())).thenReturn(
            ChangedRepresentative.builder()
                .name(testAppSolicitor.getFullName())
                .email(testAppSolicitor.getEmail())
                .organisation(Organisation.builder()
                    .organisationID("A31PTVA")
                    .organisationName("FRApplicantSolicitorFirm")
                    .build())
                .build()
        );
        setUpCaseDetails("consentedAppSolicitorAdding/after-update-details.json");
        try (InputStream resourceAsStream = getClass()
            .getResourceAsStream(PATH + "consentedAppSolicitorAdding/change-of-representatives-before.json")) {
            initialDetails = mapper.readValue(resourceAsStream, CallbackRequest.class)
                .getCaseDetails();
        }

        Map<String, Object> actualData = updateRepresentationService
            .updateRepresentationAsSolicitor(initialDetails, "bebe");

        assertEquals(actualData.get(CONSENTED_SOLICITOR_NAME), expectedCaseData.get(CONSENTED_SOLICITOR_NAME));
        assertEquals(actualData.get(SOLICITOR_EMAIL), expectedCaseData.get(SOLICITOR_EMAIL));
        assertNull(actualData.get(CONSENTED_SOLICITOR_DX_NUMBER));
        assertNull(actualData.get(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED));
        assertNull(actualData.get(SOLICITOR_PHONE));

        RepresentationUpdate actualChangeOfRep = getFirstChangeElement.apply(actualData).getFirst().getValue();
        RepresentationUpdate expectedChangeOfRep = getFirstChangeElement.apply(expectedCaseData).getFirst().getValue();

        assertEquals(actualChangeOfRep.getParty().toLowerCase(), expectedChangeOfRep.getParty().toLowerCase());
        assertEquals(actualChangeOfRep.getClientName(), expectedChangeOfRep.getClientName());
        assertEquals(actualChangeOfRep.getBy(), expectedChangeOfRep.getBy());
        assertEquals(actualChangeOfRep.getAdded(), expectedChangeOfRep.getAdded());
        assertEquals(actualChangeOfRep.getRemoved(), expectedChangeOfRep.getRemoved());

        Address solicitorAddress = mapper.convertValue(actualData.get(CONSENTED_SOLICITOR_ADDRESS), Address.class);
        assertEquals(ADDRESS_LINE_1, solicitorAddress.getAddressLine1());
        assertEquals(TOWN_CITY, solicitorAddress.getPostTown());
        assertEquals(COUNTY, solicitorAddress.getCounty());
        assertEquals(COUNTRY, solicitorAddress.getCountry());
        assertEquals(POSTCODE, solicitorAddress.getPostCode());
    }

    @Test
    void givenConsentedCaseAndEmptyChangeOfReps_WhenChangeOfRequestCaseRoleIdIsNull_thenThrowException() {
        when(auditEventService.getLatestAuditEventByName(any(), eq(NOC_EVENT))).thenReturn(Optional.of(testAuditEvent));
        Map mockedMap = mock(Map.class);
        CaseDetails caseDetails = mock(CaseDetails.class);
        when(caseDetails.getId()).thenReturn(CASE_ID_IN_LONG);
        when(caseDetails.getData()).thenReturn(mockedMap);
        when(finremCaseDetailsMapper.mapToFinremCaseData(mockedMap)).thenReturn(FinremCaseData.builder().build());

        NoticeOfChangeInvalidRequestException ex = assertThrows(NoticeOfChangeInvalidRequestException.class, () -> updateRepresentationService
            .updateRepresentationAsSolicitor(caseDetails, AUTH_TOKEN));

        String expectedMessage = "1234567890 - unexpected empty caseRoleId";
        String actualMessage = ex.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void givenConsentedCaseAndEmptyChangeOfReps_WhenChangeOfRequestCaseRoleIdIsUnrecognised_thenThrowException()
        throws Exception {
        String fixture = "consentedAppSolicitorAdding";
        setUpExceptionMockContext(testAppSolicitor, orgResponse);
        when(addedSolicitorService.getAddedSolicitorAsSolicitor(any(), any())).thenReturn(
            ChangedRepresentative.builder()
                .name(testAppSolicitor.getFullName())
                .email(testAppSolicitor.getEmail())
                .organisation(Organisation.builder()
                    .organisationID("A31PTVA")
                    .organisationName("FRApplicantSolicitorFirm")
                    .build())
                .build()
        );
        setUpCaseDetails(fixture + "/after-update-details.json");
        try (InputStream resourceAsStream = getClass()
            .getResourceAsStream(PATH + fixture + "/change-of-representatives-before.json")) {
            initialDetails = mapper.readValue(resourceAsStream, CallbackRequest.class)
                .getCaseDetails();
            Map<String, Object> changeOrganisationRequestField = (Map<String, Object>) initialDetails.getData().get("changeOrganisationRequestField");
            Map<String, Object> caseRoleId = (Map<String, Object>) changeOrganisationRequestField.get("CaseRoleId");
            caseRoleId.put("value", Map.of("code", "[INTVRSOLICITOR5]", "label", "[INTVRSOLICITOR5]"));
        }

        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class, () -> updateRepresentationService
            .updateRepresentationAsSolicitor(initialDetails, "bebe"));

        String expectedMessage = "12345678 - Unrecognised caseRoleId: [INTVRSOLICITOR5]";
        String actualMessage = ex.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void givenEmptyChangeOfRepsAndRespSolicitor_WhenUpdateRepresentation_thenReturnCorrectCaseData() throws Exception {
        String fixture = "RespSolicitorAdding";
        setUpMockContext(testRespSolicitor, orgResponse, this::getChangeOfRepsRespondent, fixture, false);
        when(addedSolicitorService.getAddedSolicitorAsSolicitor(any(), any())).thenReturn(
            ChangedRepresentative.builder()
                .name(testRespSolicitor.getFullName())
                .email(testRespSolicitor.getEmail())
                .organisation(Organisation.builder()
                    .organisationID("A31PTVU")
                    .organisationName("FRRespondentSolicitorFirm")
                    .build())
                .build()
        );
        setUpCaseDetails("RespSolicitorAdding/after-update-details.json");
        InputStream resourceAsStream = getClass()
            .getResourceAsStream(PATH + "RespSolicitorAdding/change-of-representatives-before.json");
        initialDetails = mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();

        Map<String, Object> actualData = updateRepresentationService
            .updateRepresentationAsSolicitor(initialDetails, "bebe");

        assertEquals(actualData.get(RESP_SOLICITOR_NAME), expectedCaseData.get(RESP_SOLICITOR_NAME));
        assertEquals(actualData.get(RESP_SOLICITOR_EMAIL), expectedCaseData.get(RESP_SOLICITOR_EMAIL));
        assertNull(actualData.get(RESP_SOLICITOR_DX_NUMBER));
        assertNull(actualData.get(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT));
        assertNull(actualData.get(RESP_SOLICITOR_PHONE));

        RepresentationUpdate actualChangeOfRep = getFirstChangeElement.apply(actualData).getFirst().getValue();
        RepresentationUpdate expectedChangeOfRep = getFirstChangeElement.apply(expectedCaseData).getFirst().getValue();

        assertEquals(actualChangeOfRep.getParty().toLowerCase(), expectedChangeOfRep.getParty().toLowerCase());
        assertEquals(actualChangeOfRep.getClientName(), expectedChangeOfRep.getClientName());
        assertEquals(actualChangeOfRep.getBy(), expectedChangeOfRep.getBy());
        assertEquals(actualChangeOfRep.getAdded(), expectedChangeOfRep.getAdded());
        assertEquals(actualChangeOfRep.getRemoved(), expectedChangeOfRep.getRemoved());

        Address solicitorAddress = mapper.convertValue(actualData.get(RESP_SOLICITOR_ADDRESS), Address.class);
        assertEquals(ADDRESS_LINE_1, solicitorAddress.getAddressLine1());
        assertEquals(TOWN_CITY, solicitorAddress.getPostTown());
        assertEquals(COUNTY, solicitorAddress.getCounty());
        assertEquals(COUNTRY, solicitorAddress.getCountry());
        assertEquals(POSTCODE, solicitorAddress.getPostCode());

    }

    @Test
    void givenPopulatedChangeOfReps_WhenUpdateRepresentation_thenReturnCorrectCaseData() throws Exception {
        UserDetails replacingSolicitor = UserDetails.builder().forename("Test Applicant").surname("Solicitor")
            .email("appsolicitor1@yahoo.com").build();

        Organisation secondAppOrg = Organisation.builder().organisationName("FRApplicantSolicitorFirm2")
            .organisationID("A31PTVU").build();

        OrganisationsResponse secondOrgResponse = OrganisationsResponse.builder()
            .contactInformation(List.of(organisationContactInformation)).name("FRApplicantSolicitorFirm2")
            .organisationIdentifier("FRApplicantSolicitorFirm2").build();

        setUpMockContextReplacing(replacingSolicitor, secondOrgResponse, secondAppOrg);
        when(addedSolicitorService.getAddedSolicitorAsSolicitor(any(), any())).thenReturn(
            ChangedRepresentative.builder()
                .name(replacingSolicitor.getFullName())
                .email(replacingSolicitor.getEmail())
                .organisation(secondAppOrg)
                .build());
        when(removedSolicitorService.getRemovedSolicitorAsSolicitor(any(), any())).thenReturn(
            ChangedRepresentative.builder()
                .name(testAppSolicitor.getFullName())
                .email(testAppSolicitor.getEmail())
                .organisation(Organisation.builder()
                    .organisationID("A31PTVA")
                    .organisationName("FRApplicantSolicitorFirm")
                    .build())
                .build());

        setUpCaseDetails("AppSolReplacing/after-update-details.json");

        InputStream resourceAsStream = getClass().getResourceAsStream(PATH
            + "AppSolReplacing/change-of-representatives-before.json");
        initialDetails = mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();

        Map<String, Object> actualData = updateRepresentationService
            .updateRepresentationAsSolicitor(initialDetails, "someAuthToken");

        assertEquals("Test Applicant Solicitor", actualData.get(CONTESTED_SOLICITOR_NAME));
        assertEquals("appsolicitor1@yahoo.com", actualData.get(CONTESTED_SOLICITOR_EMAIL));
        assertNull(actualData.get(CONTESTED_SOLICITOR_DX_NUMBER));
        assertNull(actualData.get(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED));
        assertNull(actualData.get(SOLICITOR_PHONE));

        RepresentationUpdate actualChangeOfRep = getFirstChangeElement.apply(actualData).getFirst().getValue();
        RepresentationUpdate expectedChangeOfRep = getFirstChangeElement.apply(expectedCaseData).getFirst().getValue();

        assertEquals(actualChangeOfRep.getParty().toLowerCase(), expectedChangeOfRep.getParty().toLowerCase());
        assertEquals(actualChangeOfRep.getClientName(), expectedChangeOfRep.getClientName());
        assertEquals(actualChangeOfRep.getBy(), expectedChangeOfRep.getBy());
        assertEquals(actualChangeOfRep.getAdded(), expectedChangeOfRep.getAdded());
        assertEquals(actualChangeOfRep.getRemoved(), expectedChangeOfRep.getRemoved());

        Address solicitorAddress = mapper.convertValue(actualData.get(CONTESTED_SOLICITOR_ADDRESS), Address.class);
        assertEquals(ADDRESS_LINE_1, solicitorAddress.getAddressLine1());
        assertEquals(TOWN_CITY, solicitorAddress.getPostTown());
        assertEquals(COUNTY, solicitorAddress.getCounty());
        assertEquals(COUNTRY, solicitorAddress.getCountry());
        assertEquals(POSTCODE, solicitorAddress.getPostCode());
    }

    @Test
    void givenUserHasRepresentedLitigantAsBarrister_whenUpdateRepresentation_thenRejectChangeOrgRequest() throws Exception {
        when(auditEventService.getLatestAuditEventByName(any(), eq(NOC_EVENT))).thenReturn(Optional.of(testAuditEvent));
        when(idamClient.getUserByUserId(any(), eq(testAuditEvent.getUserId()))).thenReturn(testAppSolicitor);
        when(barristerRepresentationChecker.hasUserBeenBarristerOnCase(any(), eq(testAppSolicitor))).thenReturn(true);

        InputStream resourceAsStream = getClass().getResourceAsStream(PATH
            + "contestedAppSolicitorAdding/change-of-representatives-before.json");
        initialDetails = mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();

        Map<String, Object> actualData = updateRepresentationService.updateRepresentationAsSolicitor(initialDetails, AUTH_TOKEN);

        ChangeOrganisationRequest changeOrganisationRequest =
            mapper.convertValue(actualData.get(CHANGE_ORGANISATION_REQUEST), ChangeOrganisationRequest.class);

        assertThat(changeOrganisationRequest.getApprovalStatus(), is(ChangeOrganisationApprovalStatus.REJECTED));
        assertThat(actualData.get(IS_NOC_REJECTED), is(YES_VALUE));
    }

    private List<Element<RepresentationUpdate>> convertToChangeOfRepresentation(Map<String, Object> data) {
        return mapper.convertValue(data.get(REPRESENTATION_UPDATE_HISTORY),
            new TypeReference<>() {
            });
    }

    private void setUpDefaultMockContext() throws Exception {
        // Precompute values that use the @Spy mapper BEFORE any when(...).thenReturn(...)
        Map<String, Object> solicitorAddressData = prepareSolAddressData(orgResponse);
        Map<String, Object> updatedContactData = getUpdatedContactData("contestedAppSolicitorAdding");
        RepresentationUpdateHistory repUpdateHistory = getChangeOfRepsAppContested();

        when(auditEventService.getLatestAuditEventByName(any(), eq(NOC_EVENT))).thenReturn(Optional.of(testAuditEvent));
        when(idamClient.getUserByUserId(any(), eq(testAuditEvent.getUserId()))).thenReturn(testAppSolicitor);
        when(organisationService.findOrganisationByOrgId(any())).thenReturn(orgResponse);
        when(updateSolicitorDetailsService.convertOrganisationAddressToSolicitorAddress(orgResponse))
            .thenReturn(solicitorAddressData);
        when(changeOfRepresentationService.generateRepresentationUpdateHistory(any()))
            .thenReturn(repUpdateHistory);
        when(updateSolicitorDetailsService.updateSolicitorContactDetails(any(), any(), anyBoolean(), anyBoolean()))
            .thenReturn(updatedContactData);
        when(updateSolicitorDetailsService.removeSolicitorFields(any(), anyBoolean(), anyBoolean()))
            .thenReturn(updatedContactData);
        when(addedSolicitorService.getAddedSolicitorAsSolicitor(any(), any())).thenReturn(
            ChangedRepresentative.builder()
                .name("Sir Solicitor")
                .email("sirsolicitor1@gmail.com")
                .organisation(Organisation.builder()
                    .organisationID("A31PTVA")
                    .organisationName("FRApplicantSolicitorFirm")
                    .build())
                .build());
    }

    private void setUpMockContext(UserDetails solicitor,
                                  OrganisationsResponse orgResponse,
                                  Supplier<RepresentationUpdateHistory> supplier,
                                  String fixture,
                                  boolean isConsented) throws Exception {
        // Precompute values that use the @Spy mapper BEFORE any when(...).thenReturn(...)
        Map<String, Object> solicitorAddressData = prepareSolAddressData(orgResponse);
        Map<String, Object> updatedContactData = getUpdatedContactData(fixture);
        RepresentationUpdateHistory repUpdateHistory = supplier.get();

        when(auditEventService.getLatestAuditEventByName(any(), eq(NOC_EVENT))).thenReturn(Optional.of(testAuditEvent));
        when(idamClient.getUserByUserId(any(), eq(testAuditEvent.getUserId()))).thenReturn(solicitor);
        when(organisationService.findOrganisationByOrgId(any())).thenReturn(orgResponse);
        when(updateSolicitorDetailsService.convertOrganisationAddressToSolicitorAddress(orgResponse))
            .thenReturn(solicitorAddressData);
        when(changeOfRepresentationService.generateRepresentationUpdateHistory(any()))
            .thenReturn(repUpdateHistory);
        when(updateSolicitorDetailsService.updateSolicitorContactDetails(any(), any(), anyBoolean(), anyBoolean()))
            .thenReturn(updatedContactData);
        when(updateSolicitorDetailsService.removeSolicitorFields(any(), anyBoolean(), anyBoolean()))
            .thenReturn(updatedContactData);
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(isConsented);
    }

    private void setUpExceptionMockContext(UserDetails solicitor,
                                  OrganisationsResponse orgResponse) {

        when(auditEventService.getLatestAuditEventByName(any(), eq(NOC_EVENT))).thenReturn(Optional.of(testAuditEvent));
        when(idamClient.getUserByUserId(any(), eq(testAuditEvent.getUserId()))).thenReturn(solicitor);
        lenient().when(organisationService.findOrganisationByOrgId(any())).thenReturn(orgResponse);
    }

    private void setUpMockContextReplacing(UserDetails newSolicitor,
                                           OrganisationsResponse orgResponse,
                                           Organisation newSolicitorOrg) throws Exception {
        // Precompute values that use the @Spy mapper BEFORE any when(...).thenReturn(...)
        Map<String, Object> solicitorAddressData = prepareSolAddressData(orgResponse);
        Map<String, Object> updatedContactData = getUpdatedContactData("AppSolReplacing");
        RepresentationUpdateHistory repUpdateHistory = getChangeOfRepsReplacingApplicant(newSolicitor, newSolicitorOrg);

        when(auditEventService.getLatestAuditEventByName(any(), eq(NOC_EVENT))).thenReturn(Optional.of(testAuditEvent));
        when(idamClient.getUserByUserId(any(), eq(testAuditEvent.getUserId()))).thenReturn(newSolicitor);
        when(organisationService.findOrganisationByOrgId(any())).thenReturn(orgResponse);
        when(updateSolicitorDetailsService.convertOrganisationAddressToSolicitorAddress(orgResponse))
            .thenReturn(solicitorAddressData);
        when(changeOfRepresentationService.generateRepresentationUpdateHistory(any()))
            .thenReturn(repUpdateHistory);
        when(updateSolicitorDetailsService.updateSolicitorContactDetails(any(), any(), anyBoolean(), anyBoolean()))
            .thenReturn(updatedContactData);
        when(updateSolicitorDetailsService.removeSolicitorFields(any(), anyBoolean(), anyBoolean()))
            .thenReturn(updatedContactData);
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(false);
    }

    private Map<String, Object> prepareSolAddressData(OrganisationsResponse organisationData) {
        OrganisationContactInformation contactInformation = organisationData.getContactInformation().getFirst();
        return mapper.convertValue(Address.builder()
            .addressLine1(contactInformation.getAddressLine1())
            .addressLine2(contactInformation.getAddressLine2())
            .addressLine3(contactInformation.getAddressLine3())
            .county(contactInformation.getCounty())
            .country(contactInformation.getCountry())
            .postTown(contactInformation.getTownCity())
            .postCode(contactInformation.getPostcode())
            .build(), Map.class);
    }

    private Map<String, Object> getUpdatedContactData(String subPath) throws Exception {
        InputStream resourceAsStream = getClass()
            .getResourceAsStream(PATH + subPath + "/after-update-details.json");
        return mapper.readValue(resourceAsStream, CallbackRequest.class)
            .getCaseDetails().getData();
    }

    private RepresentationUpdateHistory getChangeOfRepsAppContested() {
        ChangedRepresentative added = ChangedRepresentative.builder()
            .name(testAppSolicitor.getFullName())
            .email(testAppSolicitor.getEmail())
            .organisation(applicantOrg)
            .build();
        LocalDateTime date = LocalDateTime.of(LocalDate.of(2020, 6, 1),
            LocalTime.of(15, 0));
        return RepresentationUpdateHistory.builder().representationUpdateHistory(
            List.of(element(UUID.randomUUID(),
                RepresentationUpdate.builder()
                    .party("applicant")
                    .clientName("John Smith")
                    .date(date)
                    .added(added)
                    .removed(null)
                    .by(testAppSolicitor.getFullName())
                    .via(NOTICE_OF_CHANGE)
                    .build()))).build();
    }

    private RepresentationUpdateHistory getChangeOfRepsRespondent() {
        ChangedRepresentative added = ChangedRepresentative.builder()
            .name(testRespSolicitor.getFullName())
            .email(testRespSolicitor.getEmail())
            .organisation(respondentOrg)
            .build();
        LocalDateTime date = LocalDateTime.of(LocalDate.of(2020, 6, 1),
            LocalTime.of(15, 0));

        return RepresentationUpdateHistory.builder().representationUpdateHistory(
            List.of(element(UUID.randomUUID(),
                RepresentationUpdate.builder()
                    .party("respondent")
                    .clientName("Jane Smith")
                    .date(date)
                    .added(added)
                    .removed(null)
                    .by(testRespSolicitor.getFullName())
                    .via(NOTICE_OF_CHANGE)
                    .build()))).build();
    }

    private RepresentationUpdateHistory getChangeOfRepsReplacingApplicant(UserDetails testAppSolicitorReplacing,
                                                                          Organisation appOrg) {
        ChangedRepresentative added = ChangedRepresentative.builder()
            .name(testAppSolicitorReplacing.getFullName())
            .email(testAppSolicitorReplacing.getEmail())
            .organisation(appOrg)
            .build();

        ChangedRepresentative removed = ChangedRepresentative.builder()
            .name(testAppSolicitor.getFullName())
            .email(testAppSolicitor.getEmail())
            .organisation(applicantOrg)
            .build();

        LocalDateTime date = LocalDateTime.of(LocalDate.of(2020, 6, 1),
            LocalTime.of(15, 0));

        return RepresentationUpdateHistory.builder().representationUpdateHistory(
            List.of(element(UUID.randomUUID(),
                RepresentationUpdate.builder()
                    .party("applicant")
                    .clientName("John Smith")
                    .date(date)
                    .added(added)
                    .removed(removed)
                    .by(testAppSolicitorReplacing.getFullName())
                    .via(NOTICE_OF_CHANGE)
                    .build()))).build();
    }

    @Test
    void validateEmailActiveForOrganisation_whenUserLinked_thenReturnNoErrors() {
        String email = "valid@test.com";
        String caseRef = "12345678";

        when(organisationService.findUserByEmail(email)).thenReturn(Optional.of("user-id-1"));

        List<String> errors = updateRepresentationService
            .validateEmailActiveForOrganisation(email, caseRef);

        assertEquals(List.of(), errors);
        verify(organisationService).findUserByEmail(email);
    }

    @Test
    void validateEmailActiveForOrganisation_whenUserNotLinked_thenReturnNotActiveUserError() {
        String email = "missing@test.com";
        String caseRef = "12345678";

        when(organisationService.findUserByEmail(email)).thenReturn(Optional.empty());

        List<String> errors = updateRepresentationService
            .validateEmailActiveForOrganisation(email, caseRef);

        assertEquals(List.of("Email is not linked to an active User within a HMCTS organisation"), errors);
        verify(organisationService).findUserByEmail(email);
    }

    @Test
    void validateEmailActiveForOrganisation_whenFindUserThrows_thenReturnGenericErrorWithCaseReference() {
        String email = "error@test.com";
        String caseRef = "12345678";

        when(organisationService.findUserByEmail(email))
            .thenThrow(new RuntimeException("boom"));

        List<String> errors = updateRepresentationService
            .validateEmailActiveForOrganisation(email, caseRef);

        assertEquals(
            List.of("Email could not be linked to your organisation. Please check and try again Case reference: " + caseRef),
            errors
        );
        verify(organisationService).findUserByEmail(email);
    }
}
