package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;

import java.util.Map;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HELP_WITH_FEES_QUESTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PBA_PAYMENT_REFERENCE;

public interface BaseController {

    /**
     * Throws exception if callback request is missing case data.
     */
    default void validateCaseData(CallbackRequest callbackRequest) {
        if (callbackRequest == null || callbackRequest.getCaseDetails() == null
            || callbackRequest.getCaseDetails().getData() == null) {
            throw new InvalidCaseDataException(BAD_REQUEST.value(), "Missing data from callbackRequest.");
        }
    }

    default boolean isPBAPayment(Map<String, Object> caseData) {
        return Objects.toString(caseData.get(HELP_WITH_FEES_QUESTION)).equalsIgnoreCase("no");
    }

    default boolean isPBAPaymentReferenceDoesNotExists(Map<String, Object> caseData) {
        return isEmpty((String) caseData.get(PBA_PAYMENT_REFERENCE));
    }
}
