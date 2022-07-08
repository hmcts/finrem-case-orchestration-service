package uk.gov.hmcts.reform.finrem.caseorchestration.service.payments;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;

@TestPropertySource(locations = "/application.properties")
public abstract class PaymentsBaseServiceTest extends BaseServiceTest {

    @Autowired
    protected RestTemplate restTemplate;

    protected MockRestServiceServer mockServer;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }
}
