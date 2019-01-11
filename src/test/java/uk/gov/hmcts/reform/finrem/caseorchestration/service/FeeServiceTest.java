package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.Fee;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class FeeServiceTest extends BaseServiceTest {

    @Autowired
    private FeeService feeService;

    @Test
    public void retrieveApplicationFee() {
        mockServer.expect(requestTo(toUri()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"code\": \"TEST\", \"fee_amount\": \"10\"}", MediaType.APPLICATION_JSON));

        Fee fee = feeService.getApplicationFee();
        assertThat(fee.getCode(), is("TEST"));
        assertThat(fee.getFeeAmount(), is(BigDecimal.TEN));
    }

    private static String toUri() {
        return new StringBuilder("http://test/api")
                .append("?service=other")
                .append("&jurisdiction1=family")
                .append("&jurisdiction2=family-court")
                .append("&channel=default")
                .append("&event=general-application")
                .append("&keyword=without-notice")
                .toString();
    }
}