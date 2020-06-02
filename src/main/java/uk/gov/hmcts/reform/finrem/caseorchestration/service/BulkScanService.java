package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.common.error.InvalidDataException;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.InputScannedDoc;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResult;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.ValidationStatus;
import uk.gov.hmcts.reform.bsp.common.service.BulkScanFormValidator;
import uk.gov.hmcts.reform.bsp.common.service.transformation.BulkScanFormTransformer;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.FinRemBulkScanFormTransformerFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.FinRemBulkScanFormValidatorFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;

@AllArgsConstructor
@Service
public class BulkScanService {

    private final FinRemBulkScanFormValidatorFactory finRemBulkScanFormValidatorFactory;

    private final FinRemBulkScanFormTransformerFactory finRemBulkScanFormTransformerFactory;

    public OcrValidationResult validateBulkScanForm(String formType, List<OcrDataField> ocrDataFields) throws UnsupportedFormTypeException {
        BulkScanFormValidator formValidator = finRemBulkScanFormValidatorFactory.getValidator(formType);
        return formValidator.validateBulkScanForm(ocrDataFields);
    }

    public Map<String, Object> transformBulkScanForm(ExceptionRecord exceptionRecord) throws UnsupportedFormTypeException, InvalidDataException {
        validateForTransformation(exceptionRecord);

        BulkScanFormTransformer bulkScanFormTransformer = finRemBulkScanFormTransformerFactory.getTransformer(exceptionRecord.getFormType());
        return bulkScanFormTransformer.transformIntoCaseData(exceptionRecord);
    }

    private void validateForTransformation(ExceptionRecord exceptionRecord) throws UnsupportedFormTypeException, InvalidDataException {

        OcrValidationResult ocrDataFieldsValidationResult
            =  validateBulkScanForm(exceptionRecord.getFormType(), exceptionRecord.getOcrDataFields());

        OcrValidationResult bulkScanFormsValidationResult = validateFormAScannedDocuments(exceptionRecord);

        if (!ocrDataFieldsValidationResult.getStatus().equals(ValidationStatus.SUCCESS)) {
            throw new InvalidDataException(
                String.format("Validation of exception record %s finished with status %s",
                    exceptionRecord.getId(), ocrDataFieldsValidationResult.getStatus()),
                ocrDataFieldsValidationResult.getWarnings(),
                ocrDataFieldsValidationResult.getErrors()
            );
        }

        /*
        if (!bulkScanFormsValidationResult.getStatus().equals(ValidationStatus.SUCCESS)) {
            throw new InvalidDataException(
                String.format("Validation of exception record %s finished with status %s",
                    exceptionRecord.getId(), bulkScanFormsValidationResult.getStatus()),
                bulkScanFormsValidationResult.getWarnings(),
                bulkScanFormsValidationResult.getErrors()
            );
        }

         */
    }

    private OcrValidationResult validateFormAScannedDocuments(ExceptionRecord exceptionRecord) throws UnsupportedFormTypeException {

        List<InputScannedDoc> scannedDocuments = exceptionRecord.getScannedDocuments();
        List<InputScannedDoc> inputScannedDocs = Optional.ofNullable(scannedDocuments).orElse(emptyList());
        OcrValidationResult.Builder validationResultBuilder = OcrValidationResult.builder();

        List<String> validationMessagesForScannedDocuments = produceErrorsForIncorrectNumberOfAttachedDocuments(inputScannedDocs);
        validationMessagesForScannedDocuments.forEach(validationResultBuilder::addWarning);

        List<String> validationMessagesForDocumentFieldsNotFound = produceErrorsForDocumentFieldsNotFound(inputScannedDocs);
        validationMessagesForDocumentFieldsNotFound.forEach(validationResultBuilder::addWarning);

        return validationResultBuilder.build();
    }

    /*
        If a document is received that does not have a sub-type on the expected sub-type list
            Warning rules: "document sub-type not accepted."
     */

    private List<String> produceErrorsForIncorrectNumberOfAttachedDocuments(List<InputScannedDoc> inputScannedDocs) {

        /*
        Validates only a single Form A Document and a single Draft Consent Order should be attached to the Exception Record
        Validates that there is at least 1 D81 document attached to the Exception Record
         */

        List<String> validationErrorMessages = new ArrayList<>();

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
            validationErrorMessages.add("Must be only a single document with subtype of 'FormA'");
        }

        if (numberOfDraftConsentOrderDocumentsAttached != 1) {
            validationErrorMessages.add("Must be only a single document with subtype of 'DraftConsentOrder'");
        }

        if (numberOfD81DocumentsAttached == 0) {
            validationErrorMessages.add("Must be at least one document with subtype of 'D81'");
        }

        return validationErrorMessages;
    }

    private List<String> produceErrorsForDocumentFieldsNotFound(List<InputScannedDoc> inputScannedDocs) {

        /*
        Validates that all documents have the following fields populated:
            Type
            Subtype
            Filename
            document:
                - document_url
                - document_binary_url
         */
        List<String> validationErrorMessages = new ArrayList<>();

        return validationErrorMessages;
    }
}
