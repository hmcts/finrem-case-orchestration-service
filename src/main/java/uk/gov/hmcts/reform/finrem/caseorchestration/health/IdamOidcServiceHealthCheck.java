package uk.gov.hmcts.reform.finrem.caseorchestration.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class IdamOidcServiceHealthCheck extends AbstractServiceHealthCheck {

    @Autowired
    public IdamOidcServiceHealthCheck(
        @Value("${idam.oidc.health.url}") String uri,
        RestTemplate restTemplate) {
        super(uri, restTemplate);
    }
}
