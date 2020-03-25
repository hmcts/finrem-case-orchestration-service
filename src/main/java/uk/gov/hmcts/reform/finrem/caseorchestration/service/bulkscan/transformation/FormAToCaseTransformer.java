package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.mapper.AddressMapper;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.service.transformation.BulkScanFormTransformer;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChildInfo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChildrenList;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.bsp.common.mapper.AddressMapper.applyMappings;
import static uk.gov.hmcts.reform.bsp.common.mapper.GenericMapper.getValueFromOcrDataFields;
import static uk.gov.hmcts.reform.bsp.common.utils.BulkScanCommonHelper.getCommaSeparatedValuesFromOcrDataField;
import static uk.gov.hmcts.reform.bsp.common.utils.BulkScanCommonHelper.transformFormDateIntoCcdDate;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.applicantRepresentPaperToCcdFieldNames;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.dischargePeriodicalPaymentSubstituteChecklistToCcdFieldNames;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.natureOfApplicationChecklistToCcdFieldNames;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.orderForChildrenNoAgreementToCcdFieldNames;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.orderForChildrenToCcdFieldNames;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.provisionMadeForToCcdFieldNames;

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

        AddressMapper.applyMappings("applicantSolicitor", "solicitorAddress", ocrDataFields, transformedCaseData);
        applyMappingsForAddress("applicant", ocrDataFields, transformedCaseData);
        applyMappingsForAddress("respondent", ocrDataFields, transformedCaseData);
        AddressMapper.applyMappings("respondentSolicitor", "rSolicitorAddress", ocrDataFields, transformedCaseData);

        addMappingsToChildren(ocrDataFields, transformedCaseData);

        mapAuthorisationSignedToYesOrNo(OcrFieldName.AUTHORISATION_SIGNED, "authorisationSigned", ocrDataFields, transformedCaseData);

        mapFormDateToCcdDate(OcrFieldName.AUTHORISATION_DATE, "authorisation3", ocrDataFields, transformedCaseData);

        commaSeparatedEntryTransformer(OcrFieldName.ORDER_FOR_CHILDREN_NO_AGREEMENT, "natureOfApplication6",
                orderForChildrenNoAgreementToCcdFieldNames, ocrDataFields, transformedCaseData);

        getValueFromOcrDataFields(OcrFieldName.ORDER_FOR_CHILDREN, ocrDataFields)
                .map(orderForChildrenToCcdFieldNames::get)
                .filter(Objects::nonNull)
                .ifPresent(value -> transformedCaseData.put("natureOfApplication5b", value));

        getValueFromOcrDataFields(OcrFieldName.PROVISION_MADE_FOR, ocrDataFields)
                .map(provisionMadeForToCcdFieldNames::get)
                .filter(Objects::nonNull)
                .ifPresent(value -> transformedCaseData.put("provisionMadeFor", value));

        getValueFromOcrDataFields(OcrFieldName.APPLICANT_REPRESENTED, ocrDataFields)
                .map(applicantRepresentPaperToCcdFieldNames::get)
                .filter(Objects::nonNull)
                .ifPresent(value -> transformedCaseData.put("applicantRepresentPaper", value));

        return transformedCaseData;
    }

    @Override
    protected Map<String, Object> runPostMappingModification(final Map<String, Object> transformedCaseData) {
        Map<String, Object> modifiedCaseData = new HashMap<>(transformedCaseData);

        modifiedCaseData.put("paperApplication", YES_VALUE);

        // If OrderForChildren is populated then set orderForChildrenQuestion1 to Yes
        if (StringUtils.isNotEmpty((String) modifiedCaseData.get("natureOfApplication5b"))) {
            modifiedCaseData.put("orderForChildrenQuestion1", YES_VALUE);
        }

        return modifiedCaseData;
    }

    private void mapFormDateToCcdDate(String ocrFieldName, String ccdFieldName,
                                      List<OcrDataField> ocrDataFields, Map<String, Object> formSpecificMap) {
        getValueFromOcrDataFields(ocrFieldName, ocrDataFields).ifPresent(ocrAuthorisationDate -> {
            String ccdAuthorisationDate = transformFormDateIntoCcdDate(OcrFieldName.AUTHORISATION_DATE, ocrAuthorisationDate);
            formSpecificMap.put(ccdFieldName, ccdAuthorisationDate);
        });
    }

    private void mapAuthorisationSignedToYesOrNo(String ocrFieldName, String ccdFieldName,
                                                 List<OcrDataField> ocrDataFields, Map<String, Object> formSpecificMap) {
        ocrDataFields.stream()
                .filter(ocrDataField -> ocrDataField.getName().equals(ocrFieldName))
                .map(OcrDataField::getValue)
                .findFirst()
                .ifPresent(ocrValue -> {
                    String ccdValue = ocrValue.trim().isEmpty() ? NO_VALUE : YES_VALUE;
                    formSpecificMap.put(ccdFieldName, ccdValue);
                });
    }

    private void mapFullNameToFirstAndLast(String ocrFieldName, String ccdFirstNameFieldName, String ccdLastNameFieldName,
                                           List<OcrDataField> ocrDataFields, Map<String, Object> formSpecificMap) {

        getValueFromOcrDataFields(ocrFieldName, ocrDataFields)
                .ifPresent(fullName -> {
                    List<String> nameElements = asList(fullName.split(" "));
                    formSpecificMap.put(ccdFirstNameFieldName, String.join(" ", nameElements.subList(0, nameElements.size() - 1)));
                    formSpecificMap.put(ccdLastNameFieldName, nameElements.get(nameElements.size() - 1));
                });
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
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_INTENDS_TO, "applicantIntendsTo");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLYING_FOR_CONSENT_ORDER, "applyingForConsentOrder");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.DIVORCE_STAGE_REACHED, "divorceStageReached");

        // Section 1 - further details of application
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.ADDRESS_OF_PROPERTIES, "natureOfApplication3a");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.MORTGAGE_DETAILS, "natureOfApplication3b");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.ORDER_FOR_CHILDREN, "natureOfApplication5b");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.CHILD_SUPPORT_AGENCY_CALCULATION_MADE, "ChildSupportAgencyCalculationMade");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.CHILD_SUPPORT_AGENCY_CALCULATION_REASON, "ChildSupportAgencyCalculationReason");

        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_SOLICITOR_NAME, "solicitorName");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_SOLICITOR_FIRM, "solicitorFirm");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_SOLICITOR_PHONE, "solicitorPhone");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_SOLICITOR_DX_NUMBER, "solicitorDXnumber");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_SOLICITOR_REFERENCE, "solicitorReference");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_PBA_NUMBER, "PBANumber");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_SOLICITOR_EMAIL, "solicitorEmail");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_PHONE, "applicantPhone");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_EMAIL, "applicantEmail");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.AUTHORISATION_NAME, "authorisationName");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.AUTHORISATION_FIRM, "authorisationFirm");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.AUTHORISATION_SOLICITOR_ADDRESS, "authorisationSolicitorAddress");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.AUTHORISATION_SIGNED_BY, "authorisationSignedBy");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.AUTHORISATION_SOLICITOR_POSITION, "authorisation2b");

        return exceptionRecordToCcdFieldsMap;
    }

    private void applyMappingsForAddress(
            String prefix, List<OcrDataField> ocrDataFields, Map<String, Object> modifiedMap) {
        applyMappings(prefix, prefix + "Address", ocrDataFields, modifiedMap);
    }

    private void addMappingsToChildren(List<OcrDataField> ocrDataFields, Map<String, Object> modifiedMap) {
        ChildrenList children = new ChildrenList();
        int i = 1;
        while (isChildInfoPopulated(i, ocrDataFields)) {
            children.addChild(mapChild(i, ocrDataFields));
            i++;
        }

        if (children.size() > 0) {
            modifiedMap.put("childrenInfo", children);
        }
    }

    private boolean isChildInfoPopulated(int i, List<OcrDataField> ocrDataFields) {
        return ocrDataFields.stream().anyMatch(item -> item.getName().equalsIgnoreCase("NameOfChild" + i));
    }

    private ChildInfo mapChild(int index, List<OcrDataField> ocrDataFields) {
        String dob = transformFormDateIntoCcdDate(
                "childInfo" + index + ".dateOfBirth",
                getValueOrEmptyString(index, ocrDataFields, "DateOfBirthChild")
        );

        ChildInfo child = ChildInfo.builder()
                .name(getValueOrEmptyString(index, ocrDataFields, "NameOfChild"))
                .dateOfBirth(dob)
                .gender(getValueFromOcrDataFields("GenderChild" + index, ocrDataFields).orElse("notGiven"))
                .relationshipToApplicant(getValueOrEmptyString(index, ocrDataFields, "RelationshipToApplicantChild"))
                .relationshipToRespondent(getValueOrEmptyString(index, ocrDataFields, "RelationshipToRespondentChild"))
                .countryOfResidence(getValueOrEmptyString(index, ocrDataFields, "CountryOfResidenceChild"))
                .build();

        return child;
    }

    private String getValueOrEmptyString(int index, List<OcrDataField> ocrDataFields, String fieldPrefix) {
        return getValueFromOcrDataFields(fieldPrefix + index, ocrDataFields).orElse(StringUtils.EMPTY);
    }
}
