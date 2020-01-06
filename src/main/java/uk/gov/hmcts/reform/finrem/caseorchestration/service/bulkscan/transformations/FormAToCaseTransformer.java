package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformations;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.getCommaSeparatedValuesFromOcrDataField;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.maimUrgencyChecklistToCcdFieldNames;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.miamDomesticViolenceChecklistToCcdFieldNames;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.miamExemptionsChecklistToCcdFieldNames;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.miamOtherGroundsChecklistToCcdFieldNames;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.miamPreviousAttendanceChecklistToCcdFieldNames;

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
        Map<String, Object> modifiedMap = new HashMap<>();

        commaSeparatedEntryTransformer("MIAMExemptionsChecklist", miamExemptionsChecklistToCcdFieldNames, ocrDataFields, modifiedMap);
        commaSeparatedEntryTransformer("MIAMDomesticViolenceChecklist", miamDomesticViolenceChecklistToCcdFieldNames, ocrDataFields, modifiedMap);
        commaSeparatedEntryTransformer("MIAMUrgencyChecklist", maimUrgencyChecklistToCcdFieldNames, ocrDataFields, modifiedMap);
        commaSeparatedEntryTransformer("MIAMPreviousAttendanceChecklist", miamPreviousAttendanceChecklistToCcdFieldNames, ocrDataFields, modifiedMap);
        commaSeparatedEntryTransformer("MIAMOtherGroundsChecklist", miamOtherGroundsChecklistToCcdFieldNames, ocrDataFields, modifiedMap);

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

    private void commaSeparatedEntryTransformer(String commaSeparatedEntryKey,
                                                Map<String, String> ocrFieldNamesToCcdFieldNames,
                                                List<OcrDataField> ocrDataFields,
                                                Map<String, Object> modifiedMap) {

        Optional<String> commaSeparatedEntryValue = getValueFromOcrDataFields(commaSeparatedEntryKey, ocrDataFields);

        if (commaSeparatedEntryValue.isPresent()) {
            List<String> transformedCommaSeparatedValue =
                getCommaSeparatedValuesFromOcrDataField(commaSeparatedEntryValue.get())
                    .stream()
                    .map(ocrFieldNamesToCcdFieldNames::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!transformedCommaSeparatedValue.isEmpty()) {
                modifiedMap.put(commaSeparatedEntryKey, transformedCommaSeparatedValue);
            }
        }
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
