package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformations;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataField;

import java.util.HashMap;
import java.util.List;
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

    @Override
    protected Map<String, Object> runFormSpecificTransformation(List<OcrDataField> ocrDataFields) {
        return new HashMap<>();
    }

    @Override
    Map<String, Object> runPostMappingModification(Map<String, Object> transformedCaseData) {
        return transformedCaseData;
    }

    private static Map<String, String> formAExceptionRecordToCcdMap() {
        return new HashMap<>();
    }
}
