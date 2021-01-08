package uk.gov.hmcts.reform.finrem.caseorchestration.contracts;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.model.RequestResponsePact;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.util.Arrays;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "SIDAM_Provider", port = "8889")
@SpringBootTest({
    "idam.url: http://localhost:8889"
})
public class SidamServiceConsumerTest extends BaseTest {

    @Autowired
    private IdamService idamService;
    private static final String AUTH_TOKEN = "someAuthorizationToken";

    @Pact(state = "SIDAM Returns user details",
        provider = "SIDAM_Service", consumer = "finrem_caseorchetration_service")
    public RequestResponsePact sidamServicePact(PactDslWithProvider packBuilder) throws JSONException {

        return packBuilder
            .given("Provider haa user details")
            .uponReceiving("GET request for isAdmin ")
            .path("/details")
            .method("GET")
            .headers(AUTHORIZATION_HEADER, AUTH_TOKEN, CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .willRespondWith()
            .status(200)
            .body(idamUserDetailsResponse())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "sidamServicePact")
    public void verifyIdamUserDetailsRolesPact() {
        boolean isUserRoleAdmin = idamService.isUserRoleAdmin(AUTH_TOKEN);
        assertTrue("User is not Admin", isUserRoleAdmin);
    }

    @Test
    @PactTestFor(pactMethod = "sidamServicePact")
    public void verifyIdamUserDetailsFullNamePact() {
        String userName = idamService.getIdamFullName(AUTH_TOKEN);
        assertEquals("User is not Admin", userName, "Testforename Testsurname");
    }

    private JSONObject idamUserDetailsResponse() throws JSONException {
        JSONObject details = new JSONObject();
        details.put("forename", "Testforename");
        details.put("surname", "Testsurname");
        details.putOpt("roles",
            new JSONArray(Arrays.asList("caseworker-divorce-financialremedy-courtadmin", "Test")));
        return details;
    }
}
