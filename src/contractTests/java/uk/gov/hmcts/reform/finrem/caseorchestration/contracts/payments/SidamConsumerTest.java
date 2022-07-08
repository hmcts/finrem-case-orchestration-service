package uk.gov.hmcts.reform.finrem.caseorchestration.contracts.payments;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.io.IOException;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;


@SpringBootTest({"idam.url: http://localhost:8888"})
@TestPropertySource(locations = "classpath:application.properties")
@PactFolder("pacts")
public class SidamConsumerTest extends BaseTest {

    @Autowired
    private IdamService idamService;
    private static final String AUTH_TOKEN = "Bearer someAuthorizationToken";


    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("idamApi_users", "localhost", 8888, this);

    @Pact(provider = "idamApi_users", consumer = "fr_caseOrchestratorService")
    public RequestResponsePact generatePactFragment(PactDslWithProvider builder) throws JSONException, IOException {

        return builder
            .given("a valid user exists")
            .uponReceiving("A request for a User")
            .path("/details")
            .method("GET")
            .matchHeader("Authorization", AUTH_TOKEN)
            .matchHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .willRespondWith()
            .status(200)
            .body(buildIdamDetailsResponseDsl())
            .toPact();
    }

    @Test
    @PactVerification()
    public void verifyIdamUserDetailsRolesPact() {
        String userEmail = idamService.getUserEmailId(AUTH_TOKEN);
        assertEquals("User is not Admin", "joe.bloggs@hmcts.net", userEmail);
    }


    private DslPart buildIdamDetailsResponseDsl() {
        return newJsonBody((o) -> {
            o.stringType("id",
                    "123432")
                .stringType("forename", "Joe")
                .stringType("surname", "Bloggs")
                .stringType("email", "joe.bloggs@hmcts.net")
                .booleanType("active", true)
                .array("roles", r -> r.stringType("caseworker"))
            ;


        }).build();
    }
}
