package uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.client.PBAValidationClient.USER_EMAIL;

public class PBAValidationClientTest extends PaymentsBaseServiceTest {

    private static final String EMAIL = "test@test.com";
    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9";
    private static final String INVALID_AUTH_TOKEN = "eyJhbGciOiJIUzI1NiJ9";

    @Autowired
    private PBAValidationClient pbaValidationClient;

    @MockBean
    private IdamService idamService;

    @MockBean
    private PBAValidationServiceConfiguration pbaValidationServiceConfiguration;

    private JsonNode requestContent;

    @Before
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
        mockServer.expect(requestTo(toUri()))
            .andExpect(method(GET))
            .andRespond(withStatus(NOT_FOUND));

        PBAValidationResponse response = pbaValidationClient.isPBAValid(AUTH_TOKEN, "NUM3");
        assertThat(response.isPbaNumberValid(), is(false));
    }


    @Test
    public void validPbaPositive() {
        mockServer.expect(requestTo(toUri()))
            .andExpect(method(GET))
            .andRespond(withSuccess(requestContent.toString(), APPLICATION_JSON));

        PBAValidationResponse response = pbaValidationClient.isPBAValid(AUTH_TOKEN, "NUM1");
        assertThat(response.isPbaNumberValid(), is(true));
    }

    @Test
    public void validPbaNegative() {
        mockServer.expect(requestTo(toUri()))
            .andExpect(method(GET))
            .andRespond(withSuccess(requestContent.toString(), APPLICATION_JSON));


        PBAValidationResponse response = pbaValidationClient.isPBAValid(AUTH_TOKEN, "NUM3");
        assertThat(response.isPbaNumberValid(), is(false));
    }

    @Test
    public void validPbaNoPbaResult() {
        mockServer.expect(requestTo(toUri()))
            .andExpect(header(USER_EMAIL, EMAIL))
            .andExpect(method(GET))
            .andRespond(withSuccess("{\n"
                + "  \"organisationEntityResponse\": {\n"
                + "    \"organisationIdentifier\": \"LY7RZOE\",\n"
                + "    \"name\": \"TestOrg1\",\n"
                + "    \"status\": \"ACTIVE\",\n"
                + "    \"sraId\": \"1111\",\n"
                + "    \"sraRegulated\": true,\n"
                + "    \"companyNumber\": \"1110111\",\n"
                + "    \"companyUrl\": \"http://testorg2.co.uk\",\n"
                + "    \"superUser\": {\n"
                + "      \"userIdentifier\": \"9503a799-5f4f-4814-8227-776ef5c4dce8\",\n"
                + "      \"firstName\": \"Henry\",\n"
                + "      \"lastName\": \"Harper\",\n"
                + "      \"email\": \"henry_fr_harper@yahoo.com\"\n"
                + "    },\n"
                + "    \"paymentAccount\": []\n"
                + "  }\n"
                + "}", APPLICATION_JSON));

        PBAValidationResponse response = pbaValidationClient.isPBAValid(AUTH_TOKEN, "NUM1");
        assertThat(response.isPbaNumberValid(), is(false));
    }

    private static String toUri() {
        return "http://localhost:9001/v1/organisations/pbas";
    }

    @Test(expected = InvalidTokenException.class)
    public void invalidToken() {
        mockServer.expect(requestTo(toUri()))
            .andExpect(method(GET))
            .andRespond(withSuccess(requestContent.toString(), APPLICATION_JSON));

        pbaValidationClient.isPBAValid(INVALID_AUTH_TOKEN, "NUM1");
    }
}
