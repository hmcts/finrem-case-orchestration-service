package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.apache.commons.lang.ObjectUtils;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;

import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HELP_WITH_FEES_QUESTION;

public interface BaseController {

    default void validateCaseData(CallbackRequest callbackRequest) {
        if (callbackRequest == null || callbackRequest.getCaseDetails() == null
            || callbackRequest.getCaseDetails().getData() == null) {
            throw new InvalidCaseDataException(BAD_REQUEST.value(), "Missing  data from callbackRequest .");
        }
    }

    default boolean isPBAPayment(Map<String, Object> caseData) {
        return ObjectUtils.toString(caseData.get(HELP_WITH_FEES_QUESTION)).equalsIgnoreCase("no");
    }
}
