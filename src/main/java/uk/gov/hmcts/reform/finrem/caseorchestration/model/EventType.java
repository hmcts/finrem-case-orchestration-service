package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
public enum EventType {

    SEND_ORDER("FR_sendOrder"),
    PREPARE_FOR_HEARING("FR_prepareForHearing"),
    UPLOAD_CASE_FILES("FR_uploadCaseFiles"),
    UPLOAD_CONFIDENTIAL_DOCUMENT("FR_uploadConfidentialDocument"),
    UPLOAD_DOCUMENT_CONSENTED("FR_uploadDocument"),
    UPLOAD_DOCUMENT_CONTESTED("FR_uploadGeneralDocument"),
    INTERIM_HEARING("FR_listForInterimHearing"),
    SOLICITOR_CREATE("FR_solicitorCreate"),
    AMEND_APP_DETAILS("FR_amendApplicationDetails"),
    AMEND_CONTESTED_APP_DETAILS("FR_amendApplication"),
    AMEND_CONTESTED_PAPER_APP_DETAILS("FR_amendPaperApplication"),
    AMEND_CONTESTED_APPROVED_CONSENT_ORDER("FR_amendApprovedFinalOrder"),
    AMEND_CONSENT_ORDER("FR_amendedConsentOrder"),
    SEND_CONSENT_IN_CONTESTED_ORDER("FR_consentSendOrder"),
    RESPOND_TO_ORDER("FR_respondToOrder"),
    AMEND_CASE("FR_amendCase"),
    AMEND_CASE_CRON("FR_amendCaseCron"),
    APPROVE_ORDER("FR_approveApplication"),
    REJECT_ORDER("FR_orderRefusal"),
    CONSENT_ORDER_NOT_APPROVED("FR_consentOrderNotApproved"),
    GENERAL_APPLICATION("createGeneralApplication"),
    REJECT_GENERAL_APPLICATION("rejectGeneralApplication"),
    CLOSE("FR_close"),
    MANAGE_CASE_DOCUMENTS("FR_manageCaseDocuments"),
    UPLOAD_APPROVED_ORDER("FR_uploadApprovedOrder"),
    NEW_PAPER_CASE("FR_newPaperCase"),
    LIST_FOR_HEARING("FR_addSchedulingListingInfo"),
    GENERAL_APPLICATION_REFER_TO_JUDGE("FR_generalApplicationReferToJudge"),
    GENERAL_APPLICATION_OUTCOME("FR_GeneralApplicationOutcome"),
    GENERAL_APPLICATION_DIRECTIONS("FR_GeneralApplicationDirections"),
    GENERAL_ORDER_CONSENT_IN_CONTESTED("FR_generalOrderConsent"),
    GENERAL_ORDER("FR_generalOrder"),
    LIST_FOR_HEARING_CONSENTED("FR_listForHearing"),
    MANAGE_BARRISTER("FR_manageBarrister"),
    CASE_FLAG_CREATE("createFlags"),
    CASE_FLAG_MANAGE("manageFlags"),
    ISSUE_APPLICATION("FR_issueApplication"),
    REGENERATE_FORM_C("FR_regenerateFormC"),
    CONSENT_APPLICATION_APPROVED_IN_CONTESTED("FR_consentOrderApproved"),
    UPDATE_CONTESTED_GENERAL_APPLICATION("updateGeneralApplication"),
    READY_FOR_HEARING("FR_readyForHearing"),
    REFER_TO_JUDGE("FR_referToJudge"),
    REFER_TO_JUDGE_FROM_ORDER_MADE("FR_referToJudgeFromOrderMade"),
    REFER_TO_JUDGE_FROM_CONSENT_ORDER_APPROVED("FR_referToJudgeFromConsOrdApproved"),
    REFER_TO_JUDGE_FROM_CONSENT_ORDER_MADE("FR_referToJudgeFromConsOrdMade"),
    REFER_TO_JUDGE_FROM_AWAITING_RESPONSE("FR_referToJudgeFromAwaitingResponse"),
    REFER_TO_JUDGE_FROM_RESPOND_TO_ORDER("FR_referToJudgeFromRespondToOrder"),
    REFER_TO_JUDGE_FROM_CLOSE("FR_referToJudgeFromClose"),
    REASSIGN_JUDGE("FR_reassignJudge"),
    NOC_REQUEST("nocRequest"),
    MANUAL_PAYMENT("FR_manualPayment"),
    ASSIGN_TO_JUDGE_CONSENT("FR_assignToJudgeConsent"),
    MANAGE_INTERVENERS("manageInterveners"),
    CREATE_GENERAL_LETTER("FR_generalLetter"),
    CREATE_GENERAL_LETTER_JUDGE("FR_generalLetter_judge"),
    CREATE_GENERAL_EMAIL("FR_generalEmail"),
    SHARE_SELECTED_DOCUMENTS("shareSelectedDocuments"),
    CLEAR_APPLICANT_POLICY("clearApplicantPolicy"),
    CLEAR_RESPONDENT_POLICY("clearRespondentPolicy"),
    SOLICITOR_CW_DRAFT_ORDER("FR_solicitorDraftDirectionOrder"),
    JUDGE_DRAFT_ORDER("FR_judgeDraftDirectionOrder"),
    DIRECTION_UPLOAD_ORDER("FR_directionOrder"),
    CONSENT_ORDER("FR_consentOrder"),
    ASSIGN_DOCUMENT_CATEGORIES("FR_assignDocumentCategories"),
    GIVE_ALLOCATION_DIRECTIONS("FR_giveAllocationDirections"),
    UPDATE_FRC_INFORMATION("FR_updateFRCInformation"),
    UPDATE_COURT_INFO("FR_updateCourtInfo"),
    MANAGE_HEARING_BUNDLES("FR_manageHearingBundles"),
    MANAGE_SCANNED_DOCS("FR_manageScannedDocs"),
    UPDATE_CONTACT_DETAILS("FR_updateContactDetails"),
    CONSENT_SEND_ORDER_FOR_APPROVED_ORDER("FR_sendOrderForApproved"),
    HIDE_CASE("FR_hideCase"),
    UNHIDE_CASE("FR_unhideCase"),
    APPLICATION_PAYMENT_SUBMISSION("FR_applicationPaymentSubmission"),
    REMOVE_CASE_DOCUMENT("FR_removeCaseDocument"),
    REMOVE_USER_CASE_ACCESS("FR_removeUserCaseAccess"),
    DRAFT_ORDERS("FR_draftOrders"),
    @JsonEnumDefaultValue
    NONE("");

    private final String ccdType;

    @JsonValue
    public String getCcdType() {
        return ccdType;
    }

    public static EventType getEventType(String ccdType) {
        return Arrays.stream(EventType.values())
            .filter(eventTypeValue -> eventTypeValue.ccdType.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
