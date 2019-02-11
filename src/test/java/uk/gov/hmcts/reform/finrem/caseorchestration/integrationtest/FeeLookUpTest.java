package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDRequest;

import java.io.IOException;
import java.io.InputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;


import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.properties")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Slf4j
@Category(IntegrationTest.class)
public class FeeLookUpTest {
    private static final String FEE_LOOKUP_URL = "/case-orchestration/fee-lookup";
    private static final String BODY = "{\n"
            + "  \"code\": \"FEE0600\",\n"
            + "  \"description\": \"Application (without notice)\",\n"
            + "  \"version\": 1,\n"
            + "  \"fee_amount\": 50\n"
            + "}";
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule feeLookUpService = new WireMockClassRule(8086);

    private CCDRequest request;

    @Before
    public void setUp() throws IOException {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(
                "/fixtures/fee-lookup.json")) {
            request = objectMapper.readValue(resourceAsStream, CCDRequest.class);
        }
    }

    @Test
    public void feeLookup() throws Exception {
        stubFeeLookUp();
        webClient.perform(MockMvcRequestBuilders.post(FEE_LOOKUP_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content()
                        .json(expectedBody()));
        verify(getRequestedFor(urlMatching(
                "/api.*")));
    }

    @Ignore
    @Test
    public void feeLookupWithError() throws Exception {
        stubWithError();
        webClient.perform(MockMvcRequestBuilders.post(FEE_LOOKUP_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private String expectedBody() {
        return "{\"data\":{"
                + "\"amountToPay\":\"5000\","
                + "\"orderSummary\":{\"PaymentReference\":\"ABCD\","
                + "\"PaymentTotal\":\"5000\",\""
                + "Fees\":[{\"value\":"
                + "{\"FeeDescription\":\"Application (without notice)\","
                + "\"FeeVersion\":\"1\","
                + "\"FeeCode\":\"FEE0600\","
                + "\"FeeAmount\":\"5000\"}}]}"
                + "},"
                + "\"errors\":null,"
                + "\"warnings\":null}";
    }

    private void stubFeeLookUp() {
        stubFor(get("/api?service=other&jurisdiction1=family"
                + "&jurisdiction2=family-court&channel=default&event=general-application&keyword=without-notice")
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(BODY)));
    }

    private void stubWithError() {
        stubFor(get("/api?service=other&jurisdiction1=family"
                + "&jurisdiction2=family-court&channel=default&event=general-application&keyword=without-notice")
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())));
    }
}
