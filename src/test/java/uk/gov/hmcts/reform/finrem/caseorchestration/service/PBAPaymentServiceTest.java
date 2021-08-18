package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentResponse;

import java.io.File;
import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_FIRM;

public class PBAPaymentServiceTest extends BaseServiceTest {

    @Autowired
    private PBAPaymentService pbaPaymentService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private CaseDataService caseDataService;

    @Value("${payment.api.siteId}")
    private String siteId;

    @Value("${payment.api.consented-description}")
    private String consentedDescription;

    @Value("${payment.api.contested-description}")
    private String contestedDescription;

    private CallbackRequest callbackRequest;

    @ClassRule
    public static WireMockClassRule paymentService = new WireMockClassRule(9001);

    private static final String PBA_PAYMENT_API = "/payments/pba-payment";
    private static final String PBA_NUMBER = "PBA123";
    private static final String PBA_REFERENCE = "ABCD";
    private static final BigDecimal TEN = new BigDecimal("10.0");
    private static final String CASE_REFERENCE = "DD12D12345";
    private static final String CASE_ID = "123";
    private static final String CURRENCY = "GBP";
    private static final String ORG_NAME = "Mr";
    private static final String SERVICE = "FINREM";
    private static final String FEE_CODE = "code1";
    private static final String FEE_VERSION = "v1";

    @Before
    public void setupCaseData() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        callbackRequest = mapper.readValue(new File(getClass()
            .getResource("/fixtures/pba-payment.json").toURI()), CallbackRequest.class);
    }

    public void setupContestedCaseData() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        callbackRequest = mapper.readValue(new File(getClass()
            .getResource("/fixtures/pba-payment-contested.json").toURI()), CallbackRequest.class);
    }

    @Test
    public void buildPaymentRequestBySiteId() throws Exception {
        when(featureToggleService.isPBAUsingCaseTypeEnabled()).thenReturn(false);
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);

        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        PaymentRequest paymentRequest = pbaPaymentService.buildPaymentRequest(caseDetails);

        assertThat(paymentRequest, is(notNullValue()));
        assertThat(paymentRequest.getAccountNumber(), is(PBA_NUMBER));
        assertThat(paymentRequest.getAmount(), is(TEN));
        assertThat(paymentRequest.getSiteId(), is(siteId));
        assertThat(paymentRequest.getCaseType(), is(nullValue()));
        assertThat(paymentRequest.getCaseReference(), is(CASE_REFERENCE));
        assertThat(paymentRequest.getCcdCaseNumber(), is(CASE_ID));
        assertThat(paymentRequest.getService(), is(SERVICE));
        assertThat(paymentRequest.getCustomerReference(), is(PBA_REFERENCE));
        assertThat(paymentRequest.getCurrency(), is(CURRENCY));
        assertThat(paymentRequest.getDescription(), is(contestedDescription));
        assertThat(paymentRequest.getOrganisationName(), is(ORG_NAME));
        assertThat(paymentRequest.getFeesList(), hasSize(1));
        assertThat(paymentRequest.getFeesList().get(0).getCalculatedAmount(), is(TEN));
        assertThat(paymentRequest.getFeesList().get(0).getCode(), is(FEE_CODE));
        assertThat(paymentRequest.getFeesList().get(0).getVersion(), is(FEE_VERSION));
    }

    @Test
    public void buildPaymentRequestByCaseTypeConsented() throws Exception {
        when(featureToggleService.isPBAUsingCaseTypeEnabled()).thenReturn(true);
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);

        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        PaymentRequest paymentRequest = pbaPaymentService.buildPaymentRequestWithCaseType(caseDetails);

        assertThat(paymentRequest, is(notNullValue()));
        assertThat(paymentRequest.getAccountNumber(), is(PBA_NUMBER));
        assertThat(paymentRequest.getAmount(), is(TEN));
        assertThat(paymentRequest.getSiteId(), is(nullValue()));
        assertThat(paymentRequest.getCaseType(), is(CASE_TYPE_ID_CONSENTED));
        assertThat(paymentRequest.getCaseReference(), is(CASE_REFERENCE));
        assertThat(paymentRequest.getCcdCaseNumber(), is(CASE_ID));
        assertThat(paymentRequest.getService(), is(SERVICE));
        assertThat(paymentRequest.getCustomerReference(), is(PBA_REFERENCE));
        assertThat(paymentRequest.getCurrency(), is(CURRENCY));
        assertThat(paymentRequest.getDescription(), is(consentedDescription));
        assertThat(paymentRequest.getOrganisationName(), is(CONSENTED_SOLICITOR_FIRM));
        assertThat(paymentRequest.getFeesList(), hasSize(1));
        assertThat(paymentRequest.getFeesList().get(0).getCalculatedAmount(), is(TEN));
        assertThat(paymentRequest.getFeesList().get(0).getCode(), is(FEE_CODE));
        assertThat(paymentRequest.getFeesList().get(0).getVersion(), is(FEE_VERSION));
    }

    @Test
    public void buildPaymentRequestByCaseTypeContested() throws Exception {
        setupContestedCaseData();
        when(featureToggleService.isPBAUsingCaseTypeEnabled()).thenReturn(true);
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);

        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        PaymentRequest paymentRequest = pbaPaymentService.buildPaymentRequestWithCaseType(caseDetails);

        assertThat(paymentRequest, is(notNullValue()));
        assertThat(paymentRequest.getAccountNumber(), is(PBA_NUMBER));
        assertThat(paymentRequest.getAmount(), is(TEN));
        assertThat(paymentRequest.getSiteId(), is(nullValue()));
        assertThat(paymentRequest.getCaseType(), is(CASE_TYPE_ID_CONTESTED));
        assertThat(paymentRequest.getCaseReference(), is(CASE_REFERENCE));
        assertThat(paymentRequest.getCcdCaseNumber(), is(CASE_ID));
        assertThat(paymentRequest.getService(), is(SERVICE));
        assertThat(paymentRequest.getCustomerReference(), is(PBA_REFERENCE));
        assertThat(paymentRequest.getCurrency(), is(CURRENCY));
        assertThat(paymentRequest.getDescription(), is(contestedDescription));
        assertThat(paymentRequest.getOrganisationName(), is(CONTESTED_SOLICITOR_FIRM));
        assertThat(paymentRequest.getFeesList(), hasSize(1));
        assertThat(paymentRequest.getFeesList().get(0).getCalculatedAmount(), is(TEN));
        assertThat(paymentRequest.getFeesList().get(0).getCode(), is(FEE_CODE));
        assertThat(paymentRequest.getFeesList().get(0).getVersion(), is(FEE_VERSION));
    }

    @Test
    public void paymentSuccessful() throws Exception {
        when(featureToggleService.isPBAUsingCaseTypeEnabled()).thenReturn(false);

        setUpPbaPaymentForSiteId("{"
            + " \"reference\": \"RC-1545-2396-5857-4110\","
            + " \"date_created\": \"2018-12-19T17:14:18.572+0000\","
            + " \"status\": \"Success\","
            + " \"status_histories\": ["
            + "   {"
            + "     \"status\": \"success\","
            + "     \"date_created\": \"2018-12-19T17:14:18.572+0000\","
            + "     \"date_updated\": \"2018-12-19T17:14:18.572+0000\""
            + "   }"
            + " ]"
            + "}");

        PaymentResponse paymentResponse = pbaPaymentService.makePayment(AUTH_TOKEN, callbackRequest.getCaseDetails());

        assertThat(paymentResponse.getReference(), is("RC-1545-2396-5857-4110"));
        assertThat(paymentResponse.getStatus(), is("Success"));
        assertThat(paymentResponse.isPaymentSuccess(), is(true));
        assertThat(paymentResponse.getPaymentError(), nullValue());
        assertThat(paymentResponse.getStatusHistories().size(), is(1));
    }

    @Test
    public void invalidFunds() throws Exception {
        when(featureToggleService.isPBAUsingCaseTypeEnabled()).thenReturn(false);

        setUpPbaPaymentForSiteId("{"
            + " \"reference\": \"RC-1545-2396-5857-4110\","
            + " \"date_created\": \"2018-12-19T17:14:18.572+0000\","
            + " \"status\": \"Failed\","
            + " \"status_histories\": ["
            + "   {"
            + "     \"status\": \"failed\","
            + "     \"error_code\": \"CA-E0001\","
            + "     \"error_message\": \"You have insufficient funds available\","
            + "     \"date_created\": \"2018-12-19T17:14:18.572+0000\","
            + "     \"date_updated\": \"2018-12-19T17:14:18.572+0000\""
            + "   }"
            + " ]"
            + "}");

        PaymentResponse paymentResponse = pbaPaymentService.makePayment(AUTH_TOKEN, callbackRequest.getCaseDetails());

        assertThat(paymentResponse.getReference(), is("RC-1545-2396-5857-4110"));
        assertThat(paymentResponse.getStatus(), is("Failed"));
        assertThat(paymentResponse.isPaymentSuccess(), is(false));
        assertThat(paymentResponse.getPaymentError(), is("You have insufficient funds available"));
        assertThat(paymentResponse.getStatusHistories().size(), is(1));
        assertThat(paymentResponse.getStatusHistories().get(0).getErrorCode(), is("CA-E0001"));
        assertThat(paymentResponse.getStatusHistories().get(0).getErrorMessage(),
            is("You have insufficient funds available"));
    }

    @Test
    public void accountOnHold() throws Exception {
        when(featureToggleService.isPBAUsingCaseTypeEnabled()).thenReturn(false);

        setUpPbaPaymentForSiteId("{"
            + " \"reference\": \"RC-1545-2396-5857-4110\","
            + " \"date_created\": \"2018-12-19T17:14:18.572+0000\","
            + " \"status\": \"Failed\","
            + " \"status_histories\": ["
            + "   {"
            + "     \"status\": \"failed\","
            + "     \"error_code\": \"CA-E0003\","
            + "     \"error_message\": \"Your account is on hold\","
            + "     \"date_created\": \"2018-12-19T17:14:18.572+0000\","
            + "     \"date_updated\": \"2018-12-19T17:14:18.572+0000\""
            + "   }"
            + " ]"
            + "}");

        PaymentResponse paymentResponse = pbaPaymentService.makePayment(AUTH_TOKEN, callbackRequest.getCaseDetails());

        assertThat(paymentResponse.getReference(), is("RC-1545-2396-5857-4110"));
        assertThat(paymentResponse.getStatus(), is("Failed"));
        assertThat(paymentResponse.isPaymentSuccess(), is(false));
        assertThat(paymentResponse.getPaymentError(), is("Your account is on hold"));
        assertThat(paymentResponse.getStatusHistories().size(), is(1));
        assertThat(paymentResponse.getStatusHistories().get(0).getErrorCode(), is("CA-E0003"));
        assertThat(paymentResponse.getStatusHistories().get(0).getErrorMessage(), is("Your account is on hold"));
    }

    @Test
    public void accountDeleted() throws Exception {
        when(featureToggleService.isPBAUsingCaseTypeEnabled()).thenReturn(false);

        setUpPbaPaymentForSiteId("{"
            + " \"reference\": \"RC-1545-2396-5857-4110\","
            + " \"date_created\": \"2018-12-19T17:14:18.572+0000\","
            + " \"status\": \"Failed\","
            + " \"status_histories\": ["
            + "   {"
            + "     \"status\": \"failed\","
            + "     \"error_code\": \"CA-E0004\","
            + "     \"error_message\": \"Your account is deleted\","
            + "     \"date_created\": \"2018-12-19T17:14:18.572+0000\","
            + "     \"date_updated\": \"2018-12-19T17:14:18.572+0000\""
            + "   }"
            + " ]"
            + "}");

        PaymentResponse paymentResponse = pbaPaymentService.makePayment(AUTH_TOKEN, callbackRequest.getCaseDetails());

        assertThat(paymentResponse.getReference(), is("RC-1545-2396-5857-4110"));
        assertThat(paymentResponse.getStatus(), is("Failed"));
        assertThat(paymentResponse.isPaymentSuccess(), is(false));
        assertThat(paymentResponse.getPaymentError(), is("Your account is deleted"));
        assertThat(paymentResponse.getStatusHistories().size(), is(1));
        assertThat(paymentResponse.getStatusHistories().get(0).getErrorCode(), is("CA-E0004"));
        assertThat(paymentResponse.getStatusHistories().get(0).getErrorMessage(), is("Your account is deleted"));
    }

    @Test
    public void accessIsDenied() throws Exception {
        when(featureToggleService.isPBAUsingCaseTypeEnabled()).thenReturn(false);

        setUpPbaPaymentForSiteId("{"
            + "  \"timestamp\": \"2019-01-09T17:59:20.473+0000\","
            + "  \"status\": 403,"
            + "  \"error\": \"Forbidden\","
            + "  \"message\": \"Access Denied\","
            + "  \"path\": \"/credit-account-payments\""
            + "}");

        PaymentResponse paymentResponse = pbaPaymentService.makePayment(AUTH_TOKEN, callbackRequest.getCaseDetails());

        assertThat(paymentResponse.getReference(), nullValue());
        assertThat(paymentResponse.getStatus(), is("403"));
        assertThat(paymentResponse.isPaymentSuccess(), is(false));
        assertThat(paymentResponse.getPaymentError(), is("Access Denied"));
        assertThat(paymentResponse.getStatusHistories(), nullValue());
    }

    @Test
    public void paymentSuccessfulWithCaseType_Consented() throws Exception {
        when(featureToggleService.isPBAUsingCaseTypeEnabled()).thenReturn(true);
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);

        setUpPbaPaymentForCaseType("{"
            + " \"reference\": \"RC-1545-2396-5857-4110\","
            + " \"date_created\": \"2018-12-19T17:14:18.572+0000\","
            + " \"status\": \"Success\","
            + " \"status_histories\": ["
            + "   {"
            + "     \"status\": \"success\","
            + "     \"date_created\": \"2018-12-19T17:14:18.572+0000\","
            + "     \"date_updated\": \"2018-12-19T17:14:18.572+0000\""
            + "   }"
            + " ]"
            + "}");

        PaymentResponse paymentResponse = pbaPaymentService.makePayment(AUTH_TOKEN, callbackRequest.getCaseDetails());

        assertThat(paymentResponse.getReference(), is("RC-1545-2396-5857-4110"));
        assertThat(paymentResponse.getStatus(), is("Success"));
        assertThat(paymentResponse.isPaymentSuccess(), is(true));
        assertThat(paymentResponse.getPaymentError(), nullValue());
        assertThat(paymentResponse.getStatusHistories().size(), is(1));
    }

    @Test
    public void paymentSuccessfulWithCaseType_Contested() throws Exception {
        when(featureToggleService.isPBAUsingCaseTypeEnabled()).thenReturn(true);
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);

        setUpPbaPaymentForCaseType("{"
            + " \"reference\": \"RC-1545-2396-5857-4110\","
            + " \"date_created\": \"2018-12-19T17:14:18.572+0000\","
            + " \"status\": \"Success\","
            + " \"status_histories\": ["
            + "   {"
            + "     \"status\": \"success\","
            + "     \"date_created\": \"2018-12-19T17:14:18.572+0000\","
            + "     \"date_updated\": \"2018-12-19T17:14:18.572+0000\""
            + "   }"
            + " ]"
            + "}");

        callbackRequest.getCaseDetails().setCaseTypeId("FinancialRemedyContested");
        PaymentResponse paymentResponse = pbaPaymentService.makePayment(AUTH_TOKEN, callbackRequest.getCaseDetails());

        assertThat(paymentResponse.getReference(), is("RC-1545-2396-5857-4110"));
        assertThat(paymentResponse.getStatus(), is("Success"));
        assertThat(paymentResponse.isPaymentSuccess(), is(true));
        assertThat(paymentResponse.getPaymentError(), nullValue());
        assertThat(paymentResponse.getStatusHistories().size(), is(1));
    }

    @Test
    public void invalidFundsWithCaseType() throws Exception {
        when(featureToggleService.isPBAUsingCaseTypeEnabled()).thenReturn(true);

        setUpPbaPaymentForCaseType("{"
            + " \"reference\": \"RC-1545-2396-5857-4110\","
            + " \"date_created\": \"2018-12-19T17:14:18.572+0000\","
            + " \"status\": \"Failed\","
            + " \"status_histories\": ["
            + "   {"
            + "     \"status\": \"failed\","
            + "     \"error_code\": \"CA-E0001\","
            + "     \"error_message\": \"You have insufficient funds available\","
            + "     \"date_created\": \"2018-12-19T17:14:18.572+0000\","
            + "     \"date_updated\": \"2018-12-19T17:14:18.572+0000\""
            + "   }"
            + " ]"
            + "}");

        PaymentResponse paymentResponse = pbaPaymentService.makePayment(AUTH_TOKEN, callbackRequest.getCaseDetails());

        assertThat(paymentResponse.getReference(), is("RC-1545-2396-5857-4110"));
        assertThat(paymentResponse.getStatus(), is("Failed"));
        assertThat(paymentResponse.isPaymentSuccess(), is(false));
        assertThat(paymentResponse.getPaymentError(), is("You have insufficient funds available"));
        assertThat(paymentResponse.getStatusHistories().size(), is(1));
        assertThat(paymentResponse.getStatusHistories().get(0).getErrorCode(), is("CA-E0001"));
        assertThat(paymentResponse.getStatusHistories().get(0).getErrorMessage(),
            is("You have insufficient funds available"));
    }

    @Test
    public void accountOnHoldWithCaseType() throws Exception {
        when(featureToggleService.isPBAUsingCaseTypeEnabled()).thenReturn(true);

        setUpPbaPaymentForCaseType("{"
            + " \"reference\": \"RC-1545-2396-5857-4110\","
            + " \"date_created\": \"2018-12-19T17:14:18.572+0000\","
            + " \"status\": \"Failed\","
            + " \"status_histories\": ["
            + "   {"
            + "     \"status\": \"failed\","
            + "     \"error_code\": \"CA-E0003\","
            + "     \"error_message\": \"Your account is on hold\","
            + "     \"date_created\": \"2018-12-19T17:14:18.572+0000\","
            + "     \"date_updated\": \"2018-12-19T17:14:18.572+0000\""
            + "   }"
            + " ]"
            + "}");

        PaymentResponse paymentResponse = pbaPaymentService.makePayment(AUTH_TOKEN, callbackRequest.getCaseDetails());

        assertThat(paymentResponse.getReference(), is("RC-1545-2396-5857-4110"));
        assertThat(paymentResponse.getStatus(), is("Failed"));
        assertThat(paymentResponse.isPaymentSuccess(), is(false));
        assertThat(paymentResponse.getPaymentError(), is("Your account is on hold"));
        assertThat(paymentResponse.getStatusHistories().size(), is(1));
        assertThat(paymentResponse.getStatusHistories().get(0).getErrorCode(), is("CA-E0003"));
        assertThat(paymentResponse.getStatusHistories().get(0).getErrorMessage(), is("Your account is on hold"));
    }

    @Test
    public void accountDeletedWithCaseType() throws Exception {
        when(featureToggleService.isPBAUsingCaseTypeEnabled()).thenReturn(true);

        setUpPbaPaymentForCaseType("{"
            + " \"reference\": \"RC-1545-2396-5857-4110\","
            + " \"date_created\": \"2018-12-19T17:14:18.572+0000\","
            + " \"status\": \"Failed\","
            + " \"status_histories\": ["
            + "   {"
            + "     \"status\": \"failed\","
            + "     \"error_code\": \"CA-E0004\","
            + "     \"error_message\": \"Your account is deleted\","
            + "     \"date_created\": \"2018-12-19T17:14:18.572+0000\","
            + "     \"date_updated\": \"2018-12-19T17:14:18.572+0000\""
            + "   }"
            + " ]"
            + "}");

        PaymentResponse paymentResponse = pbaPaymentService.makePayment(AUTH_TOKEN, callbackRequest.getCaseDetails());

        assertThat(paymentResponse.getReference(), is("RC-1545-2396-5857-4110"));
        assertThat(paymentResponse.getStatus(), is("Failed"));
        assertThat(paymentResponse.isPaymentSuccess(), is(false));
        assertThat(paymentResponse.getPaymentError(), is("Your account is deleted"));
        assertThat(paymentResponse.getStatusHistories().size(), is(1));
        assertThat(paymentResponse.getStatusHistories().get(0).getErrorCode(), is("CA-E0004"));
        assertThat(paymentResponse.getStatusHistories().get(0).getErrorMessage(), is("Your account is deleted"));
    }

    @Test
    public void accessIsDeniedWithCaseType() throws Exception {
        when(featureToggleService.isPBAUsingCaseTypeEnabled()).thenReturn(true);

        setUpPbaPaymentForCaseType("{"
            + "  \"timestamp\": \"2019-01-09T17:59:20.473+0000\","
            + "  \"status\": 403,"
            + "  \"error\": \"Forbidden\","
            + "  \"message\": \"Access Denied\","
            + "  \"path\": \"/credit-account-payments\""
            + "}");

        PaymentResponse paymentResponse = pbaPaymentService.makePayment(AUTH_TOKEN, callbackRequest.getCaseDetails());

        assertThat(paymentResponse.getReference(), nullValue());
        assertThat(paymentResponse.getStatus(), is("403"));
        assertThat(paymentResponse.isPaymentSuccess(), is(false));
        assertThat(paymentResponse.getPaymentError(), is("Access Denied"));
        assertThat(paymentResponse.getStatusHistories(), nullValue());
    }

    private void setUpPbaPaymentForSiteId(String response) {
        paymentService.stubFor(post(urlPathEqualTo(PBA_PAYMENT_API))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(response)));
    }

    private void setUpPbaPaymentForCaseType(String response) {
        paymentService.stubFor(post(urlPathEqualTo(PBA_PAYMENT_API))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(response)));
    }
}