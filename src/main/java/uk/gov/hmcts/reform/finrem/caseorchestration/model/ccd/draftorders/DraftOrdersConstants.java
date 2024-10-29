package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders;

public class DraftOrdersConstants {

    private DraftOrdersConstants() {
        // Class contains only constants and should not be instantiated
    }

    public static final String AGREED_DRAFT_ORDER_OPTION = "anAgreedOrderFollowingAHearing";
    public static final String SUGGESTED_DRAFT_ORDER_OPTION = "aSuggestedDraftOrderPriorToAListedHearing";

    public static final String UPLOAD_PARTY_APPLICANT = "theApplicant";
    public static final String UPLOAD_PARTY_RESPONDENT = "theRespondent";

    public static final String CONFIRM_UPLOAD_DOCUMENTS_OPTION_CODE = "1";
    public static final String ORDER_TYPE = "orders";
    public static final String PSA_TYPE = "pensionSharingAnnexes";
}
