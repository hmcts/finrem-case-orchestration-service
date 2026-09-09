package uk.gov.hmcts.reform.finrem.caseorchestration.health;

public class IdamOidcServiceHealthCheckTest extends AbstractServiceHealthCheckTest {

    private static final String URI = "http://localhost:4501/health";

    @Override
    protected String uri() {
        return URI;
    }

    @Override
    protected AbstractServiceHealthCheck healthCheckInstance() {
        return new IdamOidcServiceHealthCheck(URI, restTemplate);
    }
}
