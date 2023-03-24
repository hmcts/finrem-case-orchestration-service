package uk.gov.hmcts.reform.finrem.caseorchestration.contracts.evidencemanagement;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;

import java.io.IOException;

@SpringBootTest
public class EvidenceManagementDeleteServiceConsumerTest extends BaseTest {

    private static final String authorizationToken = "Bearer some-access-token";
    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String USER_ID_VALUE = "1000";
    public static final String REQUEST_ID = "reqId";
    private final String someServiceAuthToken = "someServiceAuthToken";
    private static final String USER_ID_HEADER = "user-id";
    private static final String DOCUMENT_ID = "5c3c3906-2b51-468e-8cbb-a4002eded075";
    private static final String DELETE_FILE_URL = "/documents/" + DOCUMENT_ID;

    @MockBean
    private IdamAuthService userService;

    @Autowired
    private EvidenceManagementDeleteService evidenceManagementDeleteService;

    @Autowired
    RestTemplate restTemplate;


    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("em_dm_store", "localhost", 8889, this);

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @Pact(provider = "em_dm_store", consumer = "fr_evidenceManagementClient")
    public RequestResponsePact generatePactFragment(final PactDslWithProvider builder) throws JSONException, IOException {
        // @formatter:off
        return builder
            .given("I have existing document")
            .uponReceiving("A request to Delete a document")
            .method("DELETE")
            .headers(SERVICE_AUTHORIZATION_HEADER, someServiceAuthToken, USER_ID_HEADER, "1000")
            .path(DELETE_FILE_URL)
            .willRespondWith()
            .status(HttpStatus.SC_NO_CONTENT)
            .toPact();
    }
}
