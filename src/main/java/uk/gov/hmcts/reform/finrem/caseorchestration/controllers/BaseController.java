package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseData;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public interface BaseController {
    default void validateCaseData(CCDRequest ccdRequest) {
        if (ccdRequest == null || ccdRequest.getCaseDetails() == null
                || ccdRequest.getCaseDetails().getCaseData() == null
                || ccdRequest.getCaseDetails().getCaseData().getDivorceCaseNumber() == null) {
            throw new InvalidCaseDataException(BAD_REQUEST.value(), "Missing case data from CCD request.");
        }
    }

    default boolean isPBAPayment(CaseData caseData) {
        return caseData.getHelpWithFeesQuestion() != null
                && caseData.getHelpWithFeesQuestion().equalsIgnoreCase("no");
    }
}
