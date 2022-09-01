package uk.gov.hmcts.reform.finrem.caseorchestration.contracts.evidencemanagement;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@SpringBootTest
public class EvidenceManagementDownloadServiceConsumerTest extends BaseTest {
    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private final String someServiceAuthToken = "someServiceAuthToken";
    private static final String DOCUMENT_ID = "5c3c3906-2b51-468e-8cbb-a4002eded075";
    private static final String DOWNLOAD_FILE_URL = "/documents/" + DOCUMENT_ID + "/binary";
    private static final String USER_ROLES = "user-roles";
    private static final String FINANCIAL_REMEDY_COURT_ADMIN = "caseworker-divorce-financialremedy-courtadmin";

    @Autowired
    private EvidenceManagementDownloadService evidenceManagementDownloadService;

    @Autowired
    RestTemplate restTemplate;

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("em_dm_store", "localhost", 8889, this);

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @Pact(provider = "em_dm_store", consumer = "fr_evidenceManagementClient")
    public RequestResponsePact generatePactFragment(final PactDslWithProvider builder) throws JSONException {
        // @formatter:off
        return builder
            .given("I have existing document")
            .uponReceiving("A request to download a document")
            .method("GET")
            .headers(SERVICE_AUTHORIZATION_HEADER, someServiceAuthToken, USER_ROLES, FINANCIAL_REMEDY_COURT_ADMIN)
            .path(DOWNLOAD_FILE_URL)
            .willRespondWith()
            .status(200)
            .toPact();
    }


    @Test
    @PactVerification()
    public void verifyDocumentDownloadFromDmStore() throws Exception {
        given(authTokenGenerator.generate()).willReturn(someServiceAuthToken);
        ResponseEntity<byte[]> responses = evidenceManagementDownloadService.download("http://localhost:8889" + DOWNLOAD_FILE_URL);
        assertTrue(responses.getStatusCode().is2xxSuccessful());
    }

}
