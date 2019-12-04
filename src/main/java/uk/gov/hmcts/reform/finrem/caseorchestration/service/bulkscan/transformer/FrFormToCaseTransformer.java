package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformer;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.transformation.in.ExceptionRecord;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.validation.in.OcrDataField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
public class FrFormToCaseTransformer extends BulkScanFormTransformer {

    private static final Map<String, String> ocrToCCDMapping;

    static {
        ocrToCCDMapping = frExceptionRecordToCcdMap();
    }

    @Override
    protected Map<String, String> getOcrToCCDMapping() {
        return ocrToCCDMapping;
    }

    @Override
    Map<String, Object> runFormSpecificTransformation(List<OcrDataField> ocrDataFields) {
        return null;
    }

    @Override
    Map<String, Object> runPostMappingModification(Map<String, Object> ccdTransformedFields) {
        return null;
    }

    private static Map<String, String> frExceptionRecordToCcdMap() {
        Map<String, String> erToCcdFieldsMap = new HashMap<>();

        erToCcdFieldsMap.put("firstName", "D8FirstName");
        erToCcdFieldsMap.put("lastName", "D8LastName");

        return erToCcdFieldsMap;
    }

    @Override
    public Map<String, Object> transformIntoCaseData(ExceptionRecord exceptionRecord) {
        return exceptionRecord.getOcrDataFields().stream()
            .collect(Collectors.toMap(
                ocrDataField -> ocrToCCDMapping.get(ocrDataField.getName()),
                OcrDataField::getValue
            ));
    }
}