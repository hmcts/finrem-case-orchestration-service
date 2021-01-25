package uk.gov.hmcts.reform.finrem.caseorchestration.health;

public class IdamServiceHealthCheckTest extends AbstractServiceHealthCheckTest {

    private static final String URI = "http://localhost:8080/health";

    @Override
    protected String uri() {
        return URI;
    }

    @Override
    protected AbstractServiceHealthCheck healthCheckInstance() {
        return new IdamServiceHealthCheck(URI, restTemplate);
    }
}