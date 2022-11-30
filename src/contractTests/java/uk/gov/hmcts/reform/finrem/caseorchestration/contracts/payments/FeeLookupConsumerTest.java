package uk.gov.hmcts.reform.finrem.caseorchestration.contracts.payments;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeeService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.config.FeeServiceConfiguration;

import static org.junit.Assert.assertEquals;


@SpringBootTest({"fees.url: http://localhost:8889"})
@TestPropertySource(locations = "classpath:application.properties")
public class FeeLookupConsumerTest extends BaseTest {

    @Autowired
    FeeService feeService;

    @Autowired
    private FeeServiceConfiguration serviceConfig;

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("feeRegister_lookUp", "localhost", 8889, this);

    @Pact(provider = "feeRegister_lookUp", consumer = "fr_caseOrchestratorService")
    public RequestResponsePact generateConsentedFeesPactFragment(PactDslWithProvider builder) throws JSONException {
        return buildRequestResponsePact(builder, "general application", "GeneralAppWithoutNotice",
            "FEE0640", "Consented Fees exist for Financial Remedy", "a request for Consented FR fees");

    }

    @Pact(provider = "feeRegister_lookUp", consumer = "fr_caseOrchestratorService")
    public RequestResponsePact generateContestedFeesPactFragment(PactDslWithProvider builder) throws JSONException {
        return buildRequestResponsePact(builder, "miscellaneous", serviceConfig.getFeePayNewKeywords()
                ? serviceConfig.getContestedNewKeyword() : serviceConfig.getContestedKeyword(),
            "FEE0229", "Contested Fees exist for Financial Remedy", "a request for Contested FR fees");

    }

    private RequestResponsePact buildRequestResponsePact(PactDslWithProvider builder, String event, String keyword, String feeCode, String state,
                                                         String description) {
        return builder
            .given(state)
            .uponReceiving(description)
            .path("/fees-register/fees/lookup")
            .method("GET")
            .matchQuery("service", "other", "other")
            .matchQuery("jurisdiction1", "family", "family")
            .matchQuery("jurisdiction2", "family court", "family court")
            .matchQuery("channel", "default", "default")
            .matchQuery("event", event, event)
            .matchQuery("keyword", keyword, keyword)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildFeesResponseBodyDsl(feeCode))
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private PactDslJsonBody buildFeesResponseBodyDsl(String feeCode) {
        return new PactDslJsonBody()
            .stringType("code", feeCode)
            .stringType("description", "Fee Description")
            .numberType("version", 1)
            .decimalType("fee_amount", 200.00);
    }

    @Test
    @PactVerification(fragment = "generateConsentedFeesPactFragment")
    public void verifyConsentedFeesServicePact() {
        FeeResponse applicationFee = feeService.getApplicationFee(ApplicationType.CONSENTED, null);
        assertEquals("FEE0640", applicationFee.getCode());
    }

    @Test
    @PactVerification(fragment = "generateContestedFeesPactFragment")
    public void verifyContestedFeesServicePact() {
        String typeOfApplication  = "In connection to matrimonial and civil partnership proceedings";
        FeeResponse applicationFee = feeService.getApplicationFee(ApplicationType.CONTESTED, typeOfApplication);
        assertEquals("FEE0229", applicationFee.getCode());
    }

}
