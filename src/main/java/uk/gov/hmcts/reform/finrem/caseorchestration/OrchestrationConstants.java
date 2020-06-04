package uk.gov.hmcts.reform.finrem.caseorchestration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

import static java.util.Arrays.asList;

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

    public static final String BINARY_URL_TYPE = "binary";

    public static final String YES_VALUE = "Yes";
    public static final String NO_VALUE = "No";

    // CTSC Contact Details
    public static final String CTSC_SERVICE_CENTRE = "Courts and Tribunals Service Centre";
    public static final String CTSC_CARE_OF = "c/o HMCTS Digital Financial Remedy";
    public static final String CTSC_PO_BOX = "12746";
    public static final String CTSC_TOWN = "HARLOW";
    public static final String CTSC_POSTCODE = "CM20 9QZ";
    public static final String CTSC_EMAIL_ADDRESS = "HMCTSFinancialRemedy@justice.gov.uk";
    public static final String CTSC_PHONE_NUMBER = "0300 303 0642";
    public static final String CTSC_OPENING_HOURS = "from 8.30am to 5pm";

    // List of allowed Document Subtypes that can be attached to a BSP Exception Record
    public static final List<String> ALLOWED_DOCUMENT_SUBTYPES = asList(
        "D81",
        "FormA",
        "P1",
        "PPF1",
        "P2",
        "PPF2",
        "PPF",
        "FormE",
        "CoverLetter",
        "OtherSupportDocuments",
        "DraftConsentOrder",
        "DecreeNisi",
        "DecreeAbsolute"
    );
}
