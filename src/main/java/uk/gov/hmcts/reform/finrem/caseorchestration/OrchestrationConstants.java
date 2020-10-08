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
    public static final String CASE_TYPE_ID_CONTESTED = "FinancialRemedyContested";
    public static final String PAPER_APPLICATION = "paperApplication";
    public static final String COURT_CONTACT_DETAILS = "courtContactDetails";

    // Bulk Scan & Printing Document SubTypes
    public static final String FORM_A_DOCUMENT = "FormA";
    public static final String D81_DOCUMENT = "D81";
    public static final String P1_DOCUMENT = "P1";
    public static final String PPF1_DOCUMENT = "PPF1";
    public static final String P2_DOCUMENT = "P2";
    public static final String PPF2_DOCUMENT = "PPF2";
    public static final String PPF_DOCUMENT = "PPF";
    public static final String FORM_E_DOCUMENT = "FormE";
    public static final String COVER_LETTER_DOCUMENT = "CoverLetter";
    public static final String OTHER_SUPPORT_DOCUMENTS = "OtherSupportDocuments";
    public static final String DRAFT_CONSENT_ORDER_DOCUMENT = "DraftConsentOrder";
    public static final String DECREE_NISI_DOCUMENT = "DecreeNisi";
    public static final String DECREE_ABSOLUTE_DOCUMENT = "DecreeAbsolute";

    public static final String BINARY_URL_TYPE = "binary";

    public static final String YES_VALUE = "Yes";
    public static final String NO_VALUE = "No";

    public static final String REGION = "region";

    // CTSC Contact Details
    public static final String CTSC_SERVICE_CENTRE = "Courts and Tribunals Service Centre";
    public static final String CTSC_CARE_OF = "c/o HMCTS Digital Financial Remedy";
    public static final String CTSC_PO_BOX = "12746";
    public static final String CTSC_TOWN = "HARLOW";
    public static final String CTSC_POSTCODE = "CM20 9QZ";
    public static final String CTSC_EMAIL_ADDRESS = "contactFinancialRemedy@justice.gov.uk";
    public static final String CTSC_PHONE_NUMBER = "0300 303 0642";
    public static final String CTSC_OPENING_HOURS = "from 8.30am to 5pm";
}
