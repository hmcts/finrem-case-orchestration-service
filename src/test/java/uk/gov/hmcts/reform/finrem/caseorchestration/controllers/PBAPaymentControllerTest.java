package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationContactInformation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdDataStoreService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeeService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PBAPaymentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;

import java.io.File;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.fee;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;
import static uk.gov.hmcts.reform.finrem.caseorchestration.error.GlobalExceptionHandler.SERVER_ERROR_MSG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus.AWAITING_HWF_DECISION;

@WebMvcTest(value = {PBAPaymentController.class, FeeLookupController.class})
public class PBAPaymentControllerTest extends BaseControllerTest {

    private static final String PBA_PAYMENT_URL = "/case-orchestration/pba-payment";
    private static final String ASSIGN_APPLICANT_SOLICITOR_URL = "/case-orchestration/assign-applicant-solicitor";

    @MockBean private FeeService feeService;
    @MockBean private PBAPaymentService pbaPaymentService;
    @MockBean private CaseDataService caseDataService;
    @MockBean private AssignCaseAccessService assignCaseAccessService;
    @MockBean private CcdDataStoreService ccdDataStoreService;
    @MockBean private FeatureToggleService featureToggleService;
    @MockBean private PrdOrganisationService prdOrganisationService;

    @Autowired private ObjectMapper objectMapper;

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
    public void setUp() {
        super.setUp();
        when(featureToggleService.isAssignCaseAccessEnabled()).thenReturn(true);
        when(featureToggleService.isUseUserTokenEnabled()).thenReturn(true);

        when(prdOrganisationService.retrieveOrganisationsData(AUTH_TOKEN)).thenReturn(OrganisationsResponse.builder()
            .contactInformation(singletonList(organisationContactInformation))
            .name(TEST_SOLICITOR_NAME)
            .organisationIdentifier(TEST_SOLICITOR_REFERENCE)
            .build());
    }

    private static PaymentResponse paymentResponse(boolean success) {
        return PaymentResponse.builder()
            .reference("RC1")
            .status(success ? "success" : "failed")
            .message(success ? null : "Access denied")
            .build();
    }

    @Test
    public void shouldReturnBadRequestWhenCaseDataIsMissingInRequest() throws Exception {
        doEmptyCaseDataSetUp();
        mvc.perform(post(PBA_PAYMENT_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(startsWith(SERVER_ERROR_MSG)));
        verifyNoInteractions(ccdDataStoreService);
        verifyNoInteractions(assignCaseAccessService);
    }

    private void doPBASetUp(boolean success) throws Exception {
        requestContent = objectMapper.readTree(new File(getClass().getResource("/fixtures/pba-payment.json").toURI()));

        when(feeService.getApplicationFee(CONSENTED)).thenReturn(fee(CONSENTED));
        when(pbaPaymentService.makePayment(anyString(), any())).thenReturn(paymentResponse(success));
    }

    private void doPBAPaymentReferenceAlreadyExistsSetup() throws Exception {
        String pbaPaymentAlreadyExists = "/fixtures/pba-payment-already-exists.json";
        requestContent = objectMapper.readTree(new File(getClass().getResource(pbaPaymentAlreadyExists).toURI()));

        when(feeService.getApplicationFee(CONSENTED)).thenReturn(fee(CONSENTED));
        when(pbaPaymentService.makePayment(anyString(), any())).thenReturn(paymentResponse(true));
    }

    private void doHWFSetUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/hwf.json").toURI()));
        when(feeService.getApplicationFee(CONSENTED)).thenReturn(fee(CONSENTED));
    }

    @Test
    public void shouldNotDoPBAPaymentWhenPaymentIsDoneWithHWF() throws Exception {
        doHWFSetUp();
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);

        mvc.perform(post(PBA_PAYMENT_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.state", is(AWAITING_HWF_DECISION.toString())))
            .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeCode", is("FEE0640")))
            .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeAmount", is("1000")))
            .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeDescription", is("finrem")))
            .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeVersion", is("v1")))
            .andExpect(jsonPath("$.data.amountToPay", is("1000")))
            .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
        verify(pbaPaymentService, never()).makePayment(anyString(), any());
    }

    @Test
    public void shouldReturnErrorWhenPbaPaymentFails() throws Exception {
        doPBASetUp(false);
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);

        mvc.perform(post(PBA_PAYMENT_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
        verify(pbaPaymentService, times(1)).makePayment(anyString(), any());
        verifyNoInteractions(ccdDataStoreService);
        verifyNoInteractions(assignCaseAccessService);
    }

    @Test
    public void shouldDoPbaPayment() throws Exception {
        doPBASetUp(true);
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);

        mvc.perform(post(PBA_PAYMENT_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeCode", is("FEE0640")))
            .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeAmount", is("1000")))
            .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeDescription", is("finrem")))
            .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeVersion", is("v1")))
            .andExpect(jsonPath("$.data.amountToPay", is("1000")))
            .andExpect(jsonPath("$.data.PBAPaymentReference", is("RC1")))
            .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
        verify(pbaPaymentService, times(1)).makePayment(anyString(), any());
    }

    @Test
    public void shouldNotDoPbaPaymentWhenPBAPaymentAlreadyExists() throws Exception {
        doPBAPaymentReferenceAlreadyExistsSetup();
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);

        mvc.perform(post(PBA_PAYMENT_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
        verify(pbaPaymentService, never()).makePayment(anyString(), any());
    }

    @Test
    public void shouldAssignApplicantSolicitor() throws Exception {
        doPBAPaymentReferenceAlreadyExistsSetup();
        when(featureToggleService.isAssignCaseAccessEnabled()).thenReturn(true);

        mvc.perform(post(ASSIGN_APPLICANT_SOLICITOR_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.authorisation3", is(notNullValue())))
            .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
        verify(ccdDataStoreService, times(1)).removeCreatorRole(any(), eq(AUTH_TOKEN));
        verify(assignCaseAccessService, times(1)).assignCaseAccess(any(), eq(AUTH_TOKEN));
    }

    @Test
    @Ignore("Ignore for NoC test purposes as assignAccessFeatureToggle needs to be enabled for preview testing")
    public void shouldNotAssignApplicantSolicitor_assignCaseAccessToggledOff() throws Exception {
        doPBAPaymentReferenceAlreadyExistsSetup();
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(featureToggleService.isAssignCaseAccessEnabled()).thenReturn(false);

        mvc.perform(post(ASSIGN_APPLICANT_SOLICITOR_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
        verifyNoInteractions(ccdDataStoreService);
        verifyNoInteractions(assignCaseAccessService);
    }

    @Test
    public void shouldNotAssignApplicantSolicitor_organisationIdNoMatch() throws Exception {
        doPBAPaymentReferenceAlreadyExistsSetup();
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
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
        verifyNoInteractions(ccdDataStoreService);
        verifyNoInteractions(assignCaseAccessService);
    }

    @Test
    public void shouldNotAssignApplicantSolicitor_organisationEmpty() throws Exception {
        doPBASetUp(true);
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/pba-payment-no-app-org.json").toURI()));
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);

        mvc.perform(post(ASSIGN_APPLICANT_SOLICITOR_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", hasSize(2)))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
        verifyNoInteractions(ccdDataStoreService);
        verifyNoInteractions(assignCaseAccessService);
    }

    @Test
    public void shouldNotAssignApplicantSolicitor_acaApiFailure() throws Exception {
        doPBASetUp(true);
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        doThrow(feignError()).when(assignCaseAccessService).assignCaseAccess(any(), eq(AUTH_TOKEN));

        mvc.perform(post(ASSIGN_APPLICANT_SOLICITOR_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
        verify(assignCaseAccessService, times(1)).assignCaseAccess(any(), eq(AUTH_TOKEN));

        verifyNoInteractions(ccdDataStoreService);
    }
}
