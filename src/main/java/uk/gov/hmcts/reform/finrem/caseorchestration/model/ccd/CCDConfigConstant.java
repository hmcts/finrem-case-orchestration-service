package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

public class CCDConfigConstant {

    //Case related
    public static final String STATE = "state";
    public static final String DIVORCE_CASE_NUMBER = "divorceCaseNumber";

    public static final String APPLICANT_FIRST_MIDDLE_NAME = "applicantFMName";
    public static final String APPLICANT_LAST_NAME = "applicantLName";
    public static final String APPLICANT_ADDRESS = "applicantAddress";


    public static final String APP_RESPONDENT_FIRST_MIDDLE_NAME = "appRespondentFMName";
    public static final String APP_RESPONDENT_LAST_NAME = "appRespondentLName";
    public static final String RESPONDENT_ADDRESS = "respondentAddress";
    public static final String RESP_SOLICITOR_ADDRESS = "rSolicitorAddress";

    //Solicitor related
    public static final String SOLICITOR_REFERENCE = "solicitorReference";
    public static final String SOLICITOR_NAME = "solicitorName";
    public static final String SOLICITOR_EMAIL = "solicitorEmail";
    public static final String SOLICITOR_ADDRESS = "solicitorAddress";
    public static final String CONTESTED_SOLICITOR_EMAIL = "applicantSolicitorEmail";
    public static final String CONTESTED_SOLICITOR_NAME = "applicantSolicitorName";
    public static final String SOLICITOR_FIRM = "solicitorFirm";
    public static final String APP_SOLICITOR_ADDRESS_CCD_FIELD = "solicitorAddress";
    public static final String RESP_SOLICITOR_ADDRESS_CCD_FIELD = "rSolicitorAddress";
    public static final String SOLICITOR_AGREE_TO_RECEIVE_EMAILS = "solicitorAgreeToReceiveEmails";
    public static final String APPLICANT_REPRESENTED = "applicantRepresented";

    //Application Type related
    public static final String D81_QUESTION = "d81Question";

    //Document related
    public static final String MINI_FORM_A = "miniFormA";
    public static final String PENSION_DOCS_COLLECTION = "pensionCollection";
    public static final String GENERAL_LETTER = "generalLetterCollection";
    public static final String GENERAL_LETTER_TEXT = "generalLetterBody";
    public static final String LATEST_CONSENT_ORDER = "latestConsentOrder";
    public static final String APPROVED_ORDER_COLLECTION = "approvedOrderCollection";
    public static final String FINAL_ORDER_COLLECTION = "finalOrderCollection";
    public static final String HEARING_ORDER_COLLECTION = "uploadHearingOrder";

    //Payment related
    public static final String ORDER_SUMMARY = "orderSummary";
    public static final String FEES = "Fees";
    public static final String VALUE = "value";
    public static final String FEE_AMOUNT = "FeeAmount";
    public static final String FEE_CODE = "FeeCode";
    public static final String FEE_VERSION = "FeeVersion";
    public static final String HELP_WITH_FEES_QUESTION = "helpWithFeesQuestion";
    public static final String PBA_NUMBER = "PBANumber";
    public static final String PBA_REFERENCE = "PBAreference";
    public static final String PBA_PAYMENT_REFERENCE = "PBAPaymentReference";
    public static final String AMOUNT_TO_PAY = "amountToPay";
    public static final String ISSUE_DATE = "issueDate";
    public static final String FAST_TRACK_DECISION = "fastTrackDecision";

    public static final String HEARING_DATE = "hearingDate";
    public static final String CASE_ALLOCATED_TO = "caseAllocatedTo";
    public static final String MIAM_ATTENDANCE = "applicantAttendedMIAM";
    public static final String MIAM_EXEMPTION = "claimingExemptionMIAM";
    public static final String IS_ADMIN = "isAdmin";
    public static final String YES = "Yes";
    public static final String NO = "No";
    public static final String FR_COURT_ADMIN = "caseworker-divorce-financialremedy-courtadmin";
    public static final String ROLES = "roles";
    public static final String ALLOCATED_COURT_LIST = "allocatedCourtList";
    public static final String JUDGE_ALLOCATED = "judgeAllocated";

    // Consent Order
    public static final String CONSENT_ORDER_APPROVED_NOTIFICATION_LETTER = "consentOrderApprovedNotificationLetter";

    // Bulk Printing
    public static final String BULK_PRINT_COVER_SHEET = "bulkPrintCoverSheet";
    public static final String BULK_PRINT_COVER_SHEET_APP = "bulkPrintCoverSheetApp";
    public static final String BULK_PRINT_COVER_SHEET_RES = "bulkPrintCoverSheetRes";
    public static final String BULK_PRINT_LETTER_ID = "bulkPrintLetterId";
    public static final String BULK_PRINT_LETTER_ID_APP = "bulkPrintLetterIdApp";
    public static final String BULK_PRINT_LETTER_ID_RES = "bulkPrintLetterIdRes";
    public static final String UPLOAD_ORDER = "uploadOrder";
}
