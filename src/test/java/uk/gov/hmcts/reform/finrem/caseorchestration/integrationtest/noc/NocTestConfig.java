package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.noc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateSolicitorDetailsService;

@TestConfiguration
@ComponentScan(basePackages = {"uk.gov.hmcts.reform.finrem.caseorchestration.service.noc",
    "uk.gov.hmcts.reform.finrem.caseorchestration.helper", "uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence"})
public class NocTestConfig {

    @Bean
    public CaseDataService caseDataService() {
        return new CaseDataService(new ObjectMapper());
    }

    @Bean
    public UpdateSolicitorDetailsService solicitorContactDetailsService(PrdOrganisationService prdOrganisationService) {
        return new UpdateSolicitorDetailsService(prdOrganisationService, new ObjectMapper(), caseDataService());
    }

    @Bean
    public PrdOrganisationService prdOrganisationService() {
        return Mockito.mock(PrdOrganisationService.class);
    }
}
