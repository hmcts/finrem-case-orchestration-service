package uk.gov.hmcts.reform.finrem.caseorchestration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrchestrationConstants {

    // Authentication
    public static final String SERVICE_AUTHORISATION_HEADER = "ServiceAuthorization";
    public static final String AUTHORIZATION_HEADER = "Authorization";

    // Bulk Scan & Printing
    public static final String BULK_SCAN_CASE_REFERENCE = "bulkScanCaseReference";
    public static final String CASE_TYPE_ID_CONSENTED = "FinancialRemedyMVP2";

    public static final String BINARY_URL_TYPE = "binary";

    public static final String YES_VALUE = "Yes";
    public static final String NO_VALUE = "No";

    public static final String APPLICANT = "Applicant";
    public static final String RESPONDENT = "Respondent";

    // Document Generation
    public static final String DOCUMENT_FILENAME = "document_filename";
    public static final String DOCUMENT_URL = "document_binary_url";
    public static final String VALUE = "value";
}
