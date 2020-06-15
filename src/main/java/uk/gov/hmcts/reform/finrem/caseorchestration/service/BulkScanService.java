package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.common.error.InvalidDataException;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResult;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.ValidationStatus;
import uk.gov.hmcts.reform.bsp.common.service.BulkScanFormValidator;
import uk.gov.hmcts.reform.bsp.common.service.transformation.BulkScanFormTransformer;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.FinRemBulkScanFormTransformerFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.FinRemBulkScanFormValidatorFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.FormAValidator;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
public class BulkScanService {

    private final FinRemBulkScanFormValidatorFactory finRemBulkScanFormValidatorFactory;

    private final FinRemBulkScanFormTransformerFactory finRemBulkScanFormTransformerFactory;

    private final  FormAValidator formAValidator;

    public OcrValidationResult validateBulkScanForm(String formType, List<OcrDataField> ocrDataFields) throws UnsupportedFormTypeException {
        BulkScanFormValidator formValidator = finRemBulkScanFormValidatorFactory.getValidator(formType);
        return formValidator.validateBulkScanForm(ocrDataFields);
    }

    public Map<String, Object> transformBulkScanForm(ExceptionRecord exceptionRecord) throws UnsupportedFormTypeException, InvalidDataException {
        validateForTransformation(exceptionRecord);

        log.info("Exception Record OCR Data fields: {}", exceptionRecord.getOcrDataFields());

        BulkScanFormTransformer bulkScanFormTransformer = finRemBulkScanFormTransformerFactory.getTransformer(exceptionRecord.getFormType());
        return bulkScanFormTransformer.transformIntoCaseData(exceptionRecord);
    }

    private void validateForTransformation(ExceptionRecord exceptionRecord) throws UnsupportedFormTypeException, InvalidDataException {

        OcrValidationResult ocrDataFieldsValidationResult
            =  validateBulkScanForm(exceptionRecord.getFormType(), exceptionRecord.getOcrDataFields());

        if (!ocrDataFieldsValidationResult.getStatus().equals(ValidationStatus.SUCCESS)) {
            throw new InvalidDataException(
                String.format("Validation of exception record %s finished with status %s",
                    exceptionRecord.getId(), ocrDataFieldsValidationResult.getStatus()),
                ocrDataFieldsValidationResult.getWarnings(),
                ocrDataFieldsValidationResult.getErrors()
            );
        }

        OcrValidationResult bulkScanFormsValidationResult = formAValidator.validateFormAScannedDocuments(exceptionRecord);

        if (!bulkScanFormsValidationResult.getStatus().equals(ValidationStatus.SUCCESS)) {
            throw new InvalidDataException(
                String.format("Validation of exception record attached documents %s finished with status %s",
                    exceptionRecord.getId(), bulkScanFormsValidationResult.getStatus()),
                bulkScanFormsValidationResult.getWarnings(),
                bulkScanFormsValidationResult.getErrors()
            );
        }
    }
}
