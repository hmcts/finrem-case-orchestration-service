package uk.gov.hmcts.reform.finrem.caseorchestration;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.EvidenceManagementClient;

@Profile("test-mock-feign-clients")
@Configuration
public class MockFeignClientsTestConfiguration {

    @Bean
    @Primary
    public DocumentClient documentClient() {
        return Mockito.mock(DocumentClient.class);
    }

    @Bean
    @Primary
    public EvidenceManagementClient evidenceManagementClient() {
        return Mockito.mock(EvidenceManagementClient.class);
    }
}
