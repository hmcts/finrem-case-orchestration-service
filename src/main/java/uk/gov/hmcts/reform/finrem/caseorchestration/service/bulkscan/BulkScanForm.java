package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan;

import lombok.Getter;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.BulkScanFormValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.FormAValidator;

@Getter
public enum BulkScanForm {

    FORM_A("formA", FormAValidator.class);

    private String formName;
    private Class<? extends BulkScanFormValidator> formValidatorClass;

    BulkScanForm(String formName, Class<? extends BulkScanFormValidator> formValidatorClass) {
        this.formName = formName;
        this.formValidatorClass = formValidatorClass;
    }
}
