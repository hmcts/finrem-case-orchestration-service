package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseData;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public abstract class AbstractBaseController {
    public static final String AWAITING_HWF_DECISION_STATE = "awaitingHWFDecision";
    public static final String APPLICATION_SUBMITTED_STATE = "applicationSubmitted";

    protected void validateCaseData(CCDRequest ccdRequest) {
        if (ccdRequest == null || ccdRequest.getCaseDetails() == null
                || ccdRequest.getCaseDetails().getCaseData() == null) {
            throw new InvalidCaseDataException(BAD_REQUEST.value(), "Missing case data from CCD request.");
        }
    }

    protected boolean isPBAPayment(CaseData caseData) {
        return caseData.getHelpWithFeesQuestion() != null
                && caseData.getHelpWithFeesQuestion().equalsIgnoreCase("no");
    }
}
