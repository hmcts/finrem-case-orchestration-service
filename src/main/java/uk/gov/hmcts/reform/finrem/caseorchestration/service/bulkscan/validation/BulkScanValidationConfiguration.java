package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BulkScanValidationConfiguration {

    @Bean
    public FactoryBean<? extends BulkScanFormValidator> formAValidatorFactoryBean() {
        return new FactoryBean<FormAValidator>() {
            @Override
            public FormAValidator getObject() throws Exception {
                return new FormAValidator();
            }

            @Override
            public Class<?> getObjectType() {
                return FormAValidator.class;
            }
        };
    }
}
