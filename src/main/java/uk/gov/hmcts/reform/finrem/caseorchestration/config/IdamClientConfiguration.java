package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;

public class IdamClientConfiguration {
    @Bean
    public IdamClient idamClient(IdamApi idamApi, OAuth2Configuration oauth2Configuration) {
        return new IdamClient(idamApi, oauth2Configuration);
    }
}
