package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.service.BulkScanFormValidatorFactory;

import javax.annotation.PostConstruct;
import java.util.HashMap;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.BulkScanForms.FORM_A;

@Component
public class FinRemBulkScanFormValidatorFactory extends BulkScanFormValidatorFactory {

    @Autowired
    private FormAValidator formAValidator;

    @Override
    @PostConstruct
    public void initBean() {
        validators = new HashMap<>();
        validators.put(FORM_A, formAValidator);
    }
}
