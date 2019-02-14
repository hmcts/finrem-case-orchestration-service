package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class PBAValidationServiceTest extends BaseServiceTest {

    private static final String EMAIL = "test@test.com";
    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9";

    @Autowired
    private PBAValidationService pbaValidationService;

    private JsonNode requestContent;

    @Before
    public void setUp() {
        super.setUp();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            requestContent = objectMapper.readTree(new File(getClass()
                    .getResource("/fixtures/payment-by-account.json").toURI()));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void validPbaPositive() {
        mockServer.expect(requestTo(toUri()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(requestContent.toString(), MediaType.APPLICATION_JSON));

        assertThat(pbaValidationService.isValidPBA(AUTH_TOKEN, "NUM1"), is(true));
    }

    @Test
    public void validPbaNegative() {
        mockServer.expect(requestTo(toUri()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(requestContent.toString(), MediaType.APPLICATION_JSON));

        assertThat(pbaValidationService.isValidPBA(AUTH_TOKEN, "NUM3"), is(false));
    }

    @Test
    public void validPbaNoPbaResult() {
        mockServer.expect(requestTo(toUri()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"payment_accounts\": []}", MediaType.APPLICATION_JSON));

        assertThat(pbaValidationService.isValidPBA(AUTH_TOKEN, "NUM1"), is(false));
    }

    private static String toUri() {
        return new StringBuilder("http://test/case-orchestration/organisations/pba/")
                .append(EMAIL)
                .toString();
    }
}