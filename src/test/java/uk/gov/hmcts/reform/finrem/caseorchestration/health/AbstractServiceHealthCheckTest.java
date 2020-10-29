package uk.gov.hmcts.reform.finrem.caseorchestration.health;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractServiceHealthCheckTest {

    protected abstract String uri();

    @Mock
    protected RestTemplate restTemplate;

    protected abstract AbstractServiceHealthCheck healthCheckInstance();

    @Test
    public void statusHealthy() {
        String uri = uri();
        when(restTemplate.getForEntity(eq(uri), eq(Object.class))).thenReturn(ResponseEntity.ok(""));
        assertThat(healthCheckInstance().health(), is(Health.up().withDetail("uri", uri()).build()));
    }

    @Test
    public void statusDownHttError() {
        doHealthDownTest(new HttpClientErrorException(UNAUTHORIZED));
    }

    @Test
    public void statusDownError() {
        doHealthDownTest(new RuntimeException());
    }

    @Test
    public void statusUnknown() {
        String uri = uri();

        when(restTemplate.getForEntity(eq(uri), eq(Object.class)))
            .thenReturn(ResponseEntity.accepted().build());
        assertThat(healthCheckInstance().health(), is(Health.unknown().withDetail("uri", uri).build()));
    }

    private void doHealthDownTest(Exception ex) {
        String uri = uri();

        when(restTemplate.getForEntity(eq(uri), eq(Object.class)))
            .thenThrow(ex);

        assertThat(healthCheckInstance().health(), is(Health.down().withDetail("uri", uri).withException(ex).build()));

    }
}
