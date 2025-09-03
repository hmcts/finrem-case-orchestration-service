package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.service.BulkScanFormValidatorFactory;

import java.util.HashMap;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.BulkScanForms.FORM_A;

@Component
@RequiredArgsConstructor
public class FinRemBulkScanFormValidatorFactory extends BulkScanFormValidatorFactory {

    private final FormAValidator formAValidator;

    @Override
    @PostConstruct
    public void initBean() {
        validators = new HashMap<>();
        validators.put(FORM_A, formAValidator);
    }
}
