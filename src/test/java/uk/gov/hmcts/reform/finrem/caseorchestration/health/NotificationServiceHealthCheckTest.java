package uk.gov.hmcts.reform.finrem.caseorchestration.health;

public class NotificationServiceHealthCheckTest extends AbstractServiceHealthCheckTest {

    private static final String URI = "http://localhost:4006/health";

    @Override
    protected String uri() {
        return URI;
    }

    @Override
    protected AbstractServiceHealthCheck healthCheckInstance() {
        return new NotificationServiceHealthCheck(URI, restTemplate);
    }
}
