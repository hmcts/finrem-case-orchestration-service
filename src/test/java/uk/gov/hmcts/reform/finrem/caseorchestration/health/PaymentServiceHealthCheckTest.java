package uk.gov.hmcts.reform.finrem.caseorchestration.health;

public class PaymentServiceHealthCheckTest extends AbstractServiceHealthCheckTest {

    private static final String URI = "http://localhost:4006/health";

    @Override
    protected String uri() {
        return URI;
    }

    @Override
    protected AbstractServiceHealthCheck healthCheckInstance() {
        return new PaymentServiceHealthCheck(URI, restTemplate);
    }
}
