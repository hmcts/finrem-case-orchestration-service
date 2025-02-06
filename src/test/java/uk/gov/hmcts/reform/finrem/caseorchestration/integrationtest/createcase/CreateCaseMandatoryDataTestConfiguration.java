package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.createcase;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(basePackages = {"uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation"})
public class CreateCaseMandatoryDataTestConfiguration {
}
