package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.service.transformation.BulkScanFormTransformerFactory;

import java.util.HashMap;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.BulkScanForms.FORM_A;

@Component
@RequiredArgsConstructor
public class FinRemBulkScanFormTransformerFactory extends BulkScanFormTransformerFactory {

    private final FormAToCaseTransformer formAToCaseTransformer;

    @Override
    @PostConstruct
    public void init() {
        bulkScanFormTransformerMap = new HashMap<>();
        bulkScanFormTransformerMap.put(FORM_A, formAToCaseTransformer);
    }
}
