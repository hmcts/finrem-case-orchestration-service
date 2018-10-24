package uk.gov.hmcts.reform.finrem.finremcaseprogression.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.FinremCaseProgressionApplication;

import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = FinremCaseProgressionApplication.class)
@TestPropertySource(locations = "/application.properties")
public class PaymentByAccountServiceTest {

    private static final String EMAIL = "test@test.com";
    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9";

    @Autowired
    private PaymentByAccountService paymentByAccountService;

    @MockBean
    private IdamService idamService;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;
    private JsonNode requestContent;

    @Before
    public void setUp() throws Exception {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        ObjectMapper objectMapper = new ObjectMapper();

        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/payment-by-account.json").toURI()));

        when(idamService.getUserEmailId(AUTH_TOKEN)).thenReturn(EMAIL);
    }

    @Test
    public void validPbaPositive() {
        mockServer.expect(requestTo(toUri()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(requestContent.toString(), MediaType.APPLICATION_JSON));

        assertThat(paymentByAccountService.isValidPBA(AUTH_TOKEN, "NUM1"), is(true));
    }

    @Test
    public void validPbaNegative() {
        mockServer.expect(requestTo(toUri()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(requestContent.toString(), MediaType.APPLICATION_JSON));

        assertThat(paymentByAccountService.isValidPBA(AUTH_TOKEN, "NUM3"), is(false));
    }

    @Test
    public void validPbaNoPbaResult() {
        mockServer.expect(requestTo(toUri()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"payment_accounts\": []}", MediaType.APPLICATION_JSON));

        assertThat(paymentByAccountService.isValidPBA(AUTH_TOKEN, "NUM1"), is(false));
    }

    private static String toUri() {
        return new StringBuilder("http://test/case-progression/organisations/pba/")
                .append(EMAIL)
                .toString();
    }
}