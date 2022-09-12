package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import java.io.File;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

public class PBAValidationServiceTest extends BaseServiceTest {

    @Autowired
    private PBAValidationService pbaValidationService;

    @MockBean
    private IdamService idamService;

    @ClassRule
    public static WireMockClassRule pbaServiceClassRule = new WireMockClassRule(8090);

    private static final String PBA_VALIDATE_API = "/refdata/external/v1/organisations/pbas";

    private JsonNode responseContent;

    @Before
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            responseContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/payment-by-account-full.json").toURI()));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        when(idamService.getUserEmailId(AUTH_TOKEN)).thenReturn("test@email.com");
    }

    @Test
    public void validPbaPositive() {
        setUpPbaValidateService("{\"pbaNumberValid\": true}");
        assertThat(pbaValidationService.isValidPBA(AUTH_TOKEN, "NUM1"), is(true));
    }

    @Test
    public void validPbaNegative() {
        setUpPbaValidateService("{\"pbaNumberValid\": false}");
        assertThat(pbaValidationService.isValidPBA(AUTH_TOKEN, "NUM3"), is(false));
    }

    private void setUpPbaValidateService(String response) {
        pbaServiceClassRule.stubFor(get(urlPathEqualTo(PBA_VALIDATE_API))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(responseContent.toString())));
    }

}
