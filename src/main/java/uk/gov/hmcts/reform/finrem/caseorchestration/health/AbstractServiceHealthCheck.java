package uk.gov.hmcts.reform.finrem.caseorchestration.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Slf4j
public abstract class AbstractServiceHealthCheck implements HealthIndicator {

    private final String uri;
    private final RestTemplate restTemplate;

    public AbstractServiceHealthCheck(String uri, RestTemplate restTemplate) {
        this.uri = uri;
        this.restTemplate = restTemplate;
    }

    /**
     * Return an indication of health.
     *
     * @return the health for Fees service
     */
    @Override
    public Health health() {
        try {
            ResponseEntity<Object> response = restTemplate.getForEntity(uri, Object.class);
            return response.getStatusCode() == (HttpStatus.OK) ? statusHealthy() : statusUnknown();
        } catch (Exception ex) {
            log.error("Exception while checking health on {}, exception: {}", uri, ex);
            return statusError(ex);
        }
    }

    private Health statusError(Exception ex) {
        return Health.down().withDetail("uri", uri).withException(ex).build();
    }

    private Health statusHealthy() {
        return Health.up().withDetail("uri", uri).build();
    }

    private Health statusUnknown() {
        return Health.unknown().withDetail("uri", uri).build();
    }
}
