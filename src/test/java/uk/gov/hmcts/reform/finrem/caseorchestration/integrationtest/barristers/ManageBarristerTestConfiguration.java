package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.barristers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

@TestConfiguration
@ComponentScan(basePackages = {"uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers",
    "uk.gov.hmcts.reform.finrem.caseorchestration.handler.managebarrister",
    "uk.gov.hmcts.reform.finrem.caseorchestration.helper"})
public class ManageBarristerTestConfiguration {

    @Bean
    public CaseDataService caseDataService() {
        return new CaseDataService(new ObjectMapper());
    }
}
