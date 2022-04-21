package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentatives;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.events.AuditEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationContactInformation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_DX_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_DX_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_DX_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_PHONE;

public class UpdateRepresentationServiceTest extends BaseServiceTest {

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
    private static final String CHANGE_OF_REPS = "ChangeOfRepresentatives";

    @Autowired private UpdateRepresentationService updateRepresentationService;

    @MockBean
    private AuditEventService auditEventService;

    @MockBean
    private IdamAuthService idamClient;

    @MockBean
    private CaseDataService caseDataService;

    @MockBean
    private PrdOrganisationService organisationService;

    @MockBean private UpdateSolicitorDetailsService updateSolicitorDetailsService;

    @MockBean private ChangeOfRepresentationService changeOfRepresentationService;

    private UserDetails testAppSolicitor;
    private UserDetails testRespSolicitor;
    private Organisation applicantOrg;
    private Organisation respondentOrg;
    private AuditEvent testAuditEvent;
    private OrganisationsResponse orgResponse;

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

    @Before
    public void setUp() {
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
    public void shouldAddChangeOfRepsAndUpdateAppSolDetailsContested() throws Exception {
        when(auditEventService.getLatestAuditEventByName(any(), eq(NOC_EVENT))).thenReturn(Optional.of(testAuditEvent));
        when(idamClient.getUserByUserId(any(), eq(testAuditEvent.getUserId()))).thenReturn(testAppSolicitor);
        when(organisationService.findOrganisationByOrgId(any())).thenReturn(orgResponse);
        when(updateSolicitorDetailsService.convertOrganisationAddressToSolicitorAddress(orgResponse))
            .thenReturn(prepareSolAddressData(orgResponse));
        when(changeOfRepresentationService.generateChangeOfRepresentatives(any()))
            .thenReturn(getChangeOfRepsAppContested());
        when(updateSolicitorDetailsService.updateSolicitorContactDetails(any(), any(), anyBoolean(), anyBoolean()))
            .thenReturn(getUpdatedContactData("contestedAppSolicitorAdding"));


        setUpCaseDetails("contestedAppSolicitorAdding/after-update-details.json");
        try (InputStream resourceAsStream = getClass()
                     .getResourceAsStream(PATH + "contestedAppSolicitorAdding/change-of-representatives-before.json")) {
            initialDetails = mapper.readValue(resourceAsStream, CallbackRequest.class)
                .getCaseDetails();
        }

        Map<String, Object> actualData = updateRepresentationService
            .updateRepresentationAsSolicitor(initialDetails, "bebe");

        assertEquals(actualData.get(CONTESTED_SOLICITOR_NAME), expectedCaseData.get(CONTESTED_SOLICITOR_NAME));
        assertEquals(actualData.get(CONTESTED_SOLICITOR_EMAIL), expectedCaseData.get(CONTESTED_SOLICITOR_EMAIL));
        assertNull(actualData.get(CONTESTED_SOLICITOR_DX_NUMBER));
        assertNull(actualData.get(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED));
        assertNull(actualData.get(SOLICITOR_PHONE));

        ChangeOfRepresentation actualChangeOfRep = mapper.convertValue(actualData.get(CHANGE_OF_REPS),
            ChangeOfRepresentation[].class)[0];
        ChangeOfRepresentation expectedChangeOfRep = mapper.convertValue(expectedCaseData.get(CHANGE_OF_REPS),
            ChangeOfRepresentation[].class)[0];


        assertEquals(actualChangeOfRep.getParty(), expectedChangeOfRep.getParty());
        assertEquals(actualChangeOfRep.getClientName(), expectedChangeOfRep.getClientName());
        assertEquals(actualChangeOfRep.getBy(), expectedChangeOfRep.getBy());
        assertEquals(actualChangeOfRep.getAdded(), expectedChangeOfRep.getAdded());
        assertEquals(actualChangeOfRep.getRemoved(), expectedChangeOfRep.getRemoved());

        Address solicitorAddress = mapper.convertValue(actualData.get(CONTESTED_SOLICITOR_ADDRESS), Address.class);
        assertEquals(solicitorAddress.getAddressLine1(), ADDRESS_LINE_1);
        assertEquals(solicitorAddress.getPostTown(), TOWN_CITY);
        assertEquals(solicitorAddress.getCounty(), COUNTY);
        assertEquals(solicitorAddress.getCountry(), COUNTRY);
        assertEquals(solicitorAddress.getPostCode(), POSTCODE);
    }

    @Test
    public void shouldAddChangeOfRepsAndUpdateAppSolDetailsConsented() throws Exception {
        when(auditEventService.getLatestAuditEventByName(any(), eq(NOC_EVENT))).thenReturn(Optional.of(testAuditEvent));
        when(idamClient.getUserByUserId(any(), eq(testAuditEvent.getUserId()))).thenReturn(testAppSolicitor);
        when(organisationService.findOrganisationByOrgId(any())).thenReturn(orgResponse);
        when(updateSolicitorDetailsService.convertOrganisationAddressToSolicitorAddress(orgResponse))
            .thenReturn(prepareSolAddressData(orgResponse));
        when(changeOfRepresentationService.generateChangeOfRepresentatives(any()))
            .thenReturn(getChangeOfRepsAppContested());
        when(updateSolicitorDetailsService.updateSolicitorContactDetails(any(), any(), anyBoolean(), anyBoolean()))
            .thenReturn(getUpdatedContactData("consentedAppSolicitorAdding"));
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);

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

        ChangeOfRepresentation actualChangeOfRep = mapper.convertValue(actualData.get(CHANGE_OF_REPS),
            ChangeOfRepresentation[].class)[0];
        ChangeOfRepresentation expectedChangeOfRep = mapper.convertValue(expectedCaseData.get(CHANGE_OF_REPS),
            ChangeOfRepresentation[].class)[0];


        assertEquals(actualChangeOfRep.getParty(), expectedChangeOfRep.getParty());
        assertEquals(actualChangeOfRep.getClientName(), expectedChangeOfRep.getClientName());
        assertEquals(actualChangeOfRep.getBy(), expectedChangeOfRep.getBy());
        assertEquals(actualChangeOfRep.getAdded(), expectedChangeOfRep.getAdded());
        assertEquals(actualChangeOfRep.getRemoved(), expectedChangeOfRep.getRemoved());

        Address solicitorAddress = mapper.convertValue(actualData.get(CONSENTED_SOLICITOR_ADDRESS), Address.class);
        assertEquals(solicitorAddress.getAddressLine1(), ADDRESS_LINE_1);
        assertEquals(solicitorAddress.getPostTown(), TOWN_CITY);
        assertEquals(solicitorAddress.getCounty(), COUNTY);
        assertEquals(solicitorAddress.getCountry(), COUNTRY);
        assertEquals(solicitorAddress.getPostCode(), POSTCODE);
    }

    @Test
    public void shouldUpdateRespSolDetails() throws Exception {
        when(auditEventService.getLatestAuditEventByName(any(), eq(NOC_EVENT))).thenReturn(Optional.of(testAuditEvent));
        when(idamClient.getUserByUserId(any(), eq(testAuditEvent.getUserId()))).thenReturn(testAppSolicitor);
        when(organisationService.findOrganisationByOrgId(any())).thenReturn(orgResponse);
        when(updateSolicitorDetailsService.convertOrganisationAddressToSolicitorAddress(orgResponse))
            .thenReturn(prepareSolAddressData(orgResponse));
        when(changeOfRepresentationService.generateChangeOfRepresentatives(any()))
            .thenReturn(getChangeOfRepsRespondent());
        when(updateSolicitorDetailsService.updateSolicitorContactDetails(any(), any(), anyBoolean(), anyBoolean()))
            .thenReturn(getUpdatedContactData("RespSolicitorAdding"));
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);

        setUpCaseDetails("RespSolicitorAdding/after-update-details.json");
        try (InputStream resourceAsStream = getClass()
            .getResourceAsStream(PATH + "RespSolicitorAdding/change-of-representatives-before.json")) {
            initialDetails = mapper.readValue(resourceAsStream, CallbackRequest.class)
                .getCaseDetails();
        }

        Map<String, Object> actualData = updateRepresentationService
            .updateRepresentationAsSolicitor(initialDetails, "bebe");

        assertEquals(actualData.get(RESP_SOLICITOR_NAME), expectedCaseData.get(RESP_SOLICITOR_NAME));
        assertEquals(actualData.get(RESP_SOLICITOR_EMAIL), expectedCaseData.get(RESP_SOLICITOR_EMAIL));
        assertNull(actualData.get(RESP_SOLICITOR_DX_NUMBER));
        assertNull(actualData.get(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT));
        assertNull(actualData.get(RESP_SOLICITOR_PHONE));

        ChangeOfRepresentation actualChangeOfRep = mapper.convertValue(actualData.get(CHANGE_OF_REPS),
            ChangeOfRepresentation[].class)[0];
        ChangeOfRepresentation expectedChangeOfRep = mapper.convertValue(expectedCaseData.get(CHANGE_OF_REPS),
            ChangeOfRepresentation[].class)[0];


        assertEquals(actualChangeOfRep.getParty(), expectedChangeOfRep.getParty());
        assertEquals(actualChangeOfRep.getClientName(), expectedChangeOfRep.getClientName());
        assertEquals(actualChangeOfRep.getBy(), expectedChangeOfRep.getBy());
        assertEquals(actualChangeOfRep.getAdded(), expectedChangeOfRep.getAdded());
        assertEquals(actualChangeOfRep.getRemoved(), expectedChangeOfRep.getRemoved());

        Address solicitorAddress = mapper.convertValue(actualData.get(RESP_SOLICITOR_ADDRESS), Address.class);
        assertEquals(solicitorAddress.getAddressLine1(), ADDRESS_LINE_1);
        assertEquals(solicitorAddress.getPostTown(), TOWN_CITY);
        assertEquals(solicitorAddress.getCounty(), COUNTY);
        assertEquals(solicitorAddress.getCountry(), COUNTRY);
        assertEquals(solicitorAddress.getPostCode(), POSTCODE);

    }

    @Test
    public void shouldUpdateContactDetailsAppSolReplacing() throws Exception {
        UserDetails replacingSolicitor = UserDetails.builder()
            .forename("Test Applicant")
            .surname("Solicitor")
            .email("appsolicitor1@yahoo.com")
            .build();

        Organisation secondAppOrg = Organisation.builder()
            .organisationName("FRApplicantSolicitorFirm2")
            .organisationID("A31PTVU")
            .build();

        OrganisationsResponse secondOrgResponse = OrganisationsResponse.builder()
            .contactInformation(List.of(organisationContactInformation))
            .name("FRApplicantSolicitorFirm2")
            .organisationIdentifier("FRApplicantSolicitorFirm2")
            .build();

        when(auditEventService.getLatestAuditEventByName(any(), eq(NOC_EVENT))).thenReturn(Optional.of(testAuditEvent));
        when(idamClient.getUserByUserId(any(), eq(testAuditEvent.getUserId()))).thenReturn(replacingSolicitor);
        when(organisationService.findOrganisationByOrgId(any())).thenReturn(secondOrgResponse);
        when(updateSolicitorDetailsService.convertOrganisationAddressToSolicitorAddress(secondOrgResponse))
            .thenReturn(prepareSolAddressData(secondOrgResponse));
        when(changeOfRepresentationService.generateChangeOfRepresentatives(any()))
            .thenReturn(getChangeOfRepsReplacingApplicant(replacingSolicitor, secondAppOrg));
        when(updateSolicitorDetailsService.updateSolicitorContactDetails(any(), any(), anyBoolean(), anyBoolean()))
            .thenReturn(getUpdatedContactData("AppSolReplacing"));
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);

        setUpCaseDetails("AppSolReplacing/after-update-details.json");
        try (InputStream resourceAsStream = getClass()
            .getResourceAsStream(PATH + "AppSolReplacing/change-of-representatives-before.json")) {
            initialDetails = mapper.readValue(resourceAsStream, CallbackRequest.class)
                .getCaseDetails();
        }

        Map<String, Object> actualData = updateRepresentationService
            .updateRepresentationAsSolicitor(initialDetails, "bebe");

        assertEquals(actualData.get(CONTESTED_SOLICITOR_NAME), "Test Applicant Solicitor");
        assertEquals(actualData.get(CONTESTED_SOLICITOR_EMAIL), "appsolicitor1@yahoo.com");
        assertNull(actualData.get(CONTESTED_SOLICITOR_DX_NUMBER));
        assertNull(actualData.get(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED));
        assertNull(actualData.get(SOLICITOR_PHONE));

        ChangeOfRepresentation actualChangeOfRep = mapper.convertValue(actualData.get(CHANGE_OF_REPS),
            ChangeOfRepresentation[].class)[0];
        ChangeOfRepresentation expectedChangeOfRep = mapper.convertValue(expectedCaseData.get(CHANGE_OF_REPS),
            ChangeOfRepresentation[].class)[0];

        assertEquals(actualChangeOfRep.getParty(), expectedChangeOfRep.getParty());
        assertEquals(actualChangeOfRep.getClientName(), expectedChangeOfRep.getClientName());
        assertEquals(actualChangeOfRep.getBy(), expectedChangeOfRep.getBy());
        assertEquals(actualChangeOfRep.getAdded(), expectedChangeOfRep.getAdded());
        assertEquals(actualChangeOfRep.getRemoved(), expectedChangeOfRep.getRemoved());

        Address solicitorAddress = mapper.convertValue(actualData.get(CONTESTED_SOLICITOR_ADDRESS), Address.class);
        assertEquals(solicitorAddress.getAddressLine1(), ADDRESS_LINE_1);
        assertEquals(solicitorAddress.getPostTown(), TOWN_CITY);
        assertEquals(solicitorAddress.getCounty(), COUNTY);
        assertEquals(solicitorAddress.getCountry(), COUNTRY);
        assertEquals(solicitorAddress.getPostCode(), POSTCODE);
    }

    private Map<String, Object> prepareSolAddressData(OrganisationsResponse organisationData) {
        System.out.println(organisationData);
        return mapper.convertValue(Address.builder()
            .addressLine1(organisationData.getContactInformation().get(0).getAddressLine1())
            .addressLine2(organisationData.getContactInformation().get(0).getAddressLine2())
            .addressLine3(organisationData.getContactInformation().get(0).getAddressLine3())
            .county(organisationData.getContactInformation().get(0).getCounty())
            .country(organisationData.getContactInformation().get(0).getCountry())
            .postTown(organisationData.getContactInformation().get(0).getTownCity())
            .postCode(organisationData.getContactInformation().get(0).getPostcode())
            .build(), Map.class);
    }

    private Map<String, Object> getUpdatedContactData(String subPath) throws Exception {
        InputStream resourceAsStream = getClass()
            .getResourceAsStream(PATH + subPath + "/after-update-details.json");
        return mapper.readValue(resourceAsStream, CallbackRequest.class)
            .getCaseDetails().getData();
    }

    private ChangeOfRepresentatives getChangeOfRepsAppContested() {
        ChangedRepresentative added = ChangedRepresentative.builder()
            .name(testAppSolicitor.getFullName())
            .email(testAppSolicitor.getEmail())
            .organisation(applicantOrg)
            .build();
        return ChangeOfRepresentatives.builder().changeOfRepresentation(
            List.of(
                ChangeOfRepresentation.builder()
                    .party("applicant")
                    .clientName("John Smith")
                    .date(LocalDate.of(2020, 6, 1))
                    .added(added)
                    .removed(null)
                    .by(testAppSolicitor.getFullName())
                    .via(NOTICE_OF_CHANGE)
                    .build()
            )
        ).build();
    }

    private ChangeOfRepresentatives getChangeOfRepsRespondent() {
        ChangedRepresentative added = ChangedRepresentative.builder()
            .name(testRespSolicitor.getFullName())
            .email(testRespSolicitor.getEmail())
            .organisation(respondentOrg)
            .build();

        return ChangeOfRepresentatives.builder().changeOfRepresentation(
            List.of(
                ChangeOfRepresentation.builder()
                    .party("respondent")
                    .clientName("Jane Smith")
                    .date(LocalDate.of(2020, 6, 1))
                    .added(added)
                    .removed(null)
                    .by(testRespSolicitor.getFullName())
                    .via(NOTICE_OF_CHANGE)
                    .build()
            )
        ).build();
    }

    private ChangeOfRepresentatives getChangeOfRepsReplacingApplicant(UserDetails testAppSolicitorReplacing, Organisation appOrg) {
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

        return ChangeOfRepresentatives.builder().changeOfRepresentation(
            List.of(
                ChangeOfRepresentation.builder()
                    .party("applicant")
                    .clientName("John Smith")
                    .date(LocalDate.of(2020, 6, 1))
                    .added(added)
                    .removed(removed)
                    .by(testAppSolicitorReplacing.getFullName())
                    .via(NOTICE_OF_CHANGE)
                    .build()
            )
        ).build();
    }


}
