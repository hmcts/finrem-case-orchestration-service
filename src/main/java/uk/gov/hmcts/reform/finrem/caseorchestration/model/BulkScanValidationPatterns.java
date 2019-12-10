package uk.gov.hmcts.reform.finrem.caseorchestration.model;

public class BulkScanValidationPatterns {

    public static final String CCD_EMAIL_REGEX = "^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$";
    public static final String CCD_PHONE_NUMBER_REGEX = "^[0-9 +().-]{9,}$";
}
