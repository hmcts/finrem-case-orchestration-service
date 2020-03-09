package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.dischargePeriodicalPaymentSubstituteChecklistToCcdFieldNames;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.getCommaSeparatedValuesFromOcrDataField;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.natureOfApplicationChecklistToCcdFieldNames;


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
        Map<String, Object> formSpecificMap = new HashMap<>();
        
        mapFullNameToFirstAndLast(OcrFieldName.APPLICANT_FULL_NAME, "applicantFMName", "applicantLName",
            ocrDataFields, formSpecificMap);
        mapFullNameToFirstAndLast(OcrFieldName.RESPONDENT_FULL_NAME, "appRespondentFMname", "appRespondentLName",
            ocrDataFields, formSpecificMap);

        commaSeparatedEntryTransformer(OcrFieldName.NATURE_OF_APPLICATION, natureOfApplicationChecklistToCcdFieldNames,
            ocrDataFields, formSpecificMap);
        commaSeparatedEntryTransformer(OcrFieldName.DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE,
            dischargePeriodicalPaymentSubstituteChecklistToCcdFieldNames, ocrDataFields, formSpecificMap);

        return formSpecificMap;
    }

    private void mapFullNameToFirstAndLast(String ocrFieldName, String ccdFirstNameFieldName, String ccdLastNameFieldName,
                                           List<OcrDataField> ocrDataFields, Map<String, Object> formSpecificMap) {
        ocrDataFields.stream()
            .filter(ocrDataField -> ocrDataField.getName().equals(ocrFieldName))
            .map(OcrDataField::getValue)
            .findFirst()
            .ifPresent(fullName -> {
                List<String> nameElements = asList(fullName.split(" "));
                formSpecificMap.put(ccdFirstNameFieldName, String.join(" ", nameElements.subList(0, nameElements.size() - 1)));
                formSpecificMap.put(ccdLastNameFieldName, nameElements.get(nameElements.size() - 1));
            });
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

        // Section 0 - nature of application
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.DIVORCE_CASE_NUMBER, CCDConfigConstant.DIVORCE_CASE_NUMBER);
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.HWF_NUMBER, "HWFNumber");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.PROVISION_MADE_FOR, "provisionMadeFor");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_INTENDS_TO, "applicantIntendsTo");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLYING_FOR_CONSENT_ORDER, "applyingForConsentOrder");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.DIVORCE_STAGE_REACHED, "divorceStageReached");

        return exceptionRecordToCcdFieldsMap;
    }
}
