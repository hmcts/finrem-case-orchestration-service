package uk.gov.hmcts.reform.finrem.finremcaseprogression.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.FinremCaseProgressionApplication;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.model.fee.Fee;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = FinremCaseProgressionApplication.class)
@TestPropertySource(locations = "/application.properties")
public class FeeServiceTest {

    @Autowired
    private FeeService feeService;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void retrieveApplicationFee() {
        mockServer.expect(requestTo(toUri()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"code\": \"TEST\", \"fee_amount\": \"10\"}", MediaType.APPLICATION_JSON));

        Fee fee = feeService.getApplicationFee();
        assertThat(fee.getCode(), is("TEST"));
        assertThat(fee.getFeeAmount(), is(new BigDecimal("10")));
    }

    private String toUri() {
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