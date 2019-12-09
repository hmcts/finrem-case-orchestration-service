package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.OcrDataField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.validation.out.OcrValidationResult;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BulkScanValidationService {

    private final BulkScanFormValidatorFactory bulkScanFormValidatorFactory;

    public OcrValidationResult validate(String formType, List<OcrDataField> ocrDataFields) throws Exception {
        BulkScanFormValidator formValidator = bulkScanFormValidatorFactory.getValidator(formType);
        formValidator.validate(ocrDataFields);
        return new OcrValidationResult(formValidator.getWarnings(), formValidator.getErrors());
    }
}
