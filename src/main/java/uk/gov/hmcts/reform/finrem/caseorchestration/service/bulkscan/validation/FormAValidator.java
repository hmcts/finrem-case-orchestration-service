package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.InputScannedDoc;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResult;
import uk.gov.hmcts.reform.bsp.common.service.BulkScanFormValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.bsp.common.model.validation.BulkScanValidationPatterns.CCD_EMAIL_REGEX;
import static uk.gov.hmcts.reform.bsp.common.model.validation.BulkScanValidationPatterns.CCD_PHONE_NUMBER_REGEX;
import static uk.gov.hmcts.reform.bsp.common.service.validation.PostcodeValidator.validatePostcode;
import static uk.gov.hmcts.reform.bsp.common.service.validation.RegExpValidator.validateField;
import static uk.gov.hmcts.reform.bsp.common.utils.BulkScanCommonHelper.getCommaSeparatedValuesFromOcrDataField;
import static uk.gov.hmcts.reform.bsp.common.utils.BulkScanCommonHelper.validateFormDate;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.ALLOWED_DOCUMENT_SUBTYPES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_ADDRESS_POSTCODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_FULL_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_INTENDS_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLYING_FOR_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.AUTHORISATION_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.AUTHORISATION_SIGNED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.AUTHORISATION_SIGNED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.CHILD_SUPPORT_AGENCY_CALCULATION_MADE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.DATE_OF_BIRTH_CHILD_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.DATE_OF_BIRTH_CHILD_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.DIVORCE_STAGE_REACHED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.GENDER_CHILD_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.GENDER_CHILD_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.HWF_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.NATURE_OF_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.ORDER_FOR_CHILDREN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.ORDER_FOR_CHILDREN_NO_AGREEMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.PROVISION_MADE_FOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.RESPONDENT_ADDRESS_POSTCODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.dischargePeriodicalPaymentSubstituteChecklistToCcdFieldNames;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.natureOfApplicationChecklistToCcdFieldNames;

@Component
public class FormAValidator extends BulkScanFormValidator {

    private static final String HWF_NUMBER_6_DIGITS_REGEX = "\\d{6}";
    private static final String DIVORCE_CASE_NUMBER_REGEX = "^([A-Z|a-z][A-Z|a-z])\\d{2}[D|d]\\d{5}$";

    private static final List<String> MANDATORY_FIELDS = asList(
            DIVORCE_CASE_NUMBER,
            APPLICANT_FULL_NAME,
            RESPONDENT_FULL_NAME,
            PROVISION_MADE_FOR,
            NATURE_OF_APPLICATION,
            APPLICANT_INTENDS_TO,
            APPLYING_FOR_CONSENT_ORDER,
            DIVORCE_STAGE_REACHED,
            APPLICANT_REPRESENTED,
            AUTHORISATION_SIGNED,
            AUTHORISATION_SIGNED_BY,
            AUTHORISATION_DATE
    );

    protected List<String> getMandatoryFields() {
        return MANDATORY_FIELDS;
    }

    private static final Map<String, List<String>> ALLOWED_VALUES_PER_FIELD = new HashMap<>();

    static {
        ALLOWED_VALUES_PER_FIELD.put(PROVISION_MADE_FOR, asList(
                "in connection with matrimonial or civil partnership proceedings",
                "under paragraphs 1 or 2 of Schedule 1 to the Children Act 1989"
        ));
        ALLOWED_VALUES_PER_FIELD.put(APPLICANT_INTENDS_TO, asList(
                "ApplyToCourtFor",
                "ProceedWithApplication",
                "ApplyToVary",
                "ApplyToDischargePeriodicalPaymentOrder"
        ));
        ALLOWED_VALUES_PER_FIELD.put(APPLYING_FOR_CONSENT_ORDER, asList("Yes"));
        ALLOWED_VALUES_PER_FIELD.put(DIVORCE_STAGE_REACHED, asList("Decree Nisi", "Decree Absolute", "Petition Issued"));
        ALLOWED_VALUES_PER_FIELD.put(APPLICANT_REPRESENTED, asList(
                "I am not represented by a solicitor in these proceedings",
                "I am not represented by a solicitor in these proceedings but am receiving advice from a solicitor",
                "I am represented by a solicitor in these proceedings, who has signed Section 5"
                        + " and all documents for my attention should be sent to my solicitor whose details are as follows")
        );

        ALLOWED_VALUES_PER_FIELD.put(ORDER_FOR_CHILDREN, asList(
                "there is a written agreement made before 5 April 1993 about maintenance for the benefit of children",
                "there is a written agreement made on or after 5 April 1993 about maintenance for the benefit of children",
                "there is no agreement, but the applicant is applying for payments"
        ));
        ALLOWED_VALUES_PER_FIELD.put(ORDER_FOR_CHILDREN_NO_AGREEMENT, asList(
                "for a stepchild or stepchildren",
                "in addition to child support maintenance already paid under a Child Support Agency assessment",
                "to meet expenses arising from a child’s disability",
                "to meet expenses incurred by a child in being educated or training for work",
                "when either the child or the person with care of the child or"
                        + " the absent parent of the child is not habitually resident in the United Kingdom"
        ));
        ALLOWED_VALUES_PER_FIELD.put(CHILD_SUPPORT_AGENCY_CALCULATION_MADE, asList(
                "Yes",
                "No",
                EMPTY
        ));
        ALLOWED_VALUES_PER_FIELD.put(AUTHORISATION_SIGNED_BY, asList(
                "Applicant",
                "Litigation Friend",
                "Applicant's solicitor"
        ));
        final List<String> genderEnum = asList("male", "female", "notGiven");
        ALLOWED_VALUES_PER_FIELD.put(GENDER_CHILD_1, genderEnum);
        ALLOWED_VALUES_PER_FIELD.put(GENDER_CHILD_2, genderEnum);
    }

    @Override
    protected Map<String, List<String>> getAllowedValuesPerField() {
        return ALLOWED_VALUES_PER_FIELD;
    }

    @Override
    protected List<String> runPostProcessingValidation(Map<String, String> fieldsMap) {
        List<String> errorMessages = Stream.of(
                validateHwfNumber(fieldsMap, HWF_NUMBER),
                validateHasAtLeastTwoNames(fieldsMap, APPLICANT_FULL_NAME),
                validateHasAtLeastTwoNames(fieldsMap, RESPONDENT_FULL_NAME),
                validateNonMandatoryCommaSeparatedField(fieldsMap,
                        NATURE_OF_APPLICATION, natureOfApplicationChecklistToCcdFieldNames),
                validateNonMandatoryCommaSeparatedField(
                        fieldsMap,
                        DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE,
                        dischargePeriodicalPaymentSubstituteChecklistToCcdFieldNames
                ),
                validateField(fieldsMap, APPLICANT_PHONE, CCD_PHONE_NUMBER_REGEX),
                validateField(fieldsMap, APPLICANT_EMAIL, CCD_EMAIL_REGEX),
                validatePostcode(fieldsMap, APPLICANT_ADDRESS_POSTCODE),
                validatePostcode(fieldsMap, RESPONDENT_ADDRESS_POSTCODE),
                validateField(fieldsMap, DIVORCE_CASE_NUMBER, DIVORCE_CASE_NUMBER_REGEX)
        )
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        validateFormDate(fieldsMap, AUTHORISATION_DATE).ifPresent(errorMessages::add);
        validateFormDate(fieldsMap, DATE_OF_BIRTH_CHILD_1).ifPresent(errorMessages::add);
        validateFormDate(fieldsMap, DATE_OF_BIRTH_CHILD_2).ifPresent(errorMessages::add);

        return errorMessages;
    }

    private static List<String> validateNonMandatoryCommaSeparatedField(
            Map<String, String> fieldsMap, String commaSeparatedFieldKey,
            Map<String, String> validOcrFieldNamesToCcdFieldNames) {
        List<String> validationWarningMessages = new ArrayList<>();

        String commaSeparatedFieldValue = fieldsMap.getOrDefault(commaSeparatedFieldKey, "");

        if (StringUtils.isEmpty(commaSeparatedFieldValue)) {
            return validationWarningMessages;
        }

        boolean allOcrFieldsCanBeMapped = getCommaSeparatedValuesFromOcrDataField(commaSeparatedFieldValue)
                .stream()
                .map(validOcrFieldNamesToCcdFieldNames::containsKey)
                .reduce(Boolean::logicalAnd)
                .orElse(false);

        if (!allOcrFieldsCanBeMapped) {
            validationWarningMessages.add(
                    String.format("%s contains a value that is not accepted", commaSeparatedFieldKey)
            );
        }

        return validationWarningMessages;
    }

    private static List<String> validateHasAtLeastTwoNames(Map<String, String> fieldsMap, String fieldName) {
        String fieldValue = fieldsMap.get(fieldName);
        return fieldValue != null && Arrays.stream(fieldValue.split(" "))
                .filter(StringUtils::isNotBlank)
                .count() < 2
                ? asList(String.format("%s must contain a firstname and a lastname", fieldName))
                : emptyList();
    }

    private static List<String> validateHwfNumber(Map<String, String> fieldsMap, String fieldName) {
        String hwfNumber = fieldsMap.get(fieldName);
        return hwfNumber != null && !hwfNumber.matches(HWF_NUMBER_6_DIGITS_REGEX)
                ? Collections.singletonList("HWFNumber is usually 6 digits")
                : emptyList();
    }

    public OcrValidationResult validateFormAScannedDocuments(ExceptionRecord exceptionRecord) throws UnsupportedFormTypeException {

        List<InputScannedDoc> scannedDocuments = exceptionRecord.getScannedDocuments();
        List<InputScannedDoc> inputScannedDocs = Optional.ofNullable(scannedDocuments).orElse(emptyList());
        OcrValidationResult.Builder documentValidationResultBuilder = OcrValidationResult.builder();

        List<String> validationMessagesForScannedDocuments = produceErrorsForIncorrectNumberOfAttachedDocuments(inputScannedDocs);
        validationMessagesForScannedDocuments.forEach(documentValidationResultBuilder::addWarning);

        List<String> validationMessagesForDocumentSubTypeNotAccepted = produceErrorsForDocumentSubTypeNotAccepted(inputScannedDocs);
        validationMessagesForDocumentSubTypeNotAccepted.forEach(documentValidationResultBuilder::addWarning);

        return documentValidationResultBuilder.build();
    }

    private List<String> produceErrorsForIncorrectNumberOfAttachedDocuments(List<InputScannedDoc> inputScannedDocs) {

        /*
        Validates that only one 'Form A' and one 'Draft Consent Order' are attached to the Exception Record
        Validates that there is at least one 'D81' attached to the Exception Record
         */

        List<String> attachedDocumentsValidationErrorMessages = new ArrayList<>();

        long numberOfFormADocumentsAttached = inputScannedDocs.stream()
            .filter(doc -> doc.getSubtype().equals("FormA"))
            .count();

        long numberOfDraftConsentOrderDocumentsAttached = inputScannedDocs.stream()
            .filter(doc -> doc.getSubtype().equals("DraftConsentOrder"))
            .count();

        long numberOfD81DocumentsAttached = inputScannedDocs.stream()
            .filter(doc -> doc.getSubtype().equals("D81"))
            .count();

        if (numberOfFormADocumentsAttached != 1) {
            attachedDocumentsValidationErrorMessages.add("Must be only a single document with subtype of 'FormA'");
        }

        if (numberOfDraftConsentOrderDocumentsAttached != 1) {
            attachedDocumentsValidationErrorMessages.add("Must be only a single document with subtype of 'DraftConsentOrder'");
        }

        if (numberOfD81DocumentsAttached == 0) {
            attachedDocumentsValidationErrorMessages.add("Must be at least one document with subtype of 'D81'");
        }

        return attachedDocumentsValidationErrorMessages;
    }

    private List<String> produceErrorsForDocumentSubTypeNotAccepted(List<InputScannedDoc> inputScannedDocs) {

        /*
        Validate if a document is received that does not have a sub-type on the expected sub-type list
        */

        List<String> incomingDocSubTypes =
            inputScannedDocs.stream()
                .map(InputScannedDoc::getSubtype)
                .collect(Collectors.toList());

        return incomingDocSubTypes.stream()
            .filter(docSubType -> !ALLOWED_DOCUMENT_SUBTYPES.contains(docSubType))
            .map(docSubType -> String.format("Document sub-type not accepted: \"%s\"", docSubType))
            .collect(Collectors.toList());
    }
}
