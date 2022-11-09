package uk.gov.hmcts.reform.finrem.caseorchestration.model;

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
    AMEND_APP_DETAILS("FR_amendApplicationDetails"),
    AMEND_CONTESTED_APP_DETAILS("FR_amendApplication"),
    AMEND_CONTESTED_PAPER_APP_DETAILS("FR_amendPaperApplication"),
    AMEND_CONSENT_ORDER("FR_amendedConsentOrder"),
    RESPOND_TO_ORDER("FR_respondToOrder"),
    AMEND_CASE("FR_amendCase"),
    APPROVE_ORDER("FR_approveApplication"),
    REJECT_ORDER("FR_orderRefusal"),
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
    ISSUE_APPLICATION("FR_issueApplication"),
    CONTESTED_NEW_PAPER_CASE("FR_newPaperCase"),
    NONE("");

    private final String ccdType;

    public static EventType getEventType(String ccdType) {
        log.info("Event type to process {}", ccdType);
        return Arrays.stream(EventType.values())
            .filter(eventTypeValue -> eventTypeValue.ccdType.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    public String getCcdType() {
        return ccdType;
    }
}
