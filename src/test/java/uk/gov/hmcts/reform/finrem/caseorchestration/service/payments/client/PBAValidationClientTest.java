package uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.validation.PBAValidationResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.PaymentsBaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.config.PBAValidationServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.error.InvalidTokenException;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.client.PBAValidationClient.USER_EMAIL;

public class PBAValidationClientTest extends PaymentsBaseServiceTest {

    private static final String EMAIL = "test@test.com";
    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9";
    private static final String INVALID_AUTH_TOKEN = "eyJhbGciOiJIUzI1NiJ9";
    private static final String URL = "http://localhost:9001/v1/organisations/pbas";

    @Autowired
    private PBAValidationClient pbaValidationClient;

    @MockitoBean
    private IdamService idamService;

    @MockitoBean
    private PBAValidationServiceConfiguration pbaValidationServiceConfiguration;

    private JsonNode requestContent;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        when(pbaValidationServiceConfiguration.getUrl()).thenReturn("http://localhost:9001");
        when(pbaValidationServiceConfiguration.getApi()).thenReturn("/v1/organisations/pbas");
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/payment-by-account-full.json").toURI()));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        when(idamService.getUserEmailId(AUTH_TOKEN)).thenReturn(EMAIL);
    }

    @Test
    public void pbaNotFound() {
        mockServer.expect(requestTo(URL))
            .andExpect(method(GET))
            .andRespond(withStatus(NOT_FOUND));

        PBAValidationResponse response = pbaValidationClient.isPBAValid(AUTH_TOKEN, "NUM3");
        assertThat(response.isPbaNumberValid(), is(false));
    }

    @Test
    public void validPbaPositive() {
        mockServer.expect(requestTo(URL))
            .andExpect(method(GET))
            .andRespond(withSuccess(requestContent.toString(), APPLICATION_JSON));

        PBAValidationResponse response = pbaValidationClient.isPBAValid(AUTH_TOKEN, "NUM1");
        assertThat(response.isPbaNumberValid(), is(true));
    }

    @Test
    public void validPbaNegative() {
        mockServer.expect(requestTo(URL))
            .andExpect(method(GET))
            .andRespond(withSuccess(requestContent.toString(), APPLICATION_JSON));

        PBAValidationResponse response = pbaValidationClient.isPBAValid(AUTH_TOKEN, "NUM3");
        assertThat(response.isPbaNumberValid(), is(false));
    }

    @Test
    public void validPbaNoPbaResult() {
        mockServer.expect(requestTo(URL))
            .andExpect(header(USER_EMAIL, EMAIL))
            .andExpect(method(GET))
            .andRespond(withSuccess("""
            {
              "organisationEntityResponse": {
                "organisationIdentifier": "LY7RZOE",
                "name": "TestOrg1",
                "status": "ACTIVE",
                "sraId": "1111",
                "sraRegulated": true,
                "companyNumber": "1110111",
                "companyUrl": "http://testorg2.co.uk",
                "superUser": {
                  "userIdentifier": "9503a799-5f4f-4814-8227-776ef5c4dce8",
                  "firstName": "Henry",
                  "lastName": "Harper",
                  "email": "henry_fr_harper@yahoo.com"
                },
                "paymentAccount": []
              }
            }
            """, APPLICATION_JSON));

        PBAValidationResponse response = pbaValidationClient.isPBAValid(AUTH_TOKEN, "NUM1");
        assertThat(response.isPbaNumberValid(), is(false));
    }

    @Test(expected = InvalidTokenException.class)
    public void invalidToken() {
        mockServer.expect(requestTo(URL))
            .andExpect(method(GET))
            .andRespond(withSuccess(requestContent.toString(), APPLICATION_JSON));

        pbaValidationClient.isPBAValid(INVALID_AUTH_TOKEN, "NUM1");
    }

    @Test(expected = HttpServerErrorException.class)
    public void internalServerErrorResponse() {
        mockServer.expect(requestTo(URL))
            .andExpect(method(GET))
            .andRespond(withServerError());

        pbaValidationClient.isPBAValid(AUTH_TOKEN, "NUM1");
    }
}
