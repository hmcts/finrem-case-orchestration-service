package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CCDConfigConstant {

    //Case related
    public static final String STATE = "state";
    public static final String DIVORCE_CASE_NUMBER = "divorceCaseNumber";
    public static final String CASE_DETAILS_BEFORE = "case_details_before";

    public static final String APPLICANT_FIRST_MIDDLE_NAME = "applicantFMName";
    public static final String APPLICANT_LAST_NAME = "applicantLName";
    public static final String APPLICANT_ADDRESS = "applicantAddress";
    public static final String APPLICANT_PHONE = "applicantPhone";
    public static final String APPLICANT_EMAIL = "applicantEmail";

    public static final String CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME = "appRespondentFMName";
    public static final String CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME = "respondentFMName";
    public static final String CONSENTED_RESPONDENT_LAST_NAME = "appRespondentLName";
    public static final String CONTESTED_RESPONDENT_LAST_NAME = "respondentLName";
    public static final String RESPONDENT_ADDRESS = "respondentAddress";

    //Applicant Solicitor related
    public static final String SOLICITOR_REFERENCE = "solicitorReference";
    public static final String CONSENTED_SOLICITOR_NAME = "solicitorName";
    public static final String SOLICITOR_EMAIL = "solicitorEmail";
    public static final String SOLICITOR_PHONE = "solicitorPhone";
    public static final String CONSENTED_SOLICITOR_ADDRESS = "solicitorAddress";
    public static final String CONTESTED_SOLICITOR_ADDRESS = "applicantSolicitorAddress";
    public static final String CONTESTED_SOLICITOR_EMAIL = "applicantSolicitorEmail";
    public static final String CONTESTED_SOLICITOR_NAME = "applicantSolicitorName";
    public static final String CONSENTED_SOLICITOR_FIRM = "solicitorFirm";
    public static final String CONTESTED_SOLICITOR_FIRM = "applicantSolicitorFirm";
    public static final String APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED = "solicitorAgreeToReceiveEmails";
    public static final String APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED = "applicantSolicitorConsentForEmails";
    public static final String APPLICANT_REPRESENTED = "applicantRepresented";
    public static final String APPLICANT_REPRESENTED_PAPER = "applicantRepresentedPaper";

    //Respondent Solicitor Related
    public static final String CONSENTED_RESPONDENT_REPRESENTED = "appRespondentRep";
    public static final String CONTESTED_RESPONDENT_REPRESENTED = "respondentRepresented";
    public static final String RESP_SOLICITOR_NAME = "rSolicitorName";
    public static final String RESP_SOLICITOR_ADDRESS = "rSolicitorAddress";
    public static final String RESP_SOLICITOR_REFERENCE = "rSolicitorReference";

    //Court related
    public static final String KENT = "kentfrc";
    public static final String REGION = "regionList";
    public static final String WALES_FRC_LIST = "walesFRCList";
    public static final String SOUTHEAST_FRC_LIST = "southEastFRCList";
    public static final String SOUTHWEST_FRC_LIST = "southWestFRCList";
    public static final String NORTHEAST_FRC_LIST = "northEastFRCList";
    public static final String NORTHWEST_FRC_LIST = "northWestFRCList";
    public static final String LONDON_FRC_LIST = "londonFRCList";
    public static final String MIDLANDS_FRC_LIST = "midlandsFRCList";
    public static final String LIVERPOOL = "liverpool";
    public static final String MANCHESTER = "manchester";
    public static final String OTHER = "other";
    public static final String CFC = "cfc";
    public static final String NEWPORT = "newport";
    public static final String MIDLANDS = "midlands";
    public static final String LONDON = "london";
    public static final String NORTHWEST = "northwest";
    public static final String NORTHEAST = "northeast";
    public static final String SOUTHEAST = "southeast";
    public static final String SOUTHWEST = "southwest";
    public static final String WALES = "wales";
    public static final String SWANSEA = "swansea";
    public static final String CLEAVELAND = "cleaveland";
    public static final String NWYORKSHIRE = "nwyorkshire";
    public static final String HSYORKSHIRE = "hsyorkshire";
    public static final String NOTTINGHAM = "nottingham";
    public static final String BIRMINGHAM = "birmingham";
    public static final String COURT_DETAILS_NAME_KEY = "courtName";
    public static final String COURT_DETAILS_ADDRESS_KEY = "courtAddress";
    public static final String COURT_DETAILS_PHONE_KEY = "phoneNumber";
    public static final String COURT_DETAILS_EMAIL_KEY = "email";
    public static final String ALLOCATED_COURT_LIST = "allocatedCourtList";

    //contest courts
    public static final String HEARING_DATE = "hearingDate";
    public static final String CASE_ALLOCATED_TO = "caseAllocatedTo";
    public static final String APPLICANT_ATTENDED_MIAM = "applicantAttendedMIAM";
    public static final String FAMILY_MEDIATOR_MIAM = "familyMediatorMIAM";
    public static final String CLAIMING_EXEMPTION_MIAM = "claimingExemptionMIAM";
    public static final String IS_ADMIN = "isAdmin";
    public static final String FR_COURT_ADMIN = "caseworker-divorce-financialremedy-courtadmin";
    public static final String ROLES = "roles";
    public static final String KENTFRC_COURTLIST = "kentSurreyCourtList";
    public static final String SEOTHER_COURTLIST = "otherSECourtList";
    public static final String SWOTHER_COURTLIST = "otherSWCourtList";
    public static final String NEWPORT_COURTLIST = "newportCourtList";
    public static final String SWANSEA_COURTLIST = "swanseaCourtList";
    public static final String WALES_OTHER_COURTLIST = "welshOtherCourtList";
    public static final String CLEAVELAND_COURTLIST = "cleavelandCourtList";
    public static final String NWYORKSHIRE_COURTLIST = "nwyorkshireCourtList";
    public static final String HSYORKSHIRE_COURTLIST = "humberCourtList";
    public static final String NOTTINGHAM_COURTLIST = "nottinghamCourtList";
    public static final String BIRMINGHAM_COURTLIST = "birminghamCourtList";
    public static final String LONDON_CFC = "cfc";
    public static final String CFC_COURTLIST = "cfcCourtList";
    public static final String LONDON_COURTLIST = "londonCourtList";
    public static final String MANCHESTER_COURTLIST = "manchesterCourtList";
    public static final String NWOTHER_COURTLIST = "otherNWCourtList";
    public static final String LIVERPOOL_COURTLIST = "liverpoolCourtList";

    //Draft Order Related
    public static final String SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER = "solicitorResponsibleForDraftingOrder";
    public static final String APPLICANT_SOLICITOR = "applicantSolicitor";
    public static final String RESPONDENT_SOLICITOR = "respondentSolicitor";

    //Refusal Order / Application Not Approved (Contested)
    public static final String CONTESTED_APPLICATION_NOT_APPROVED_REASONS_FOR_REFUSAL = "judgeNotApprovedReasons";
    public static final String CONTESTED_APPLICATION_NOT_APPROVED_PREVIEW_DOCUMENT = "refusalOrderPreviewDocument";
    public static final String CONTESTED_APPLICATION_NOT_APPROVED_COLLECTION = "refusalOrderCollection";
    public static final String CONTESTED_APPLICATION_NOT_APPROVED_LATEST_DOCUMENT = "latestRefusalOrder";
    public static final String CONTESTED_APPLICATION_NOT_APPROVED_JUDGE_NAME = "refusalOrderJudgeName";
    public static final String CONTESTED_APPLICATION_NOT_APPROVED_JUDGE_TYPE = "refusalOrderJudgeType";
    public static final String CONTESTED_APPLICATION_NOT_APPROVED_DATE = "refusalOrderDate";

    //Application Type related
    public static final String CONSENT_D81_QUESTION = "consentD81Question";

    //Nature of Application
    public static final String CONSENTED_NATURE_OF_APPLICATION = "natureOfApplication2";
    public static final String CONSENT_IN_CONTESTED_NATURE_OF_APPLICATION = "consentNatureOfApplicationChecklist";
    public static final String CONSENTED_NATURE_OF_APPLICATION_3A = "natureOfApplication3a";
    public static final String CONSENT_IN_CONTESTED_NATURE_OF_APPLICATION_3A = "consentNatureOfApplicationAddress";
    public static final String CONSENTED_NATURE_OF_APPLICATION_3B = "natureOfApplication3b";
    public static final String CONSENT_IN_CONTESTED_NATURE_OF_APPLICATION_3B = "consentNatureOfApplicationMortgage";
    public static final String CONSENTED_ORDER_FOR_CHILDREN = "orderForChildrenQuestion1";
    public static final String CONSENT_IN_CONTESTED_ORDER_FOR_CHILDREN = "consentOrderForChildrenQuestion1";
    public static final String CONSENTED_NATURE_OF_APPLICATION_5 = "natureOfApplication5";
    public static final String CONSENT_IN_CONTESTED_NATURE_OF_APPLICATION_5 = "consentNatureOfApplication5";
    public static final String CONSENTED_NATURE_OF_APPLICATION_6 = "natureOfApplication6";
    public static final String CONSENT_IN_CONTESTED_NATURE_OF_APPLICATION_6 = "consentNatureOfApplication6";
    public static final String CONSENTED_NATURE_OF_APPLICATION_7 = "natureOfApplication7";
    public static final String CONSENT_IN_CONTESTED_NATURE_OF_APPLICATION_7 = "consentNatureOfApplication7";

    //Document related
    public static final String MINI_FORM_A = "miniFormA";
    public static final String MINI_FORM_A_CONSENTED_IN_CONTESTED = "consentMiniFormA";
    public static final String PENSION_DOCS_COLLECTION = "pensionCollection";
    public static final String FORM_A_COLLECTION = "copyOfPaperFormA";
    public static final String OTHER_DOCS_COLLECTION = "otherCollection";
    public static final String GENERAL_LETTER = "generalLetterCollection";
    public static final String GENERAL_LETTER_BODY = "generalLetterBody";
    public static final String GENERAL_ORDER_PREVIEW_DOCUMENT = "generalOrderPreviewDocument";
    public static final String GENERAL_ORDER_CREATED_BY = "generalOrderCreatedBy";
    public static final String GENERAL_ORDER_BODY_TEXT = "generalOrderBodyText";
    public static final String GENERAL_ORDER_DATE = "generalOrderDate";
    public static final String GENERAL_ORDER_ADDRESS_TO = "generalOrderAddressTo";
    public static final String GENERAL_ORDER_JUDGE_TYPE = "generalOrderJudgeType";
    public static final String GENERAL_ORDER_RECITALS = "generalOrderRecitals";
    public static final String GENERAL_ORDER_JUDGE_NAME = "generalOrderJudgeName";
    public static final String GENERAL_ORDER_LATEST_DOCUMENT = "generalOrderLatestDocument";
    public static final String GENERAL_ORDER_COLLECTION_CONTESTED = "generalOrders";
    public static final String GENERAL_ORDER_COLLECTION_CONSENTED_IN_CONTESTED = "generalOrdersConsent";
    public static final String GENERAL_ORDER_COLLECTION_CONSENTED = "generalOrderCollection";
    public static final String APPROVED_ORDER_COLLECTION = "approvedOrderCollection";
    public static final String FINAL_ORDER_COLLECTION = "finalOrderCollection";
    public static final String HEARING_ORDER_COLLECTION = "uploadHearingOrder";
    public static final String RESPOND_TO_ORDER_DOCUMENTS = "respondToOrderDocuments";
    public static final String CONSENT_ORDER = "consentOrder";
    public static final String LATEST_CONSENT_ORDER = "latestConsentOrder";
    public static final String AMENDED_CONSENT_ORDER_COLLECTION = "amendedConsentOrderCollection";
    public static final String ORDER_REFUSAL_COLLECTION = "orderRefusalCollection";
    public static final String ORDER_REFUSAL_PREVIEW_COLLECTION = "orderRefusalPreviewDocument";
    public static final String GENERAL_LETTER_ADDRESS_TO = "generalLetterAddressTo";
    public static final String GENERAL_LETTER_RECIPIENT = "generalLetterRecipient";
    public static final String GENERAL_LETTER_RECIPIENT_ADDRESS = "generalLetterRecipientAddress";
    public static final String GENERAL_LETTER_PREVIEW = "generalLetterPreview";
    public static final String CONTESTED_CONSENT_ORDER_COLLECTION = "Contested_ConsentedApprovedOrders";
    public static final String CONTESTED_CONSENT_PENSION_COLLECTION = "consentPensionCollection";
    public static final String CONTESTED_CONSENT_ORDER_NOT_APPROVED_COLLECTION = "consentedNotApprovedOrders";
    public static final String CONSENTED_ORDER_DIRECTION_JUDGE_TITLE = "orderDirectionJudge";
    public static final String CONTESTED_ORDER_DIRECTION_JUDGE_TITLE = "consentSelectJudge";
    public static final String CONSENTED_ORDER_DIRECTION_JUDGE_NAME = "orderDirectionJudgeName";
    public static final String CONTESTED_ORDER_DIRECTION_JUDGE_NAME = "consentJudgeName";
    public static final String CONSENTED_ORDER_DIRECTION_DATE = "orderDirectionDate";
    public static final String CONTESTED_ORDER_DIRECTION_DATE = "consentDateOfOrder";


    public static final String FR_RESPOND_TO_ORDER = "FR_respondToOrder";
    public static final String FR_AMENDED_CONSENT_ORDER = "FR_amendedConsentOrder";
    public static final String FR_CONSENT_ORDER = "FR_consentOrder";
    public static final String FR_RESPOND_TO_CONSENT_ORDER = "FR_respondToConsentOrder";

    //general email
    public static final String GENERAL_EMAIL_COLLECTION = "generalEmailCollection";
    public static final String GENERAL_EMAIL_RECIPIENT = "generalEmailRecipient";
    public static final String GENERAL_EMAIL_CREATED_BY = "generalEmailCreatedBy";
    public static final String GENERAL_EMAIL_BODY = "generalEmailBody";

    // General application
    public static final String GENERAL_APPLICATION_DOCUMENT_LATEST = "generalApplicationLatestDocument";
    public static final String GENERAL_APPLICATION_DOCUMENT = "generalApplicationDocument";
    public static final String GENERAL_APPLICATION_RECEIVED_FROM = "generalApplicationReceivedFrom";
    public static final String GENERAL_APPLICATION_CREATED_BY = "generalApplicationCreatedBy";
    public static final String GENERAL_APPLICATION_HEARING_REQUIRED = "generalApplicationHearingRequired";
    public static final String GENERAL_APPLICATION_TIME_ESTIMATE = "generalApplicationTimeEstimate";
    public static final String GENERAL_APPLICATION_SPECIAL_MEASURES = "generalApplicationSpecialMeasures";
    public static final String GENERAL_APPLICATION_DRAFT_ORDER = "generalApplicationDraftOrder";
    public static final String GENERAL_APPLICATION_DOCUMENT_COLLECTION = "generalApplicationCollection";
    public static final String GENERAL_APPLICATION_DOCUMENT_LATEST_DATE = "generalApplicationLatestDocumentDate";
    public static final String GENERAL_APPLICATION_PRE_STATE = "generalApplicationPreState";

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
    public static final String CONSENTED_AUTHORISATION_FIRM = "authorisationFirm";
    public static final String CONTESTED_AUTHORISATION_FIRM = "solicitorFirm";

    // Bulk Printing
    public static final String BULK_PRINT_COVER_SHEET_APP = "bulkPrintCoverSheetApp";
    public static final String BULK_PRINT_COVER_SHEET_RES = "bulkPrintCoverSheetRes";
    public static final String BULK_PRINT_LETTER_ID_APP = "bulkPrintLetterIdApp";
    public static final String BULK_PRINT_LETTER_ID_RES = "bulkPrintLetterIdRes";
    public static final String UPLOAD_ORDER = "uploadOrder";

    //Contested Case Documents
    public static final String CONTESTED_UPLOADED_DOCUMENTS = "uploadCaseDocument";
    public static final String APPLICANT_CORRESPONDENCE_COLLECTION = "appCorrespondenceCollection";
    public static final String APPLICANT_FR_FORM_COLLECTION = "appFRFormsCollection";
    public static final String APPLICANT_EVIDENCE_COLLECTION = "appEvidenceCollection";
    public static final String APPLICANT_TRIAL_BUNDLE_COLLECTION = "appTrialBundleCollection";
    public static final String RESPONDENT_CORRESPONDENCE_COLLECTION = "respCorrespondenceCollection";
    public static final String RESPONDENT_FR_FORM_COLLECTION = "respFRFormsCollection";
    public static final String RESPONDENT_EVIDENCE_COLLECTION = "respEvidenceCollection";
    public static final String RESPONDENT_TRIAL_BUNDLE_COLLECTION = "respTrialBundleCollection";

    public static final String AMENDED_CONSENT_ORDER = "AmendedConsentOrder";

    //states
    public static final String CONSENTED_ORDER_APPROVED = "consentedOrderApproved";
}
