package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.scanneddocs;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.caselocation.CaseManagementLocationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.globalsearch.GlobalSearchService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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

    @Bean
    public CaseManagementLocationService caseManagementLocationService() {
        return Mockito.mock(CaseManagementLocationService.class);
    }

    @Bean
    public GlobalSearchService globalSearchService() {
        GlobalSearchService globalSearchService = Mockito.mock(GlobalSearchService.class);
        doNothing().when(globalSearchService).setGlobalSearchData(any(FinremCaseData.class));
        return globalSearchService;
    }
}
