package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformations;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class FormAToCaseTransformer extends BulkScanFormTransformer {

    private static final Map<String, String> ocrToCCDMapping;

    static {
        ocrToCCDMapping = d8ExceptionRecordToCcdMap();
    }

    @Override
    protected Map<String, String> getOcrToCCDMapping() {
        return ocrToCCDMapping;
    }

    @Override
    protected Map<String, Object> runFormSpecificTransformation(List<OcrDataField> ocrDataFields) {
        Map<String, Object> modifiedMap = new HashMap<>();

        return modifiedMap;
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

    private static Map<String, String> d8ExceptionRecordToCcdMap() {
        Map<String, String> erToCcdFieldsMap = new HashMap<>();

        // Section 2 - About you (the applicant/petitioner)
        erToCcdFieldsMap.put("PetitionerFirstName", "D8PetitionerFirstName");
        erToCcdFieldsMap.put("PetitionerLastName", "D8PetitionerLastName");

        return erToCcdFieldsMap;
    }
}