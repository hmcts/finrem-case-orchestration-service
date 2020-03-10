package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
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
        Map<String, Object> transformedCaseData = new HashMap<>();

        mapFullNameToFirstAndLast(OcrFieldName.APPLICANT_FULL_NAME, "applicantFMName", "applicantLName",
            ocrDataFields, transformedCaseData);
        mapFullNameToFirstAndLast(OcrFieldName.RESPONDENT_FULL_NAME, "appRespondentFMname", "appRespondentLName",
            ocrDataFields, transformedCaseData);

        commaSeparatedEntryTransformer(OcrFieldName.NATURE_OF_APPLICATION, natureOfApplicationChecklistToCcdFieldNames,
            ocrDataFields, transformedCaseData);
        commaSeparatedEntryTransformer(OcrFieldName.DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE,
            dischargePeriodicalPaymentSubstituteChecklistToCcdFieldNames, ocrDataFields, transformedCaseData);

        applyMappingsForAddress("applicantSolicitor", "solicitorAddress", ocrDataFields, transformedCaseData);
        applyMappingsForAddress("applicant", ocrDataFields, transformedCaseData);
        applyMappingsForAddress("respondent", ocrDataFields, transformedCaseData);
        applyMappingsForAddress("respondentSolicitor", "rSolicitorAddress", ocrDataFields, transformedCaseData);

        return transformedCaseData;
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

        // Section 1 - further details of application
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.ADDRESS_OF_PROPERTIES, "natureOfApplication3a");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.MORTGAGE_DETAILS, "natureOfApplication3b");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.ORDER_FOR_CHILDREN, "natureOfApplication5b");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.ORDER_FOR_CHILDREN_NO_AGREEMENT, "natureOfApplication6");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.CHILD_SUPPORT_AGENCY_CALCULATION_MADE, "ChildSupportAgencyCalculationMade");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.CHILD_SUPPORT_AGENCY_CALCULATION_REASON, "ChildSupportAgencyCalculationReason");

        exceptionRecordToCcdFieldsMap.put("ApplicantRepresented", "applicantRepresentPaper");
        exceptionRecordToCcdFieldsMap.put("ApplicantSolicitorName", "solicitorName");
        exceptionRecordToCcdFieldsMap.put("ApplicantSolicitorFirm", "solicitorFirm");
        exceptionRecordToCcdFieldsMap.put("ApplicantSolicitorPhone", "solicitorPhone");
        exceptionRecordToCcdFieldsMap.put("ApplicantSolicitorDXnumber", "solicitorDXnumber");
        exceptionRecordToCcdFieldsMap.put("ApplicantSolicitorReference", "solicitorReference");
        exceptionRecordToCcdFieldsMap.put("ApplicantPBANumber", "PBANumber");
        exceptionRecordToCcdFieldsMap.put("ApplicantSolicitorEmail", "solicitorEmail");
        exceptionRecordToCcdFieldsMap.put("ApplicantPhone", "applicantPhone");
        exceptionRecordToCcdFieldsMap.put("ApplicantEmail", "applicantEmail");

        return exceptionRecordToCcdFieldsMap;
    }

    private void applyMappingsForAddress(String prefix, String unusualField, List<OcrDataField> ocrDataFields, Map<String, Object> modifiedMap) {
        String nestedFieldPrefix = StringUtils.capitalize(prefix + "Address");
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

    private void mapIfSourceExists(String srcField, String targetField, HashMap<String, Object> parentObject, List<OcrDataField> ocrDataFields) {
        getValueFromOcrDataFields(srcField, ocrDataFields)
            .ifPresent(srcFieldValue -> {
                parentObject.put(targetField, srcFieldValue);
            });
    }

    @Override
    protected Map<String, Object> runPostMappingModification(Map<String, Object> transformedCaseData) {

        // If OrderForChildren is populated then set orderForChildrenQuestion1 to Yes
        if (StringUtils.isNotEmpty((String) transformedCaseData.get("natureOfApplication5b"))) {

            transformedCaseData.put("orderForChildrenQuestion1", YES_VALUE);
        }

        return transformedCaseData;
    }
}
