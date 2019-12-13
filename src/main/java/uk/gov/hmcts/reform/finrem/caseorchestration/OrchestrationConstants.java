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
    public static final String BULK_SCAN_CASE_CREATE_EVENT_ID = "createCase";
    public static final String CASE_TYPE_ID_FR = "FINANCIAL_REMEDY";
    public static final String CASE_TYPE_ID_CONSENTED = "FinancialRemedyMVP2";
    public static final String CASE_TYPE_ID_CONTESTED = "FinancialRemedyContested";
}
