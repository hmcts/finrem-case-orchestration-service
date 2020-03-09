package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;

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
        Map<String, Object> transformedCaseData = new HashMap<>();

        applyMappingsForAddress("applicantSolicitor", "solicitorAddress", ocrDataFields, transformedCaseData);
        applyMappingsForAddress("applicant", ocrDataFields, transformedCaseData);
        applyMappingsForAddress("respondent", ocrDataFields, transformedCaseData);
        applyMappingsForAddress("respondentSolicitor", "rSolicitorAddress", ocrDataFields, transformedCaseData);

        return transformedCaseData;
    }

    private static Map<String, String> formAExceptionRecordToCcdMap() {
        Map<String, String> erToCcdFieldsMap = new HashMap<>();

        erToCcdFieldsMap.put("ApplicantRepresented", "applicantRepresentPaper");
        erToCcdFieldsMap.put("ApplicantSolicitorName", "solicitorName");
        erToCcdFieldsMap.put("ApplicantSolicitorFirm", "solicitorFirm");
        erToCcdFieldsMap.put("ApplicantSolicitorPhone", "solicitorPhone");
        erToCcdFieldsMap.put("ApplicantSolicitorDXnumber", "solicitorDXnumber");
        erToCcdFieldsMap.put("ApplicantSolicitorReference", "solicitorReference");
        erToCcdFieldsMap.put("ApplicantPBANumber", "PBANumber");
        erToCcdFieldsMap.put("ApplicantSolicitorEmail", "solicitorEmail");
        erToCcdFieldsMap.put("ApplicantPhone", "applicantPhone");
        erToCcdFieldsMap.put("ApplicantEmail", "applicantEmail");

        return erToCcdFieldsMap;
    }

    private void applyMappingsForAddress(
            String prefix, String unusualField, List<OcrDataField> ocrDataFields, Map<String, Object> modifiedMap) {
        String nestedFieldPrefix =  capitalize(prefix + "Address");
        addMappingsTo(
            unusualField,
            ImmutableMap.of(
                nestedFieldPrefix + "Line1", "AddressLine1",
                nestedFieldPrefix + "County", "County",
                nestedFieldPrefix + "Postcode", "PostCode",
                nestedFieldPrefix + "Town", "PostTown",
                nestedFieldPrefix + "Country", "Country"
            ),
            modifiedMap,
            ocrDataFields
        );
    }

    private void applyMappingsForAddress(
            String prefix, List<OcrDataField> ocrDataFields, Map<String, Object> modifiedMap) {
        applyMappingsForAddress(prefix, prefix + "Address", ocrDataFields, modifiedMap);
    }

    private String capitalize(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private void addMappingsTo(String parentField, ImmutableMap<String, String> mappings,
                               Map<String, Object> modifiedMap, List<OcrDataField> ocrDataFields) {
        HashMap<String, Object> parentFieldObject = new HashMap<>();

        mappings.forEach((srcField, targetField) -> {
            mapIfSourceExists(srcField, targetField, parentFieldObject, ocrDataFields);
        });

        if (parentFieldObject.size() > 0) {
            modifiedMap.put(parentField, parentFieldObject);
        }
    }

    private void mapIfSourceExists(String srcField, String targetField, HashMap<String, Object> parentObject,
                                   List<OcrDataField> ocrDataFields) {
        getValueFromOcrDataFields(srcField, ocrDataFields)
            .ifPresent(srcFieldValue -> {
                parentObject.put(targetField, srcFieldValue);
            });
    }

    private Optional<String> getValueFromOcrDataFields(String fieldName, List<OcrDataField> ocrDataFields) {
        return ocrDataFields.stream()
            .filter(f -> f.getName().equals(fieldName))
            .map(OcrDataField::getValue)
            .findFirst();
    }
}
