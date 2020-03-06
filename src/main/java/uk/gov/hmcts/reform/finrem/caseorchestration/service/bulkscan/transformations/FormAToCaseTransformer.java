package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformations;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FormAToCaseTransformer extends BulkScanFormTransformer {

    private static final Map<String, String> ocrToCCDMapping;

    static {
        ocrToCCDMapping = formAExceptionRecordToCcdMap();
    }

    @Override
    protected Map<String, String> getOcrToCCDMapping() {
        return ocrToCCDMapping;
    }

    private static Map<String, String> formAExceptionRecordToCcdMap() {
        return new HashMap<>();
    }
}
