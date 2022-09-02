package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType;

import java.io.IOException;
import java.io.InputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType.CONTESTED;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.properties")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Slf4j
@Category(IntegrationTest.class)
public class FeeLookUpTest extends BaseTest {

    private static final String FEE_REGISTER_CONSENTED_URL =
        "/fees-register/fees/lookup\\?service=other&jurisdiction1=family&jurisdiction2=family-court&channel=default&event=general%20application"
            + "&keyword=GeneralAppWithoutNotice";
    private static final String FEE_REGISTER_CONTESTED_URL =
        "/fees-register/fees/lookup\\?service=other&jurisdiction1=family&jurisdiction2=family-court&channel=default&event=miscellaneous"
            + "&keyword=FinancialOrderOnNotice";

    private static final String COS_URL = "/case-orchestration/fee-lookup";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule feeLookUpService = new WireMockClassRule(8182);

    private CallbackRequest request;

    public CallbackRequest request(ApplicationType applicationType) throws IOException {
        String fileName = applicationType == CONSENTED
            ? "/fixtures/fee-lookup.json" : "/fixtures/contested/fee-lookup.json";
        try (InputStream resourceAsStream = getClass().getResourceAsStream(fileName)) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        }
    }

    @Test
    public void consentedFeeLookup() throws Exception {
        stubFeeLookUp(CONSENTED);
        webClient.perform(MockMvcRequestBuilders.post(COS_URL)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request(CONSENTED))))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(content()
                .json(expectedBody(CONSENTED)));
        verify(getRequestedFor(urlMatching(FEE_REGISTER_CONSENTED_URL)));
    }

    @Test
    public void contestedFeeLookup() throws Exception {
        stubFeeLookUp(CONTESTED);
        webClient.perform(MockMvcRequestBuilders.post(COS_URL)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request(CONTESTED))))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(content()
                .json(expectedBody(CONTESTED)));
        verify(getRequestedFor(
            urlMatching(FEE_REGISTER_CONTESTED_URL)));
    }

    private String expectedBody(ApplicationType applicationType) {
        String amount = applicationType == CONSENTED ? "5000" : "25500";
        return "{\"data\":{"
            + "\"amountToPay\":\"" + amount + "\","
            + "\"orderSummary\":{\"PaymentReference\":\"ABCD\","
            + "\"PaymentTotal\":\"" + amount + "\","
            + "\"Fees\":[{\"value\":"
            + "{\"FeeDescription\":\"Application (without notice)\","
            + "\"FeeVersion\":\"1\","
            + "\"FeeCode\":\"FEE0600\","
            + "\"FeeAmount\":\"" + amount + "\"}}]}"
            + "},"
            + "\"errors\":null,"
            + "\"warnings\":null}";
    }

    private void stubFeeLookUp(ApplicationType applicationType) {
        stubFor(get(urlMatching(applicationType == ApplicationType.CONSENTED ? FEE_REGISTER_CONSENTED_URL : FEE_REGISTER_CONTESTED_URL))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(response(applicationType))));
    }

    private String response(ApplicationType applicationType) {
        int amount = applicationType == CONSENTED ? 50 : 255;
        return "{\n"
            + "  \"code\": \"FEE0600\",\n"
            + "  \"description\": \"Application (without notice)\",\n"
            + "  \"version\": 1,\n"
            + "  \"fee_amount\": " + amount + "\n"
            + "}";
    }
}
