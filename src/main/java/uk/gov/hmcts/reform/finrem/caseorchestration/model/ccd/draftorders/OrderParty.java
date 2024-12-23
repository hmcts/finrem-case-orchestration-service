package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.UPLOAD_PARTY_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.UPLOAD_PARTY_RESPONDENT;

public enum OrderParty {
    APPLICANT,
    RESPONDENT;

    public static OrderParty forUploadParty(String uploadParty) {
        if (UPLOAD_PARTY_APPLICANT.equals(uploadParty)) {
            return APPLICANT;
        } else if (UPLOAD_PARTY_RESPONDENT.equals(uploadParty)) {
            return RESPONDENT;
        } else {
            return null;
        }
    }
}
