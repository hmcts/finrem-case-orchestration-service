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
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

public class FeeServiceTest extends BaseServiceTest {

    @Autowired
    private FeeService feeService;

    @ClassRule
    public static WireMockClassRule paymentService = new WireMockClassRule(9001);

    private static final String FEE_LOOKUP_API = "/payments/fee-lookup";

    @Test
    public void retrieveApplicationFee() {
        paymentService.stubFor(get(urlPathEqualTo(FEE_LOOKUP_API))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody("{\"code\": \"TEST\", \"fee_amount\": \"10\", "
                                + "\"description\": \"desc\", \"version\": \"1.0\"}")));

        FeeResponse feeResponse = feeService.getApplicationFee();
        assertThat(feeResponse.getCode(), is("TEST"));
        assertThat(feeResponse.getFeeAmount(), is(BigDecimal.TEN));
        assertThat(feeResponse.getDescription(), is("desc"));
        assertThat(feeResponse.getVersion(), is("1.0"));
    }
}