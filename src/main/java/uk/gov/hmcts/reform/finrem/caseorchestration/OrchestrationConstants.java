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

    public static final String CTSC_COURT_NAME = "Family Court at the Courts and Tribunal Service Centre";
    public static final String CTSC_COURT_ADDRESS = "PO Box 12746, Harlow, CM20 9QZ";
    public static final String CTSC_SERVICE_CENTRE = "Courts and Tribunals Service Centre";
    public static final String CTSC_CARE_OF = "c/o HMCTS Digital Financial Remedy";
    public static final String CTSC_PO_BOX = "12746";
    public static final String CTSC_TOWN = "HARLOW";
    public static final String CTSC_POSTCODE = "CM20 9QZ";
    public static final String CTSC_EMAIL_ADDRESS = "contactFinancialRemedy@justice.gov.uk";
    public static final String CTSC_PHONE_NUMBER = "0300 303 0642";
    public static final String CTSC_OPENING_HOURS = "from 8am to 6pm, Monday to Friday";

    // VARIATION ORDER RELATED

    public static final String VARIATION_ORDER = "Variation Order";
    public static final String CV_ORDER_CAMELCASE_LABEL_FIELD = "consentVariationOrderLabelC";
    public static final String VARIATION_ORDER_CAMELCASE_LABEL_VALUE = "Consent / Variation Order";
    public static final String CONSENT_ORDER_CAMELCASE_LABEL_VALUE = "Consent Order";
    public static final String CV_LOWERCASE_LABEL_FIELD = "consentVariationOrderLabelL";
    public static final String VARIATION_ORDER_LOWERCASE_LABEL_VALUE = "consent / variation order";
    public static final String CONSENT_ORDER_LOWERCASE_LABEL_VALUE = "consent order";
    public static final String CV_OTHER_DOC_LABEL_FIELD = "otherDocLabel";
    public static final String CV_OTHER_DOC_LABEL_VALUE =
        "If you are applying for variation order, please upload the order you are varying";
    public static final String CONSENT_OTHER_DOC_LABEL_VALUE =
        "Upload other order documentation related to your application";
}
