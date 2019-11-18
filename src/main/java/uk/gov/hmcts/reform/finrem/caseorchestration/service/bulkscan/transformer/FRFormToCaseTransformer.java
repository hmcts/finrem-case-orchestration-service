package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformer;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.OcrDataField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.transformation.in.ExceptionRecord;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class FRFormToCaseTransformer implements ExceptionRecordToCaseTransformer {

    private static Map<String, String> ocrToCCDMapping;

    public FRFormToCaseTransformer() {
        ocrToCCDMapping = frExceptionRecordToCcdMap();
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