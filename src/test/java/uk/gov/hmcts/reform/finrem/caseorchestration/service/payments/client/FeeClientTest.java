package uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.client;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.PaymentsBaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.config.FeeServiceConfiguration;

import java.math.BigDecimal;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType.CONTESTED;


public class FeeClientTest extends PaymentsBaseServiceTest {

    @Autowired
    private FeeClient feeClient;

    @Value("${fees.consented-keyword}")
    private String consentedFeeKeyword;

    @Autowired
    private FeeServiceConfiguration serviceConfig;

    @Test
    public void retrieveConsentedFee() {
        String typeOfApplication  =  null;
        mockServer.expect(requestTo(consentedUri()))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("{ \"code\" : \"FEE0640\", \"fee_amount\" : 50, "
                + "\"description\" : \"finrem\", \"version\" : \"v1\" }", APPLICATION_JSON));

        FeeResponse feeResponse = feeClient.getApplicationFee(CONSENTED, typeOfApplication);

        MatcherAssert.assertThat(feeResponse.getCode(), Matchers.is("FEE0640"));
        MatcherAssert.assertThat(feeResponse.getDescription(), Matchers.is("finrem"));
        MatcherAssert.assertThat(feeResponse.getVersion(), Matchers.is("v1"));
        MatcherAssert.assertThat(feeResponse.getFeeAmount(), Matchers.is(BigDecimal.valueOf(50)));
    }


    @Test
    public void retrieveContestedFee() {
        String typeOfApplication  =  "In connection to matrimonial and civil partnership proceedings";
        mockServer.expect(requestTo(contestedUri()))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("{ \"code\" : \"FEE0229\", \"fee_amount\" : 255, "
                + "\"description\" : \"finrem\", \"version\" : \"v1\" }", APPLICATION_JSON));

        FeeResponse feeResponse = feeClient.getApplicationFee(CONTESTED, typeOfApplication);

        MatcherAssert.assertThat(feeResponse.getCode(), Matchers.is("FEE0229"));
        MatcherAssert.assertThat(feeResponse.getDescription(), Matchers.is("finrem"));
        MatcherAssert.assertThat(feeResponse.getVersion(), Matchers.is("v1"));
        MatcherAssert.assertThat(feeResponse.getFeeAmount(), Matchers.is(BigDecimal.valueOf(255)));
    }


    @Test
    public void retrieveContestedSchedule1ApplicationFee() {
        String typeOfApplication  =  "Under paragraph 1 or 2 of schedule 1 children act 1989";
        mockServer.expect(requestTo(contestedSchedule1Uri()))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("{ \"code\" : \"FEE0318\", \"fee_amount\" : 232, "
                + "\"description\" : \"finrem\", \"version\" : \"2\" }", APPLICATION_JSON));

        FeeResponse feeResponse = feeClient.getApplicationFee(CONTESTED, typeOfApplication);

        MatcherAssert.assertThat(feeResponse.getCode(), Matchers.is("FEE0318"));
        MatcherAssert.assertThat(feeResponse.getDescription(), Matchers.is("finrem"));
        MatcherAssert.assertThat(feeResponse.getVersion(), Matchers.is("2"));
        MatcherAssert.assertThat(feeResponse.getFeeAmount(), Matchers.is(BigDecimal.valueOf(232)));
    }

    @Test
    public void shouldDetermineKeyword() {
        String keyword = feeClient.getKeyword(CONSENTED);
        MatcherAssert.assertThat(keyword, Matchers.is(serviceConfig.getConsentedKeyword()));

        String keywordContested = feeClient.getKeyword(CONTESTED);
        MatcherAssert.assertThat(keywordContested,
            Matchers.is(serviceConfig.getFeePayNewKeywords()
                ? serviceConfig.getContestedNewKeyword() : serviceConfig.getContestedKeyword()));
    }

    private String consentedUri() {
        return "http://localhost:8182/fees-register/fees/lookup?service=other&jurisdiction1=family&jurisdiction2=family-court&channel=default"
            + "&event=general%20application&keyword=" + consentedFeeKeyword;
    }

    private String contestedUri() {
        return "http://localhost:8182/fees-register/fees/lookup?service=other&jurisdiction1=family&jurisdiction2=family-court&channel=default"
            + "&event=miscellaneous&keyword=" + (serviceConfig.getFeePayNewKeywords() ? serviceConfig.getContestedNewKeyword()
            : serviceConfig.getContestedKeyword());
    }

    private String contestedSchedule1Uri() {
        return "http://localhost:8182/fees-register/fees/lookup?service=private%20law&jurisdiction1=family&jurisdiction2=family-court&channel=default"
            + "&event=miscellaneous&keyword=" + serviceConfig.getSchedule1Keyword();
    }
}
