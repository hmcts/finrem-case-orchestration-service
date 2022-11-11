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
    UPLOAD_GENERAL_DOCUMENT("FR_uploadGeneralDocument"),
    INTERIM_HEARING("FR_listForInterimHearing"),
    SOLICITOR_CREATE("FR_solicitorCreate"),
    AMEND_CONTESTED_APP_DETAILS("FR_amendApplication"),
    AMEND_CONTESTED_PAPER_APP_DETAILS("FR_amendPaperApplication"),
    AMEND_CONSENT_ORDER("FR_amendedConsentOrder"),
    RESPOND_TO_ORDER("FR_respondToOrder"),
    AMEND_CASE("FR_amendCase"),
    CONSENT_ORDER_NOT_APPROVED("FR_consentOrderNotApproved"),
    GENERAL_APPLICATION("createGeneralApplication"),
    REJECT_GENERAL_APPLICATION("rejectGeneralApplication"),
    CLOSE("FR_close"),
    MANAGE_CASE_DOCUMENTS("FR_manageCaseDocuments"),
    UPLOAD_APPROVED_ORDER("FR_uploadApprovedOrder"),
    LIST_FOR_HEARING("FR_addSchedulingListingInfo"),
    GENERAL_APPLICATION_REFER_TO_JUDGE("FR_generalApplicationReferToJudge"),
    GENERAL_APPLICATION_OUTCOME("FR_GeneralApplicationOutcome"),
    GENERAL_APPLICATION_DIRECTIONS("FR_GeneralApplicationDirections"),
    LIST_FOR_HEARING_CONSENTED("FR_listForHearing"),
    MANAGE_BARRISTER("FR_manageBarrister"),

    @JsonEnumDefaultValue
    NONE(""),
    MIGRATE_CASE("FR_migrateCase"),
    MIGRATE_Frc_CASE("FR_migrateFrcCase"),
    CASE_NOTES("FR_caseNotes"),

    AMENDED_CONSENT_ORDER("FR_amendedConsentOrder"),
    UPDATE_CONTACT_DETAILS("FR_updateContactDetails"),
    UPDATE_DUE_DATE("FR_updateDueDate"),
    UPLOAD_DOCUMENT("FR_uploadDocument"),
    AMEND_APPLICATION_DETAILS("FR_amendApplicationDetails"),
    UPDATE_ORDER("FR_updateOrder"),
    REFUND("FR_refund"),
    ATTACH_SCANNED_DOCS("attachScannedDocs"),
    HANDLE_EVIDENCE("handleEvidence"),

    APPLICATION_PAYMENT_SUBMISSION("FR_applicationPaymentSubmission"),
    HWF_DECISION("FR_HWFDecision"),
    PAYMENT_REQUIRED("FR_paymentRequired"),
    AWAITING_PAYMENT_RESPONSE_FROM_HWF("FR_awaitingPaymentResponseFromHWF"),
    HWF_DECISION_MADE("FR_HWFDecisionMade"),
    PAYMENT_MADE_FROM_HWF("FR_paymentMadeFromHWF"),
    AWAITING_PAYMENT_RESPONSE("FR_awaitingPaymentResponse"),
    PAYMENT_MADE("FR_paymenetMade"),
    HWF_DECISION_MADE_FROM_AWAITING_PAYMENT("FR_HWFDecisionMadeFromAwaitingPayment"),
    PAYMENT_MADE_FROM_AWAITING_PAYMENT("FR_paymentMadeFromAwaitingPayment"),
    ISSUE_APPLICATION("FR_issueApplication"),
    
    REFER_TO_JUDGE("FR_referToJudge"),
    GENERAL_ORDER("FR_generalOrder"),
    ORDER_REFUSAL("FR_orderRefusal"),
    APPROVE_APPLICATION("FR_approveApplication"),
    UPLOAD_ORDER("FR_uploadOrder"),
    UPLOAD_CONSENT_ORDER("FR_uploadConsentOrder"),
    REFER_TO_JUDGE_FROM_ORDER_MADE("FR_referToJudgeFromOrderMade"),
    REFER_TO_JUDGE_FROM_CONS_ORD_APPROVED("FR_referToJudgeFromConsOrdApproved"),
    REFER_TO_JUDGE_FROM_CONS_ORD_MADE("FR_referToJudgeFromConsOrdMade"),
    REFER_TO_JUDGE_FROM_AWAITING_RESPONSE("FR_referToJudgeFromAwaitingResponse"),
    REFER_TO_JUDGE_FROM_RESPOND_TO_ORDER("FR_referToJudgeFromRespondToOrder"),
    REFER_TO_JUDGE_FROM_CLOSE("FR_referToJudgeFromClose"),

    SEND_ORDER_FOR_APPROVED("FR_sendOrderForApproved"),
    GENERAL_LETTER("FR_generalLetter"),
    CALLBACK_REJECTED_ORDER("FR_callbackRejectedOrder"),
    CALLBACK_APPROVED_ORDER("FR_callbackApprovedOrder"),
    AMEND_FINAL_ORDER("FR_amendFinalOrder"),
    REASSIGN_JUDGE("FR_reassignJudge"),
    MANUAL_PAYMENT("FR_manualPayment"),
    NEW_PAPER_CASE("FR_newPaperCase"),
    ADD_PAYMENT("addPayment"),
    PAPER_RESPONSE_RECEIVED("paperResponseReceived"),
    UPDATE_COURT_INFO("FR_updateCourtInfo"),
    AWAITING_INFO("FR_awaitingInfo"),
    CASE_SUBMISSION_FROM_AWAITING_INFO("FR_caseSubmissionFromAwaitingInfo"),
    ISSUE_APPLICATION_FROM_AWAITING_INFO("FR_issueApplicationFromAwaitingInfo"),
    RESPONSE_RECEIVED_FROM_AWAITING_INFO("FR_responseReceivedFromAwaitingInfo"),
    GENERAL_ORDER_FROM_AWAITING_INFO("FR_generalOrderFromAwaitingInfo"),
    CONS_ORD_NOT_APPROVED_FROM_AWAITING_INFO("FR_consOrdNotApprovedFromAwaitingInfo"),
    CONS_ORD_APPROVED_FROM_AWAITING_INFO("FR_consOrdApprovedFromAwaitingInfo"),
    GENERAL_EMAIL("FR_generalEmail"),
    UPLOAD_PENSION_DOCUMENT_SOLICITOR("FR_UploadPensionDocument_Solicitor"),
    UPLOAD_PENSION_DOCUMENT_COURT_ADMIN("FR_UploadPensionDocument_CourtAdmin"),
    SOL_UPLOAD_DOCUMENT("FR_solUploadDocument"),
    SOL_COMPLETE_UPLOAD_DOCUMENT("FR_solCompleteUploadDocument"),
    CASE_SUBMISSION_FROM_INFO_RECEIVED("FR_caseSubmissionFromInfoReceived"),
    ISSUE_APPLICATION_FROM_INFO_RECEIVED("FR_issueApplicationFromInfoReceived"),
    RESPONSE_RECEIVED_FROM_INFO_RECEIVED("FR_responseReceivedFromInfoReceived"),
    GENERAL_ORDER_FROM_INFO_RECEIVED("FR_generalOrderFromInfoReceived"),
    CONS_ORD_NOT_APPROVED_FROM_INFO_RECEIVED("FR_consOrdNotApprovedFromInfoReceived"),
    CONS_ORD_APPROVED_FROM_INFO_RECEIVED("FR_consOrdApprovedFromInfoReceived"),
    UPDATE_RESPONDENT_CONTACT_DETAILS("FR_updateRespondentContactDetails"),
    TRANSFERRED_TO_LOCAL_COURT("FR_TransferredToLocalCourt"),
    NOC_REQUEST("nocRequest"),
    APPLY_NOC_DECISION("applyNocDecision"),
    ADD_SCHEDULING_LISTING_INFO("FR_addSchedulingListingInfo"),
    SUBMIT_UPLOADED_CASE_FILES("FR_submitUploadedCaseFiles"),
    ASSIGN_TO_JUDGE("FR_assignToJudge"),
    GENERAL_ORDER_COURT_ADMIN("FR_generalOrderCourtAdmin"),
    APPLICATION_NOT_APPROVED("FR_applicationNotApproved"),
    HWF_FEE_ACCOUNT_DEBITED("FR_HWFFeeAccountDebited"),
    HWF_FEE_ACCOUNT_DEBITED_FROM_AWAITING_PAYMENT("FR_HWFFeeAccountDebitedFromAwaitingPayment"),
    ALLOCATE_TO_JUDGE("FR_allocateToJudge"),
    PROGRESS_TO_SCHEDULING_AND_LISTING("FR_progressToSchedulingAndListing"),
    GIVE_ALLOCATION_DIRECTIONS("FR_giveAllocationDirections"),
    RETURN_TO_GATE_KEEPING_AND_ALLOCATION("FR_returnTogateKeepingAndAllocation"),
    RETURN_TO_SCHEDULING_AND_HEARING("FR_returnToschedulingAndHearing"),
    RETURN_TO_PREPARE_FOR_HEARING("FR_returnToprepareForHearing"),
    RETURN_TO_CASE_FILE_SUBMITTED("FR_returnToCaseFileSubmitted"),
    JUDGE_TO_DRAFT_ORDER("FR_judgeToDraftOrder"),
    SOLICITOR_TO_DRAFT_ORDER("FR_solicitorToDraftOrder"),
    SOLICITOR_DRAFT_DIRECTION_ORDER("FR_solicitorDraftDirectionOrder"),
    DRAFT_ORDER_NOT_APPROVED("FR_draftOrderNotApproved"),
    SEND_REFUSAL_REASON("FR_sendRefusalReason"),
    DRAFT_ORDER_APPROVED("FR_draftOrderApproved"),
    JUDGE_DRAFT_DIRECTION_ORDER("FR_judgeDraftDirectionOrder"),
    RECALL_ORDER("FR_RecallOrder"),
    CREATE_CASE("createCase"),
    ATTACH_SCANNED_DOCS_WITH_OCR("attachScannedDocsWithOcr"),
    GENERAL_LETTER_JUDGE("FR_generalLetter_judge"),
    CONSENT_ORDER("FR_consentOrder"),
    ASSIGN_TO_JUDGE_CONSENT("FR_assignToJudgeConsent"),
    CONSENT_ORDER_ASSIGN_JUDGE("FR_consentOrderAssignJudge"),
    CONSENT_ORDER_APPROVED("FR_consentOrderApproved"),
    GENERAL_ORDER_CONSENT("FR_generalOrderConsent"),
    RESPOND_TO_CONSENT_ORDER("FR_respondToConsentOrder"),
    CONSENT_SEND_ORDER("FR_consentSendOrder"),
    CREATE_GENERAL_APPLICATION("FR_createGeneralApplication"),
    AMEND_APPROVED_FINAL_ORDER("FR_amendApprovedFinalOrder"),
    DIRECTION_ORDER("FR_directionOrder"),
    RESERVE_FOR_JUDGE("FR_ReserveForJudge"),
    SHARE_DOCUMENTS_WITH_APPLICANT("FR_shareDocumentsWithApplicant"),
    SHARE_DOCUMENTS_WITH_RESP("FR_shareDocumentsWithResp"),
    REVERT_FROM_SCHEDULING_TO_SUBMITTED("FR_revertFromSchedulingToSubmitted"),
    REVERT_TO_PREPARE_FOR_HEARING("FR_revertToPrepareForHearing"),
    ADD_LIST_FOR_INTERIM_HEARING_INFO("FR_addListForInterimHearingInfo"),
    UPDATE_Frc_INFORMATION("FR_updateFrcInformation"),
    MANAGE_HEARING_BUNDLES("FR_manageHearingBundles"),
    CONSENT_APPLICATION_APPROVED_IN_CONTESTED("FR_consentOrderApproved");

    private final String ccdType;

    @JsonValue
    public String getCcdType() {
        return ccdType;
    }

    public static EventType getEventType(String ccdType) {
        log.info("Event type to process {}", ccdType);
        return Arrays.stream(EventType.values())
            .filter(eventTypeValue -> eventTypeValue.ccdType.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
