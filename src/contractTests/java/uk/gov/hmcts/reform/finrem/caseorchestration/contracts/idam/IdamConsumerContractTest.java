package uk.gov.hmcts.reform.finrem.caseorchestration.contracts.idam;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.google.common.collect.Maps;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.IdamServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@SpringBootTest
public class IdamConsumerContractTest  extends BaseTest {

    private static final String IDAM_DETAILS_URL = "/details";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    public static final String FR_COURT_ADMIN = "caseworker-divorce-financialremedy-courtadmin";

    @Autowired
    IdamService idamService;

    @MockBean
    IdamServiceConfiguration idamServiceConfig;

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("Idam_api", "localhost", 8889, this);

    @Pact(provider = "Idam_api", consumer = "fr_caseOrchestratorService")
    public RequestResponsePact generatePactFragment(PactDslWithProvider builder) {
        Map<String, String> requestHeaders = Maps.newHashMap();
        requestHeaders.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        requestHeaders.put(AUTHORIZATION_HEADER,AUTHORIZATION_TOKEN);

        Map<String, String> responseheaders = Maps.newHashMap();
        responseheaders.put("Content-Type", "application/json");

        return builder
            .given("User exists in IDAM ")
            .uponReceiving("A request for retrieving details , the fullname of the user is returned")
            .path(IDAM_DETAILS_URL)
            .headers(requestHeaders)
            .method("GET")
            .willRespondWith()
            .status(200)
            .body(createUserInfoResponse())
            .toPact();
    }

    /**
     * One  Method that verifies the following Idam Interactions that check for fullName, userId and userRole
     * in the response.
     */

    @Test
    @PactVerification()
    public void verifyAllIdamInteractions() throws JSONException {
        given(idamServiceConfig.getUrl()).willReturn("http://localhost:8889");
        given(idamServiceConfig.getApi()).willReturn("/details");

        String fullName  = idamService.getIdamFullName(AUTHORIZATION_TOKEN);
        assertThat(fullName, is("Joe Bloggs"));

        String idamUserId  = idamService.getIdamUserId(AUTHORIZATION_TOKEN);
        assertThat(idamUserId, is("1234-2345-3456-4567"));

        boolean isUserRoleAdmin  = idamService.isUserRoleAdmin(AUTHORIZATION_TOKEN);
        assertTrue(isUserRoleAdmin);
    }

    private DslPart createUserInfoResponse() {
        return new PactDslJsonBody()
            .stringType("id", "1234-2345-3456-4567")
            .stringType("forename", "Joe")
            .stringType("surname", "Bloggs")
            .array("roles")
            .stringType(FR_COURT_ADMIN)
            .closeArray();
    }
}