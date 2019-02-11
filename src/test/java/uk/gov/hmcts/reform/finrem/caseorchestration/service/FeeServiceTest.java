package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeResponse;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class FeeServiceTest  extends BaseServiceTest {

    @Autowired
    private FeeService feeService;

    @Test
    public void retrieveFee() {
        mockServer.expect(requestTo(toUri()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{ \"code\" : \"FEE0640\", \"fee_amount\" : 50, "
                        + "\"description\" : \"finrem\", \"version\" : \"v1\" }", MediaType.APPLICATION_JSON));

        FeeResponse feeResponse = feeService.getApplicationFee();

        assertThat(feeResponse.getCode(), Matchers.is("FEE0640"));
        assertThat(feeResponse.getDescription(), Matchers.is("finrem"));
        assertThat(feeResponse.getVersion(), Matchers.is("v1"));
        assertThat(feeResponse.getFeeAmount(), Matchers.is(BigDecimal.valueOf(50)));
    }

    private String toUri() {
        return "http://localhost:8086/api?service=other&jurisdiction1=family&jurisdiction2=family-court&channel=default"
                + "&event=general-application&keyword=without-notice";
    }

}