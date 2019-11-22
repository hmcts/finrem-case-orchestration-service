package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.OcrDataField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.OcrValidationResult;

import java.util.List;

@Service
public class BulkScanValidationService {

    BulkScanFormValidatorFactory bulkScanFormValidatorFactory;

    public BulkScanValidationService(BulkScanFormValidatorFactory bulkScanFormValidatorFactory) {
        this.bulkScanFormValidatorFactory = bulkScanFormValidatorFactory;
    }

    public OcrValidationResult validate(String formType, List<OcrDataField> ocrDataFields) {
        BulkScanFormValidator formValidator = bulkScanFormValidatorFactory.getValidator(formType);
        formValidator.validate(ocrDataFields);
        return new OcrValidationResult(formValidator.getWarnings(), formValidator.getErrors());
    }
}
