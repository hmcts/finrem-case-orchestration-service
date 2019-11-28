package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.BulkScanForm;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Component
public class BulkScanFormValidatorFactory {

    private final Map<String, FactoryBean<? extends BulkScanFormValidator>> validators;

    public BulkScanFormValidatorFactory(Set<FactoryBean<? extends BulkScanFormValidator>> factoryBeans) {
        validators = mapBulkScanFormNamesToFactoryBeans(factoryBeans);
    }

    private Map<String, FactoryBean<? extends BulkScanFormValidator>> mapBulkScanFormNamesToFactoryBeans(
        Set<FactoryBean<? extends BulkScanFormValidator>> factoryBeans
    ) {
        return Arrays.stream(BulkScanForm.values())
            .collect(Collectors.toMap(
                BulkScanForm::getFormName,
                bulkScanForm -> findFactoryBeanForBulkScanForm(bulkScanForm, factoryBeans)));
    }

    private FactoryBean<? extends BulkScanFormValidator> findFactoryBeanForBulkScanForm(
        BulkScanForm bulkScanForm, Set<FactoryBean<? extends BulkScanFormValidator>> factoryBeans
    ) {
        return factoryBeans.stream()
            .filter(factoryBean -> factoryBean.getObjectType().equals(bulkScanForm.getFormValidatorClass()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(String.format("Factory bean for form %s not found",
                bulkScanForm.getFormName())));
    }

    public BulkScanFormValidator getValidator(final String formType) throws Exception {
        if (!validators.containsKey(formType)) {
            throw new UnsupportedOperationException(format("\"%s\" form type is not supported", formType));
        }

        return validators.get(formType).getObject();
    }
}
