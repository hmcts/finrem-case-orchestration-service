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

    // Letter Types
    public static final String FINANCIAL_REMEDY_PACK = "FINANCIAL_REMEDY_PACK";

    // CCD Fields
    public static final String APP_FIRST_AND_MIDDLE_NAME_CCD_FIELD = "applicantFMName";
    public static final String APP_LAST_NAME_CCD_FIELD = "applicantLName";
    public static final String APP_ADDRESS_CCD_FIELD = "applicantAddress";
    public static final String APP_SOLICITOR_ADDRESS_CCD_FIELD = "solicitorAddress";

    public static final String APP_RESP_FIRST_AND_MIDDLE_NAME_CCD_FIELD = "appRespondentFMName";
    public static final String APP_RESP_LAST_NAME_CCD_FIELD = "appRespondentLName";
    public static final String RESP_ADDRESS_CCD_FIELD = "respondentAddress";
    public static final String RESP_SOLICITOR_ADDRESS_CCD_FIELD = "rSolicitorAddress";

    // Document Generation
    public static final String DOCUMENT_FILENAME = "document_filename";
    public static final String DOCUMENT_URL = "document_binary_url";
    public static final String VALUE = "value";
}
