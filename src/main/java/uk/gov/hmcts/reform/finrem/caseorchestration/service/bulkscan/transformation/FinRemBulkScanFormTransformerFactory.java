package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;
import uk.gov.hmcts.reform.bsp.common.service.transformation.BulkScanFormTransformerFactory;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.BulkScanForms.FORM_A;

@Component
public class FinRemBulkScanFormTransformerFactory extends BulkScanFormTransformerFactory {

    @Autowired
    private FormAToCaseTransformer formAToCaseTransformer;

    @Override
    @PostConstruct
    public void init() {
        bulkScanFormTransformerMap = new HashMap<>();
        bulkScanFormTransformerMap.put(FORM_A, formAToCaseTransformer);
    }
}
