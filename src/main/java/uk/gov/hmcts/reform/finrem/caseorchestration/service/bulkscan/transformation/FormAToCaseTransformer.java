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

        commaSeparatedEntryTransformer(OcrFieldName.NATURE_OF_APPLICATION, "natureOfApplication2", natureOfApplicationChecklistToCcdFieldNames,
            ocrDataFields, transformedCaseData);
        commaSeparatedEntryTransformer(OcrFieldName.DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE, "dischargePeriodicalPaymentSubstituteFor",
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

    private void commaSeparatedEntryTransformer(String ocrNameWithCommaSeparatedValues,
                                                String ccdName,
                                                Map<String, String> ocrValuesToCcdValues,
                                                List<OcrDataField> ocrDataFields,
                                                Map<String, Object> transformedCaseData) {

        Optional<String> commaSeparatedOcrValue = getValueFromOcrDataFields(ocrNameWithCommaSeparatedValues, ocrDataFields);

        if (commaSeparatedOcrValue.isPresent()) {
            List<String> transformedCommaSeparatedValue =
                getCommaSeparatedValuesFromOcrDataField(commaSeparatedOcrValue.get())
                    .stream()
                    .map(ocrValuesToCcdValues::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!transformedCommaSeparatedValue.isEmpty()) {
                transformedCaseData.put(ccdName, transformedCommaSeparatedValue);
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

        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_REPRESENTED, "applicantRepresentPaper");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_SOLICITOR_NAME, "solicitorName");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_SOLICITOR_FIRM, "solicitorFirm");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_SOLICITOR_PHONE, "solicitorPhone");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_SOLICITOR_DX_NUMBER, "solicitorDXnumber");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_SOLICITOR_REFERENCE, "solicitorReference");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_PBA_NUMBER, "PBANumber");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_SOLICITOR_EMAIL, "solicitorEmail");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_PHONE, "applicantPhone");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_EMAIL, "applicantEmail");
        return exceptionRecordToCcdFieldsMap;
    }

    private void applyMappingsForAddress(String prefix, String parentField, List<OcrDataField> ocrDataFields, Map<String, Object> modifiedMap) {
        String nestedFieldPrefix = StringUtils.capitalize(prefix + "Address");
        addMappingsTo(
            parentField,
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

        transformedCaseData.replace("natureOfApplication6",
            "for a stepchild or stepchildren",
            "Step Child or Step Children");

        transformedCaseData.replace("natureOfApplication6",
            "in addition to child support maintenance already paid under a Child Support Agency assessment",
            "In addition to child support");

        transformedCaseData.replace("natureOfApplication6",
            "to meet expenses arising from a child’s disability", "disability expenses");

        transformedCaseData.replace("natureOfApplication6",
            "when either the child or the person with care of the child or "
                + "the absent parent of the child is not habitually resident in the United Kingdom",
            "When not habitually resident");

        return transformedCaseData;
    }
}
