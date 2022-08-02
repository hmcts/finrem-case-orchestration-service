package uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
public abstract class AbstractServiceHealthCheck implements HealthIndicator {

    private final String uri;
    private final RestTemplate restTemplate;

    public AbstractServiceHealthCheck(String uri, RestTemplate restTemplate) {
        this.uri = uri;
        this.restTemplate = restTemplate;
    }

    @Override
    public Health health() {
        try {
            ResponseEntity<Object> response = restTemplate.getForEntity(uri, Object.class);
            return response.getStatusCode() == (HttpStatus.OK) ? statusHealthy(uri) : statusUnknown(uri);
        } catch (Exception ex) {
            log.error("Exception while checking health on {}", uri, ex);
            return statusError(ex, uri);
        }
    }

    private Health statusHealthy(String uri) {
        return Health.up().withDetail("uri", uri).build();
    }

    private Health statusError(Exception ex, String uri) {
        return Health.down().withDetail("uri", uri).withException(ex).build();
    }

    private Health statusUnknown(String uri) {
        return Health.unknown().withDetail("uri", uri).build();
    }
}
