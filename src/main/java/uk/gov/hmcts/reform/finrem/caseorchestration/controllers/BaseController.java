package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseData;

import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public interface BaseController {
    default void validateCaseData(CCDRequest ccdRequest) {
        if (ccdRequest == null || ccdRequest.getCaseDetails() == null
                || ccdRequest.getCaseDetails().getCaseData() == null) {
            throw new InvalidCaseDataException(BAD_REQUEST.value(), "Missing case data from CCD request.");
        }
    }

    default void validateCaseData(CallbackRequest callbackRequest) {
        if (callbackRequest == null || callbackRequest.getCaseDetails() == null
                || callbackRequest.getCaseDetails().getData() == null) {
            throw new InvalidCaseDataException(BAD_REQUEST.value(), "Missing  data from callbackRequest .");
        }
    }

    default boolean isPBAPayment(CaseData caseData) {
        return caseData.getHelpWithFeesQuestion() != null
                && caseData.getHelpWithFeesQuestion().equalsIgnoreCase("no");
    }

    default boolean isPBAPayment(Map<String, Object> caseData) {
        return caseData.get("helpWithFeesQuestion") != null
                && caseData.get("helpWithFeesQuestion").toString().equalsIgnoreCase("no");
    }
}
