package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationContactInformation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdDataStoreService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeeService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.miam.MiamLegacyExemptionsService;

import java.io.File;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.fee;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType.CONSENTED;

@WebMvcTest(value = {PBAPaymentController.class, FeeLookupController.class})
public class PBAPaymentControllerTest extends BaseControllerTest {

    private static final String ASSIGN_APPLICANT_SOLICITOR_URL = "/case-orchestration/assign-applicant-solicitor";

    @MockitoBean
    private FeeService feeService;
    @MockitoBean
    private CaseDataService caseDataService;
    @MockitoBean
    private AssignCaseAccessService assignCaseAccessService;
    @MockitoBean
    private CcdDataStoreService ccdDataStoreService;
    @MockitoBean
    private PrdOrganisationService prdOrganisationService;
    @MockitoBean
    private MiamLegacyExemptionsService miamLegacyExemptionsService;

    @Autowired
    private ObjectMapper objectMapper;

    public static final String ADDRESS_LINE_1 = "addressLine1";
    public static final String ADDRESS_LINE_2 = "addressLine2";
    public static final String ADDRESS_LINE_3 = "addressLine3";
    public static final String COUNTY = "county";
    public static final String COUNTRY = "country";
    public static final String TOWN_CITY = "townCity";
    public static final String POSTCODE = "postCode";

    OrganisationContactInformation organisationContactInformation = OrganisationContactInformation.builder()
        .addressLine1(ADDRESS_LINE_1)
        .addressLine2(ADDRESS_LINE_2)
        .addressLine3(ADDRESS_LINE_3)
        .county(COUNTY)
        .country(COUNTRY)
        .townCity(TOWN_CITY)
        .postcode(POSTCODE)
        .build();

    @Before
    @Override
    public void setUp() {
        super.setUp();

        when(prdOrganisationService.retrieveOrganisationsData(AUTH_TOKEN)).thenReturn(OrganisationsResponse.builder()
            .contactInformation(singletonList(organisationContactInformation))
            .name(TEST_SOLICITOR_NAME)
            .organisationIdentifier(TEST_SOLICITOR_REFERENCE)
            .build());
    }

    private void doPBASetUp() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass().getResource("/fixtures/pba-payment.json").toURI()));

        when(feeService.getApplicationFee(CONSENTED, null)).thenReturn(fee(CONSENTED));
    }

    private void doPBAPaymentReferenceAlreadyExistsSetup() throws Exception {
        String pbaPaymentAlreadyExists = "/fixtures/pba-payment-already-exists.json";
        requestContent = objectMapper.readTree(new File(getClass().getResource(pbaPaymentAlreadyExists).toURI()));

        when(feeService.getApplicationFee(CONSENTED, null)).thenReturn(fee(CONSENTED));
    }

    @Test
    public void shouldAssignApplicantSolicitor() throws Exception {
        doPBAPaymentReferenceAlreadyExistsSetup();
        when(assignCaseAccessService.isCreatorRoleActiveOnCase(any())).thenReturn(true);

        mvc.perform(post(ASSIGN_APPLICANT_SOLICITOR_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.authorisation3", is(notNullValue())))
            .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
        verify(ccdDataStoreService, times(1)).removeCreatorRole(any(CaseDetails.class), eq(AUTH_TOKEN));
        verify(assignCaseAccessService, times(1)).assignCaseAccess(any(CaseDetails.class), eq(AUTH_TOKEN));
    }

    @Test
    public void shouldNotAssignApplicantSolicitor_assignCaseAccessDraftCaseActive() throws Exception {
        doPBAPaymentReferenceAlreadyExistsSetup();
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(true);
        when(assignCaseAccessService.isCreatorRoleActiveOnCase(any())).thenReturn(false);

        mvc.perform(post(ASSIGN_APPLICANT_SOLICITOR_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldNotAssignApplicantSolicitor_organisationIdNoMatch() throws Exception {
        doPBAPaymentReferenceAlreadyExistsSetup();
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(true);
        when(assignCaseAccessService.isCreatorRoleActiveOnCase(any())).thenReturn(true);
        when(prdOrganisationService.retrieveOrganisationsData(AUTH_TOKEN)).thenReturn(OrganisationsResponse.builder()
            .contactInformation(singletonList(organisationContactInformation))
            .name(TEST_SOLICITOR_NAME)
            .organisationIdentifier("INCORRECT_IDENTIFIER")
            .build());

        mvc.perform(post(ASSIGN_APPLICANT_SOLICITOR_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", hasSize(2)))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldNotAssignApplicantSolicitor_organisationEmpty() throws Exception {
        doPBASetUp();
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/pba-payment-no-app-org.json").toURI()));
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(true);
        when(assignCaseAccessService.isCreatorRoleActiveOnCase(any())).thenReturn(true);

        mvc.perform(post(ASSIGN_APPLICANT_SOLICITOR_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", hasSize(2)))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
        verifyNoInteractions(ccdDataStoreService);
    }

    @Test
    public void shouldNotAssignApplicantSolicitor_acaApiFailure() throws Exception {
        doPBASetUp();
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(true);
        when(assignCaseAccessService.isCreatorRoleActiveOnCase(any())).thenReturn(true);
        doThrow(feignError()).when(assignCaseAccessService).assignCaseAccess(any(CaseDetails.class), eq(AUTH_TOKEN));

        mvc.perform(post(ASSIGN_APPLICANT_SOLICITOR_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void givenDraftCaseWithInvalidMiamExemptions_whenAssignAppSolUrlRequest_thenReturnsErrors() throws Exception {
        doPBASetUp();
        List<String> errors = List.of("Error 1", "Error 2");
        when(miamLegacyExemptionsService.isLegacyExemptionsInvalid(any(Map.class))).thenReturn(true);
        when(miamLegacyExemptionsService.getInvalidLegacyExemptions(any(Map.class))).thenReturn(errors);

        mvc.perform(post(ASSIGN_APPLICANT_SOLICITOR_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", hasSize(3)))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }
}
