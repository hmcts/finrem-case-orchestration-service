package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CCDConfigConstant {

    //Case related
    public static final String STATE = "state";
    public static final String DIVORCE_CASE_NUMBER = "divorceCaseNumber";

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
    public static final String RESPONDENT_PHONE = "respondentPhone";
    public static final String RESPONDENT_EMAIL = "respondentEmail";

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
    public static final String CONSENTED_SOLICITOR_DX_NUMBER = "solicitorDXnumber";
    public static final String CONTESTED_SOLICITOR_DX_NUMBER = "applicantSolicitorDXnumber";

    //Respondent Solicitor Related
    public static final String CONSENTED_RESPONDENT_REPRESENTED = "appRespondentRep";
    public static final String CONTESTED_RESPONDENT_REPRESENTED = "respondentRepresented";
    public static final String RESP_SOLICITOR_NAME = "rSolicitorName";
    public static final String RESP_SOLICITOR_FIRM = "rSolicitorFirm";
    public static final String RESP_SOLICITOR_ADDRESS = "rSolicitorAddress";
    public static final String RESP_SOLICITOR_REFERENCE = "rSolicitorReference";
    public static final String RESP_SOLICITOR_PHONE = "rSolicitorPhone";
    public static final String RESP_SOLICITOR_EMAIL = "rSolicitorEmail";
    public static final String RESP_SOLICITOR_DX_NUMBER = "rSolicitorDXnumber";

    //Court related
    public static final String REGION = "regionList";
    public static final String REGION_CT = "region";

    //FRC List Names
    public static final String LONDON_FRC_LIST = "londonFRCList";
    public static final String LONDON_FRC_LIST_CT = "londonList";
    public static final String MIDLANDS_FRC_LIST = "midlandsFRCList";
    public static final String MIDLANDS_FRC_LIST_CT = "midlandsList";
    public static final String NORTHEAST_FRC_LIST = "northEastFRCList";
    public static final String NORTHEAST_FRC_LIST_CT = "northEastList";
    public static final String NORTHWEST_FRC_LIST = "northWestFRCList";
    public static final String NORTHWEST_FRC_LIST_CT = "northWestList";
    public static final String SOUTHEAST_FRC_LIST = "southEastFRCList";
    public static final String SOUTHEAST_FRC_LIST_CT = "southEastList";
    public static final String SOUTHWEST_FRC_LIST = "southWestFRCList";
    public static final String SOUTHWEST_FRC_LIST_CT = "southWestList";
    public static final String WALES_FRC_LIST = "walesFRCList";
    public static final String WALES_FRC_LIST_CT = "walesList";

    //Regions
    public static final String LONDON = "london";
    public static final String MIDLANDS = "midlands";
    public static final String NORTHEAST = "northeast";
    public static final String NORTHWEST = "northwest";
    public static final String SOUTHEAST = "southeast";
    public static final String SOUTHWEST = "southwest";
    public static final String WALES = "wales";

    //FRCs
    public static final String BEDFORDSHIRE = "bedfordshire";
    public static final String BIRMINGHAM = "birmingham";
    public static final String BRISTOLFRC = "bristol";
    public static final String CFC = "cfc";
    public static final String CLEAVELAND = "cleaveland";
    public static final String DEVON = "devon";
    public static final String DORSET = "dorset";
    public static final String HSYORKSHIRE = "hsyorkshire";
    public static final String KENT = "kentfrc";
    public static final String LANCASHIRE = "lancashire";
    public static final String LIVERPOOL = "liverpool";
    public static final String MANCHESTER = "manchester";
    public static final String NEWPORT = "newport";
    public static final String NORTHWALES = "northwales";
    public static final String NOTTINGHAM = "nottingham";
    public static final String NWYORKSHIRE = "nwyorkshire";
    public static final String OTHER = "other";
    public static final String SWANSEA = "swansea";
    public static final String THAMESVALLEY = "thamesvalley";

    //courtLists
    public static final String BEDFORDSHIRE_COURTLIST = "bedfordshireCourtList";
    public static final String BIRMINGHAM_COURTLIST = "birminghamCourtList";
    public static final String BRISTOL_COURTLIST = "bristolCourtList";
    public static final String CFC_COURTLIST = "cfcCourtList";
    public static final String CLEAVELAND_COURTLIST = "cleavelandCourtList";
    public static final String DEVON_COURTLIST = "devonCourtList";
    public static final String DORSET_COURTLIST = "dorsetCourtList";
    public static final String HSYORKSHIRE_COURTLIST = "humberCourtList";
    public static final String KENTFRC_COURTLIST = "kentSurreyCourtList";
    public static final String LANCASHIRE_COURTLIST = "lancashireCourtList";
    public static final String LIVERPOOL_COURTLIST = "liverpoolCourtList";
    public static final String LONDON_CFC = "cfc";
    public static final String LONDON_COURTLIST = "londonCourtList";
    public static final String MANCHESTER_COURTLIST = "manchesterCourtList";
    public static final String NEWPORT_COURTLIST = "newportCourtList";
    public static final String NORTH_WALES_COURTLIST = "northWalesCourtList";
    public static final String NOTTINGHAM_COURTLIST = "nottinghamCourtList";
    public static final String NWOTHER_COURTLIST = "otherNWCourtList";
    public static final String NWYORKSHIRE_COURTLIST = "nwyorkshireCourtList";
    public static final String SEOTHER_COURTLIST = "otherSECourtList";
    public static final String SWANSEA_COURTLIST = "swanseaCourtList";
    public static final String SWOTHER_COURTLIST = "otherSWCourtList";
    public static final String THAMESVALLEY_COURTLIST = "thamesvalleyCourtList";
    public static final String WALES_OTHER_COURTLIST = "welshOtherCourtList";

    //Lancashire Court Codes
    public static final String PRESTON = "FR_lancashireList_1";
    public static final String BLACKBURN = "FR_lancashireList_2";
    public static final String BLACKPOOL = "FR_lancashireList_3";
    public static final String LANCASTER = "FR_lancashireList_4";
    public static final String LEYLAND = "FR_lancashireList_5";
    public static final String REEDLEY = "FR_lancashireList_6";

    public static final String BARROW = "FR_lancashireList_7";
    public static final String CARLISLE = "FR_lancashireList_8";
    public static final String WEST_CUMBRIA = "FR_lancashireList_9";

    //Bedfordshire Court List
    public static final String PETERBOROUGH = "FR_bedfordshireList_1";
    public static final String CAMBRIDGE = "FR_bedfordshireList_2";
    public static final String BURY = "FR_bedfordshireList_3";
    public static final String NORWICH = "FR_bedfordshireList_4";
    public static final String IPSWICH = "FR_bedfordshireList_5";
    public static final String CHELMSFORD = "FR_bedfordshireList_6";
    public static final String SOUTHEND = "FR_bedfordshireList_7";
    public static final String BEDFORD = "FR_bedfordshireList_8";
    public static final String LUTON = "FR_bedfordshireList_9";
    public static final String HERTFORD = "FR_bedfordshireList_10";
    public static final String WATFORD = "FR_bedfordshireList_11";

    //Thamesvalley CourtList
    public static final String OXFORD = "FR_thamesvalleyList_1";
    public static final String READING = "FR_thamesvalleyList_2";
    public static final String MILTON_KEYNES = "FR_thamesvalleyList_3";
    public static final String SLOUGH = "FR_thamesvalleyList_4";

    //Devon CourtList
    public static final String PLYMOUTH = "FR_devonList_1";
    public static final String EXETER = "FR_devonList_2";
    public static final String TAUNTON = "FR_devonList_3";
    public static final String TORQUAY = "FR_devonList_4";
    public static final String BARNSTAPLE = "FR_devonList_5";
    public static final String TRURO = "FR_devonList_6";
    public static final String YEOVIL = "FR_devonList_7";
    public static final String BODMIN = "FR_devonList_8";

    //Dorset CourtList
    public static final String BOURNEMOUTH = "FR_dorsetList_1";
    public static final String WEYMOUTH = "FR_dorsetList_2";
    public static final String WINCHESTER = "FR_dorsetList_3";
    public static final String PORTSMOUTH = "FR_dorsetList_4";
    public static final String SOUTHAMPTON = "FR_dorsetList_5";
    public static final String ALDERSHOT = "FR_dorsetList_6";
    public static final String BASINGSTOKE = "FR_dorsetList_7";
    public static final String ISLE_OF_WIGHT = "FR_dorsetList_8";

    //Bristol CourtList
    public static final String BRISTOL = "FR_bristolList_1";
    public static final String GLOUCESTER = "FR_bristolList_2";
    public static final String SWINDON = "FR_bristolList_3";
    public static final String SALISBURY = "FR_bristolList_4";
    public static final String BATH = "FR_bristolList_5";
    public static final String WESTON = "FR_bristolList_6";

    //North Wales Court List
    public static final String WREXHAM = "FR_northwalesList_1";
    public static final String CAERNARFON = "FR_northwalesList_2";
    public static final String PRESTATYN = "FR_northwalesList_3";
    public static final String WELSHPOOL = "FR_northwalesList_4";
    public static final String MOLD = "FR_northwalesList_5";

    public static final String COURT_DETAILS_NAME_KEY = "courtName";
    public static final String COURT_DETAILS_ADDRESS_KEY = "courtAddress";
    public static final String COURT_DETAILS_PHONE_KEY = "phoneNumber";
    public static final String COURT_DETAILS_EMAIL_KEY = "email";

    // IDAM
    public static final String FR_COURT_ADMIN = "caseworker-divorce-financialremedy-courtadmin";
    public static final String ROLES = "roles";

    //contest courts
    public static final String CASE_ALLOCATED_TO = "caseAllocatedTo";
    public static final String APPLICANT_ATTENDED_MIAM = "applicantAttendedMIAM";
    public static final String FAMILY_MEDIATOR_MIAM = "familyMediatorMIAM";
    public static final String CLAIMING_EXEMPTION_MIAM = "claimingExemptionMIAM";
    public static final String IS_ADMIN = "isAdmin";

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
    public static final String FORM_C = "formC";
    public static final String FORM_G = "formG";
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
    public static final String PENSION_DOCUMENTS = "pensionDocuments";
    public static final String ORDER_LETTER = "orderLetter";

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
    public static final String GENERAL_APPLICATION_REFER_TO_JUDGE_EMAIL = "generalApplicationReferToJudgeEmail";

    // General application directions
    public static final String GENERAL_APPLICATION_DIRECTIONS_ADDITIONAL_INFORMATION = "generalApplicationDirectionsAdditionalInformation";
    public static final String GENERAL_APPLICATION_DIRECTIONS_COURT_ORDER_DATE = "generalApplicationDirectionsCourtOrderDate";
    public static final String GENERAL_APPLICATION_DIRECTIONS_DOCUMENT = "generalApplicationDirectionsDocument";
    public static final String GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED = "generalApplicationDirectionsHearingRequired";
    public static final String GENERAL_APPLICATION_DIRECTIONS_HEARING_DATE = "generalApplicationDirectionsHearingDate";
    public static final String GENERAL_APPLICATION_DIRECTIONS_HEARING_TIME = "generalApplicationDirectionsHearingTime";
    public static final String GENERAL_APPLICATION_DIRECTIONS_HEARING_TIME_ESTIMATE = "generalApplicationDirectionsHearingTimeEstimate";
    public static final String GENERAL_APPLICATION_DIRECTIONS_HEARING_REGION = "generalApplicationDirections_regionList";
    public static final String GENERAL_APPLICATION_DIRECTIONS_JUDGE_NAME = "generalApplicationDirectionsJudgeName";
    public static final String GENERAL_APPLICATION_DIRECTIONS_JUDGE_TYPE = "generalApplicationDirectionsJudgeType";
    public static final String GENERAL_APPLICATION_DIRECTIONS_PREFIX = "generalApplicationDirections_";
    public static final String GENERAL_APPLICATION_DIRECTIONS_RECITALS = "generalApplicationDirectionsRecitals";
    public static final String GENERAL_APPLICATION_DIRECTIONS_TEXT_FROM_JUDGE = "generalApplicationDirectionsTextFromJudge";

    // Generap Application FRCs
    public static final String GENERAL_APPLICATION_DIRECTIONS_LONDON_FRC = "generalApplicationDirections_londonFRCList";
    public static final String GENERAL_APPLICATION_DIRECTIONS_MIDLANDS_FRC = "generalApplicationDirections_midlandsFRCList";
    public static final String GENERAL_APPLICATION_DIRECTIONS_NORTHEAST_FRC = "generalApplicationDirections_northEastFRCList";
    public static final String GENERAL_APPLICATION_DIRECTIONS_NORTHWEST_FRC = "generalApplicationDirections_northWestFRCList";
    public static final String GENERAL_APPLICATION_DIRECTIONS_SOUTHEAST_FRC = "generalApplicationDirections_southEastFRCList";
    public static final String GENERAL_APPLICATION_DIRECTIONS_SOUTHWEST_FRC = "generalApplicationDirections_southWestFRCList";
    public static final String GENERAL_APPLICATION_DIRECTIONS_WALES_FRC = "generalApplicationDirections_walesFRCList";

    // Generap Application CourtLists
    public static final String GENERAL_APPLICATION_DIRECTIONS_BEDFORDSHIRE_COURT = "generalApplicationDirections_bedfordshireCourtList";
    public static final String GENERAL_APPLICATION_DIRECTIONS_BIRMINGHAM_COURT = "generalApplicationDirections_birminghamCourtList";
    public static final String GENERAL_APPLICATION_DIRECTIONS_BRISTOL_COURT = "generalApplicationDirections_bristolCourtList";
    public static final String GENERAL_APPLICATION_DIRECTIONS_CFC_COURT = "generalApplicationDirections_cfcCourtList";
    public static final String GENERAL_APPLICATION_DIRECTIONS_CLEVELAND_COURT = "generalApplicationDirections_cleavelandCourtList";
    public static final String GENERAL_APPLICATION_DIRECTIONS_DEVON_COURT = "generalApplicationDirections_cleavelandCourtList";
    public static final String GENERAL_APPLICATION_DIRECTIONS_DORSET_COURT = "generalApplicationDirections_dorsetCourtList";
    public static final String GENERAL_APPLICATION_DIRECTIONS_HUMBER_COURT = "generalApplicationDirections_humberCourtList";
    public static final String GENERAL_APPLICATION_DIRECTIONS_KENTSURREY_COURT = "generalApplicationDirections_kentSurreyCourtList";
    public static final String GENERAL_APPLICATION_DIRECTIONS_LANCASHIRE_COURT = "generalApplicationDirections_lancashireCourtList";
    public static final String GENERAL_APPLICATION_DIRECTIONS_LIVERPOOL_COURT = "generalApplicationDirections_liverpoolCourtList";
    public static final String GENERAL_APPLICATION_DIRECTIONS_MANCHESTER_COURT = "generalApplicationDirections_manchesterCourtList";
    public static final String GENERAL_APPLICATION_DIRECTIONS_NEWPORT_COURT = "generalApplicationDirections_newportCourtList";
    public static final String GENERAL_APPLICATION_DIRECTIONS_NOTTINGHAM_COURT = "generalApplicationDirections_nottinghamCourtList";
    public static final String GENERAL_APPLICATION_DIRECTIONS_NWYORKSHIRE_COURT = "generalApplicationDirections_nwyorkshireCourtList";
    public static final String GENERAL_APPLICATION_DIRECTIONS_SWANSEA_COURT = "generalApplicationDirections_swanseaCourtList";
    public static final String GENERAL_APPLICATION_DIRECTIONS_THAMESVALLEY_COURT = "generalApplicationDirections_thamesvalleyCourtList";
    public static final String GENERAL_APPLICATION_DIRECTIONS_WALES_OTHER_COURT = "generalApplicationDirections_welshOtherCourtList";

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

    //organisation policy
    public static final String ORGANISATION_POLICY_APPLICANT = "ApplicantOrganisationPolicy";
    public static final String ORGANISATION_POLICY_ROLE = "OrgPolicyCaseAssignedRole";
    public static final String ORGANISATION_POLICY_REF = "OrgPolicyReference";
    public static final String ORGANISATION_POLICY_ORGANISATION = "Organisation";
    public static final String ORGANISATION_POLICY_ORGANISATION_ID = "OrganisationID";
    public static final String APP_SOLICITOR_POLICY = "[APPSOLICITOR]";
    public static final String CREATOR_USER_ROLE = "[CREATOR]";

    //scheduled hearings
    public static final String ADDITIONAL_HEARING_DOCUMENT_COLLECTION = "additionalHearingDocuments";
    public static final String HEARING_TYPE = "hearingType";
    public static final String TIME_ESTIMATE = "timeEstimate";
    public static final String HEARING_DATE = "hearingDate";
    public static final String HEARING_TIME = "hearingTime";
    public static final String HEARING_ADDITIONAL_INFO = "additionalInformationAboutHearing";
    public static final String DIRECTION_DETAILS_COLLECTION_CT = "directionDetailsCollection";
    public static final String LOCAL_COURT_CT = "localCourt";
    public static final String HEARING_TYPE_CT = "typeOfHearing";
    public static final String TIME_ESTIMATE_CT = "timeEstimate";
    public static final String HEARING_DATE_CT = "dateOfHearing";
    public static final String HEARING_TIME_CT = "hearingTime";
    public static final String IS_ANOTHER_HEARING_CT = "isAnotherHearingYN";

    //draft hearing order
    public static final String LATEST_DRAFT_HEARING_ORDER  = "latestDraftHearingOrder";
    public static final String DRAFT_DIRECTION_ORDER_COLLECTION  = "draftDirectionOrderCollection";
    public static final String DRAFT_DIRECTION_DETAILS_COLLECTION = "draftDirectionDetailsCollection";
    public static final String DRAFT_DIRECTION_DETAILS_COLLECTION_RO = "draftDirectionDetailsCollectionRO";
    public static final String HEARING_ORDER_OTHER_COLLECTION = "hearingOrderOtherDocuments";

}
