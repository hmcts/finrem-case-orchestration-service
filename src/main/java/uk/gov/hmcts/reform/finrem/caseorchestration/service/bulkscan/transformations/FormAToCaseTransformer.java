package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformations;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataField;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        return Collections.emptyMap();
    }

    @Override
    Map<String, Object> runPostMappingModification(Map<String, Object> transformedCaseData) {
        return transformedCaseData;
    }

    private Optional<String> getValueFromOcrDataFields(String fieldName, List<OcrDataField> ocrDataFields) {
        return ocrDataFields.stream()
            .filter(f -> f.getName().equals(fieldName))
            .map(OcrDataField::getValue)
            .findFirst();
    }

    private static Map<String, String> formAExceptionRecordToCcdMap() {
        Map<String, String> exceptionRecordToCcdFieldsMap = new HashMap<>();

        // Section 2 - Requirement to attend MIAM
        exceptionRecordToCcdFieldsMap.put("claimingExemptionMIAM", "claimingExemptionMIAM");
        exceptionRecordToCcdFieldsMap.put("familyMediatorMIAM", "familyMediatorMIAM");
        exceptionRecordToCcdFieldsMap.put("applicantAttendedMIAM", "applicantAttendedMIAM");

        return exceptionRecordToCcdFieldsMap;
    }
}