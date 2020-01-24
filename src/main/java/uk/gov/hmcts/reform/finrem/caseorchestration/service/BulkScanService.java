package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;
import uk.gov.hmcts.reform.bsp.common.model.transformation.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResult;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.BulkScanFormTransformer;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.BulkScanFormTransformerFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.BulkScanFormValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.BulkScanFormValidatorFactory;

import java.util.List;
import java.util.Map;

@Service
public class BulkScanService {

    @Autowired
    private BulkScanFormValidatorFactory bulkScanFormValidatorFactory;

    @Autowired
    private BulkScanFormTransformerFactory bulkScanFormTransformerFactory;

    public OcrValidationResult validateBulkScanForm(String formType, List<OcrDataField> ocrDataFields) throws UnsupportedFormTypeException {
        BulkScanFormValidator formValidator = bulkScanFormValidatorFactory.getValidator(formType);
        return formValidator.validateBulkScanForm(ocrDataFields);
    }

    public Map<String, Object> transformBulkScanForm(ExceptionRecord exceptionRecord) throws UnsupportedFormTypeException {
        BulkScanFormTransformer bulkScanFormTransformer = bulkScanFormTransformerFactory.getTransformer(exceptionRecord.getFormType());
        return bulkScanFormTransformer.transformIntoCaseData(exceptionRecord);
    }
}