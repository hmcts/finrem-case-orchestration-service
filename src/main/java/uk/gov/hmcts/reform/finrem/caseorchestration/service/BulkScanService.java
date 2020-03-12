package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.common.error.InvalidDataException;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResult;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.ValidationStatus;
import uk.gov.hmcts.reform.bsp.common.service.BulkScanFormValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.BulkScanFormTransformer;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.BulkScanFormTransformerFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.FinRemBulkScanFormValidatorFactory;

import java.util.List;
import java.util.Map;

@Service
public class BulkScanService {

    @Autowired
    private FinRemBulkScanFormValidatorFactory finRemBulkScanFormValidatorFactory;

    @Autowired
    private BulkScanFormTransformerFactory bulkScanFormTransformerFactory;

    public OcrValidationResult validateBulkScanForm(String formType, List<OcrDataField> ocrDataFields) throws UnsupportedFormTypeException {
        BulkScanFormValidator formValidator = finRemBulkScanFormValidatorFactory.getValidator(formType);
        return formValidator.validateBulkScanForm(ocrDataFields);
    }

    public Map<String, Object> transformBulkScanForm(ExceptionRecord exceptionRecord) throws UnsupportedFormTypeException, InvalidDataException {
        validateForTransformation(exceptionRecord);

        BulkScanFormTransformer bulkScanFormTransformer = bulkScanFormTransformerFactory.getTransformer(exceptionRecord.getFormType());
        return bulkScanFormTransformer.transformIntoCaseData(exceptionRecord);
    }

    private void validateForTransformation(ExceptionRecord exceptionRecord) throws UnsupportedFormTypeException {
        OcrValidationResult validationResult =  validateBulkScanForm(exceptionRecord.getFormType(), exceptionRecord.getOcrDataFields());

        if (!validationResult.getStatus().equals(ValidationStatus.SUCCESS)) {
            throw new InvalidDataException(
                String.format("Validation of exception record %s finished with status %s", exceptionRecord.getId(), validationResult.getStatus()),
                validationResult.getWarnings(),
                validationResult.getErrors()
            );
        }
    }
}
