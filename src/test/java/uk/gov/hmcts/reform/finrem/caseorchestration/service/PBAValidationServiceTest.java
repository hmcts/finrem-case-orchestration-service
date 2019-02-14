package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

public class PBAValidationServiceTest extends BaseServiceTest {

    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9";

    @Autowired
    private PBAValidationService pbaValidationService;

    @ClassRule
    public static WireMockClassRule paymentService = new WireMockClassRule(9001);

    private static final String PBA_VALIDATE_API = "/payments/pba-validate/";

    @Test
    public void validPbaPositive() {
        setUpPbaValidateService("NUM1", "{\"pbaNumberValid\": true}");
        assertThat(pbaValidationService.isValidPBA(AUTH_TOKEN, "NUM1"), is(true));
    }

    @Test
    public void validPbaNegative() {
        setUpPbaValidateService("NUM3", "{\"pbaNumberValid\": false}");
        assertThat(pbaValidationService.isValidPBA(AUTH_TOKEN, "NUM3"), is(false));
    }

    private void setUpPbaValidateService(String pbaNumber, String response) {
        paymentService.stubFor(get(urlPathEqualTo(PBA_VALIDATE_API + pbaNumber))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(response)));

    }
}