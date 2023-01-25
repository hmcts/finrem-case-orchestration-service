package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeResponse;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType.CONTESTED;

public class FeeServiceTest extends BaseServiceTest {

    @Autowired
    private FeeService feeService;

    @ClassRule
    public static WireMockClassRule feeClient = new WireMockClassRule(8182);

    private static final String FEE_LOOKUP_API = "/fees-register/fees/lookup";

    @Test
    public void retrieveConsentedApplicationFee() {
        feeClient.stubFor(get(urlPathEqualTo(FEE_LOOKUP_API))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody("{\"code\": \"TEST\", \"fee_amount\": \"50\", "
                    + "\"description\": \"desc\", \"version\": \"1.0\"}")));

        FeeResponse feeResponse = feeService.getApplicationFee(CONSENTED, null);
        assertThat(feeResponse.getCode(), is("TEST"));
        assertThat(feeResponse.getFeeAmount(), is(BigDecimal.valueOf(50)));
        assertThat(feeResponse.getDescription(), is("desc"));
        assertThat(feeResponse.getVersion(), is("1.0"));
    }

    @Test
    public void retrieveContestedApplicationFee() {
        String typeOfApplication  =  "In connection to matrimonial and civil partnership proceedings";
        feeClient.stubFor(get(urlPathEqualTo(FEE_LOOKUP_API))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody("{\"code\": \"TEST\", \"fee_amount\": \"255\", "
                    + "\"description\": \"desc\", \"version\": \"1.0\"}")));

        FeeResponse feeResponse = feeService.getApplicationFee(CONTESTED, typeOfApplication);
        assertThat(feeResponse.getCode(), is("TEST"));
        assertThat(feeResponse.getFeeAmount(), is(BigDecimal.valueOf(255)));
        assertThat(feeResponse.getDescription(), is("desc"));
        assertThat(feeResponse.getVersion(), is("1.0"));
    }


    @Test
    public void retrieveContestedScedule1ApplicationFee() {
        String typeOfApplication  =  "Under paragraph 1 or 2 of schedule 1 children act 1989";
        feeClient.stubFor(get(urlPathEqualTo(FEE_LOOKUP_API))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody("{\"code\": \"TEST\", \"fee_amount\": \"232\", "
                    + "\"description\": \"desc\", \"version\": \"1.0\"}")));

        FeeResponse feeResponse = feeService.getApplicationFee(CONTESTED, typeOfApplication);
        assertThat(feeResponse.getCode(), is("TEST"));
        assertThat(feeResponse.getFeeAmount(), is(BigDecimal.valueOf(232)));
        assertThat(feeResponse.getDescription(), is("desc"));
        assertThat(feeResponse.getVersion(), is("1.0"));
    }
}