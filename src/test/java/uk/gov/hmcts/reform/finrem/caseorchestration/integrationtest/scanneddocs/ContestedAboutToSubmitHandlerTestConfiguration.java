package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.scanneddocs;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import static org.mockito.Mockito.when;

@TestConfiguration
@ComponentScan(basePackages = {"uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments"})
public class ContestedAboutToSubmitHandlerTestConfiguration {

    @Bean
    public FeatureToggleService featureToggleService() {
        FeatureToggleService featureToggleService = Mockito.mock(FeatureToggleService.class);
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        return featureToggleService;
    }
}
